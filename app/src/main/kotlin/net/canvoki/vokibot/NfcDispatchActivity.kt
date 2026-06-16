package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox
import net.canvoki.vokibot.common.ErrorSplash
import net.canvoki.vokibot.common.ExecutionState
import net.canvoki.vokibot.common.Loading
import net.canvoki.vokibot.common.NotAutomatedYet
import net.canvoki.vokibot.common.TriggerDispatcher

class NfcDispatchActivity : ComponentActivity() {
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent
        val context = this

        setContent {
            val intent = currentIntent.value ?: return@setContent
            AppScaffold {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    NfcDispatchScreen(
                        intent = intent,
                        onDone = { finish() },
                        onCreateAutomation = { triggerId ->
                            editAutomationForTrigger(context = context, triggerId)
                            finish()
                        },
                        iconRes = NfcTrigger.iconRes,
                        searchingText = stringResource(R.string.nfc_trigger_searching),
                        executingText = stringResource(R.string.nfc_trigger_executing),
                        notRegisteredTitle = stringResource(R.string.nfc_trigger_detected),
                        notRegisteredHelp = stringResource(R.string.nfc_trigger_not_registered),
                        noAutomationHelp = stringResource(R.string.nfc_trigger_no_automation),
                        createAutomationText = stringResource(R.string.nfc_trigger_create_automation),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent
    }
}

@Composable
private fun NfcDispatchScreen(
    intent: Intent,
    onDone: () -> Unit,
    onCreateAutomation: (triggerId: String) -> Unit,
    iconRes: Int,
    searchingText: String,
    executingText: String,
    notRegisteredTitle: String,
    notRegisteredHelp: String,
    noAutomationHelp: String,
    createAutomationText: String,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    val uid = remember(intent) { extractUidFromIntent(intent) }
    var executionState by remember { mutableStateOf<ExecutionState>(ExecutionState.Idle) }
    var registeredTrigger by remember { mutableStateOf<NfcTrigger?>(null) }
    val noTagMessage = stringResource(R.string.nfc_trigger_no_tag)
    var showNameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid == null) {
            executionState = ExecutionState.Error(noTagMessage)
            return@LaunchedEffect
        }

        executionState = ExecutionState.Searching

        val trigger = repository.loadNfcTrigger(uid = uid)
        registeredTrigger = trigger

        if (trigger == null) {
            executionState = ExecutionState.NoTrigger
            return@LaunchedEffect
        }

        executionState = ExecutionState.Executing
        val hadAutomations =
            Automation.executeByTrigger(repository, trigger.id, context) {
                (context as? ComponentActivity)?.runOnUiThread { onDone() }
            }
        if (!hadAutomations) {
            executionState = ExecutionState.NoAutomation
            return@LaunchedEffect
        }
    }

    when (executionState) {
        is ExecutionState.Idle,
        is ExecutionState.Searching,
        -> {
            Loading(searchingText)
        }
        is ExecutionState.Executing -> {
            Loading(executingText)
        }
        is ExecutionState.Error -> {
            ErrorSplash(text = (executionState as ExecutionState.Error)?.message ?: "...")
        }
        is ExecutionState.NoTrigger -> {
            NotAutomatedYet(
                iconRes = iconRes,
                title = notRegisteredTitle,
                subtitle = uid ?: "???",
                help = notRegisteredHelp,
                actionText = createAutomationText,
                action = {
                    showNameDialog = true
                },
            )
        }
        is ExecutionState.NoAutomation -> {
            NotAutomatedYet(
                iconRes = iconRes,
                title = registeredTrigger?.getTitle(context) ?: notRegisteredTitle,
                subtitle = uid ?: "???",
                help = noAutomationHelp,
                actionText = createAutomationText,
                action = {
                    uid?.let { rawUid ->
                        onCreateAutomation(NfcTrigger.idFromUid(rawUid))
                    }
                },
            )
        }
    }
    InputDialog(
        show = showNameDialog,
        title = stringResource(R.string.nfc_trigger_name_dialog_title),
        label = stringResource(R.string.nfc_editor_name_label),
        placeholder = stringResource(R.string.nfc_editor_name_placeholder),
        confirmText = stringResource(R.string.nfc_trigger_name_dialog_confirm),
        dismissText = stringResource(R.string.nfc_trigger_name_dialog_cancel),
        onDismiss = { showNameDialog = false },
        onConfirm = { value ->
            if (uid != null) {
                val trigger =
                    NfcTrigger(
                        displayName = value,
                        uid = uid,
                    )
                repository.trigger.save(trigger)
                showNameDialog = false
                onCreateAutomation(trigger.id)
            }
        },
    )
}

private fun extractUidFromIntent(intent: Intent): String? =
    if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
        intent.action == NfcAdapter.ACTION_TECH_DISCOVERED
    ) {
        val tag =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            }
        tag?.id?.joinToString(":") { "%02X".format(it) }
    } else {
        null
    }

fun editAutomationForTrigger(
    context: Context,
    triggerId: String,
) {
    val editorIntent =
        Intent(context, AutomationEditorActivity::class.java).apply {
            putExtra("trigger_id", triggerId)
        }
    context.startActivity(editorIntent)
}

package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox
import net.canvoki.vokibot.common.ErrorSplash
import net.canvoki.vokibot.common.ExecutionState
import net.canvoki.vokibot.common.Loading
import net.canvoki.vokibot.common.NotAutomatedYet

class NfcDispatchActivity : ComponentActivity() {
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent
        val context = this

        setContent {
            val intent = currentIntent.value ?: return@setContent
            val uid = remember(intent) { extractUidFromIntent(intent) }
            val triggerId = remember(uid) { uid?.let { NfcTrigger.idFromUid(it) } }
            val repository = remember { FileDataRepository.fromContext(context) }
            AppScaffold {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    NfcDispatchScreen(
                        triggerId = triggerId,
                        description = uid ?: "???",
                        onDone = { finish() },
                        onCreateAutomation = { id ->
                            editAutomationForTrigger(context = context, id)
                            finish()
                        },
                        onCreateTrigger = { name ->
                            if (uid != null) {
                                val newTrigger =
                                    NfcTrigger(
                                        displayName = name,
                                        uid = uid,
                                    )
                                repository.trigger.save(newTrigger)
                                editAutomationForTrigger(context = context, newTrigger.id)
                                finish()
                            }
                        },
                        iconRes = NfcTrigger.iconRes,
                        badInputError = stringResource(R.string.nfc_trigger_no_tag),
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
    triggerId: String?,
    onDone: () -> Unit,
    onCreateAutomation: (triggerId: String) -> Unit,
    onCreateTrigger: (name: String) -> Unit,
    iconRes: Int,
    description: String,
    badInputError: String,
    searchingText: String,
    executingText: String,
    notRegisteredTitle: String,
    notRegisteredHelp: String,
    noAutomationHelp: String,
    createAutomationText: String,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    var executionState by remember { mutableStateOf<ExecutionState>(ExecutionState.Idle) }
    var trigger by remember { mutableStateOf<Trigger?>(null) }
    var showNameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(triggerId) {
        if (triggerId == null) {
            executionState = ExecutionState.Error(badInputError)
            return@LaunchedEffect
        }

        executionState = ExecutionState.Searching

        trigger = repository.trigger.load(id = triggerId)
        val triggerNotNull = trigger

        if (triggerNotNull == null) {
            executionState = ExecutionState.NoTrigger
            return@LaunchedEffect
        }

        executionState = ExecutionState.Executing
        val hadAutomations =
            Automation.executeByTrigger(repository, triggerNotNull.id, context) {
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
                subtitle = description,
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
                title = trigger?.getTitle(context) ?: notRegisteredTitle,
                subtitle = description,
                help = noAutomationHelp,
                actionText = createAutomationText,
                action = {
                    onCreateAutomation(triggerId!!)
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
            showNameDialog = false
            onCreateTrigger(value)
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

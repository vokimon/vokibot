package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox

class NfcTriggerActivity : ComponentActivity() {
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent

        setContent {
            val intent = currentIntent.value ?: return@setContent
            NfcActivityScreen(
                intent = intent,
                onAutomationExecuted = { finish() },
                onCreateAutomation = { triggerId ->
                    editAutomationForTrigger(context = this, triggerId)
                    finish()
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent
    }
}

@Composable
private fun NfcActivityScreen(
    intent: Intent,
    onAutomationExecuted: () -> Unit,
    onCreateAutomation: (triggerId: String) -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    val uid = remember(intent) { extractUidFromIntent(intent) }
    var executionState by remember { mutableStateOf<ExecutionState>(ExecutionState.Idle) }
    var registeredTrigger by remember { mutableStateOf<NfcTrigger?>(null) }

    LaunchedEffect(uid) {
        if (uid == null) {
            executionState = ExecutionState.Error
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
                (context as? ComponentActivity)?.runOnUiThread { onAutomationExecuted() }
            }
        if (!hadAutomations) {
            executionState = ExecutionState.NoAutomation
            return@LaunchedEffect
        }
    }

    AppScaffold {
        WatermarkBox(
            watermark = painterResource(R.drawable.ic_brand),
        ) {
            uid?.let {
                NfcUidDisplayScreen(
                    uid = uid,
                    triggerName = registeredTrigger?.displayName,
                    executionState = executionState,
                    onCreateAutomation = { triggerId -> onCreateAutomation(triggerId) },
                )
            }
        }
    }
}

@Composable
private fun NfcUidDisplayScreen(
    uid: String?,
    triggerName: String?,
    executionState: ExecutionState,
    onCreateAutomation: (String) -> Unit,
) {
    val context = LocalContext.current
    var showNameDialog by remember { mutableStateOf(false) }
    val repository = remember { FileDataRepository.fromContext(context) }

    when (executionState) {
        is ExecutionState.Searching -> {
            Loading(stringResource(R.string.nfc_trigger_searching))
        }
        is ExecutionState.Executing -> {
            Loading(stringResource(R.string.nfc_trigger_executing))
        }
        is ExecutionState.NoTrigger -> {
            NotAutomatedYet(
                iconRes = NfcTrigger.iconRes,
                title = stringResource(R.string.nfc_trigger_detected),
                subtitle = uid ?: "???",
                help = stringResource(R.string.nfc_trigger_not_registered),
                actionText = stringResource(R.string.nfc_trigger_create_automation),
                action = {
                    showNameDialog = true
                },
            )
        }
        is ExecutionState.NoAutomation -> {
            NotAutomatedYet(
                iconRes = NfcTrigger.iconRes,
                title = triggerName ?: stringResource(R.string.nfc_trigger_detected),
                subtitle = uid ?: "???",
                help = stringResource(R.string.nfc_trigger_no_automation),
                actionText = stringResource(R.string.nfc_trigger_create_automation),
                action = {
                    uid?.let { rawUid ->
                        onCreateAutomation(NfcTrigger.idFromUid(rawUid))
                    }
                },
            )
        }
        is ExecutionState.Error, is ExecutionState.Idle -> {
            FullCenter() {
                Text(
                    text = stringResource(R.string.nfc_trigger_no_tag),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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

@Composable
fun Loading(text: String) {
    FullCenter() {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(text)
    }
}

@Composable
fun FullCenter(content: @Composable ()->Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content.invoke()
    }
}

@Composable
fun NotAutomatedYet(
    iconRes: Int,
    title: String,
    subtitle: String,
    help: String,
    actionText: String,
    action: () -> Unit,
) {
    FullCenter() {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = help,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = { action.invoke() }) {
            Text(actionText)
        }
    }
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

private sealed class ExecutionState {
    data object Idle : ExecutionState()

    data object Searching : ExecutionState()

    data object Executing : ExecutionState()

    data object NoTrigger : ExecutionState()

    data object NoAutomation : ExecutionState()

    data object Error : ExecutionState()
}

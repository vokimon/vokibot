package net.canvoki.vokibot

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox
import net.canvoki.shared.log

class NfcTriggerActivity : ComponentActivity() {
    // Single source of truth for the latest intent.
    // Updating this triggers recomposition without destroying dialog state.
    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent

        setContent {
            val intent = currentIntent.value ?: return@setContent
            NfcActivityScreen(
                intent = intent,
                onAutomationExecuted = { finish() },
                onCreateAutomation = { triggerId, triggerType ->
                    val editorIntent = Intent(this, AutomationEditorActivity::class.java).apply {
                        putExtra("trigger_type", triggerType)
                        putExtra("trigger_id", triggerId)
                    }
                    startActivity(editorIntent)
                    finish()
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent // Triggers recomposition, preserves dialog state
    }
}

@Composable
private fun NfcActivityScreen(
    intent: Intent,
    onAutomationExecuted: () -> Unit,
    onCreateAutomation: (triggerId: String, triggerType: String) -> Unit,
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

        val trigger = repository.nfcTrigger.load(uid)
        registeredTrigger = trigger

        if (trigger == null) {
            executionState = ExecutionState.NoTrigger
            return@LaunchedEffect
        }

        val automations = repository.automation.all()
            .filter { it.triggerType == "nfc" && it.triggerId == trigger.id }

        if (automations.isEmpty()) {
            executionState = ExecutionState.NoAutomation
            return@LaunchedEffect
        }

        executionState = ExecutionState.Executing

        CoroutineScope(Dispatchers.IO).launch {
            automations.forEach { automation ->
                automation.commandIds.forEach { cmdId ->
                    repository.command.load(cmdId)?.execute(context)
                }
            }
            (context as? ComponentActivity)?.runOnUiThread {
                onAutomationExecuted()
            }
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
                    onCreateAutomation = { triggerId, triggerType ->
                        onCreateAutomation(triggerId, triggerType)
                    }
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
    onCreateAutomation: (String, String) -> Unit,
) {
    val context = LocalContext.current
    var showNameDialog by remember { mutableStateOf(false) }
    var triggerNameInput by remember { mutableStateOf("") }
    var defaultNfcName = stringResource(R.string.nfc_trigger_default_name)
    val repository = remember { FileDataRepository.fromContext(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (executionState) {
            is ExecutionState.Searching -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.nfc_trigger_searching))
            }
            is ExecutionState.Executing -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.nfc_trigger_executing))
            }
            is ExecutionState.NoTrigger -> {
                Icon(
                    painter = painterResource(R.drawable.ic_nfc),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.nfc_trigger_detected),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uid ?: "???",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.nfc_trigger_not_registered),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    showNameDialog = true
                    triggerNameInput = ""
                }) {
                    Text(stringResource(R.string.nfc_trigger_create_automation))
                }
            }
            is ExecutionState.NoAutomation -> {
                Icon(
                    painter = painterResource(R.drawable.ic_nfc),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = triggerName ?: stringResource(R.string.nfc_trigger_detected),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uid ?: "???",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.nfc_trigger_no_automation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onCreateAutomation(uid?.replace(":", "_")?:"", "nfc") }) {
                    Text(stringResource(R.string.nfc_trigger_create_automation))
                }
            }
            is ExecutionState.Error, is ExecutionState.Idle -> {
                Text(
                    text = stringResource(R.string.nfc_trigger_no_tag),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    // Dialog renders only when explicitly opened
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = {
                showNameDialog = false
            },
            title = { Text(stringResource(R.string.nfc_trigger_name_dialog_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = triggerNameInput,
                        onValueChange = { triggerNameInput = it },
                        label = { Text(stringResource(R.string.nfc_trigger_name_label)) },
                        placeholder = { Text(stringResource(R.string.nfc_trigger_name_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Theoretically the case, cannot happen
                        if (uid != null) {
                            val trigger = NfcTrigger(
                                displayName = triggerNameInput.ifBlank { defaultNfcName },
                                uid = uid,
                            )
                            repository.nfcTrigger.save(trigger)
                            showNameDialog = false
                            onCreateAutomation(trigger.id, "nfc")
                        }
                    },
                    enabled = triggerNameInput.isNotBlank()
                ) {
                    Text(stringResource(R.string.nfc_trigger_name_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNameDialog = false
                }) {
                    Text(stringResource(R.string.nfc_trigger_name_dialog_cancel))
                }
            }
        )
    }
}

private fun extractUidFromIntent(intent: Intent): String? {
    return if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
        intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {

        val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        tag?.id?.joinToString(":") { "%02X".format(it) }
    } else {
        null
    }
}

private sealed class ExecutionState {
    data object Idle : ExecutionState()
    data object Searching : ExecutionState()
    data object Executing : ExecutionState()
    data object NoTrigger : ExecutionState()
    data object NoAutomation : ExecutionState()
    data object Error : ExecutionState()
}

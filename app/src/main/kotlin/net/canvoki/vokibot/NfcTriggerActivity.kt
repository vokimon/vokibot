package net.canvoki.vokibot

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox

class NfcTriggerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NfcActivityScreen(
                intent = intent,
                onAutomationExecuted = { finish() },
                onNoAutomation = { /* Show UI for cases 2 & 3 */ }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setContent {
            NfcActivityScreen(
                intent = intent,
                onAutomationExecuted = { finish() },
                onNoAutomation = { /* Show UI for cases 2 & 3 */ }
            )
        }
    }
}

@Composable
private fun NfcActivityScreen(
    intent: Intent,
    onAutomationExecuted: () -> Unit,
    onNoAutomation: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    val uid = remember(intent) { extractUidFromIntent(intent) }
    var executionState by remember { mutableStateOf<ExecutionState>(ExecutionState.Idle) }

    // Case 1: Check for automation and execute silently
    LaunchedEffect(uid) {
        uid?.let { detectedUid ->
            executionState = ExecutionState.Searching
            val trigger = repository.nfcTrigger.load(detectedUid.replace(":", "_"))

            if (trigger != null) {
                // Find automations that use this trigger
                val automations = repository.automation.all()
                    .filter { it.triggerType == "nfc" && it.triggerId == trigger.id }

                if (automations.isNotEmpty()) {
                    executionState = ExecutionState.Executing
                    CoroutineScope(Dispatchers.IO).launch {
                        automations.forEach { automation ->
                            automation.commandIds.forEach { cmdId ->
                                repository.command.load(cmdId)?.execute(context)
                            }
                        }
                        // Post back to main thread to finish
                        (context as? ComponentActivity)?.runOnUiThread {
                            onAutomationExecuted()
                        }
                    }
                } else {
                    // Case 3: Trigger exists but no automation
                    executionState = ExecutionState.NoAutomation
                    onNoAutomation()
                }
            } else {
                // Case 2: Tag not registered at all
                executionState = ExecutionState.NoTrigger
                onNoAutomation()
            }
        } ?: run {
            executionState = ExecutionState.Error
            onNoAutomation()
        }
    }

    // Only show UI if we're not executing silently
    if (executionState !is ExecutionState.Executing && executionState !is ExecutionState.Searching) {
        AppScaffold {
            WatermarkBox(
                watermark = painterResource(R.drawable.ic_brand),
            ) {
                NfcUidDisplayScreen(
                    uid = uid,
                    executionState = executionState,
                    onCreateTrigger = { /* Stub for case 2 */ },
                    onCreateAutomation = { /* Stub for case 3 */ }
                )
            }
        }
    }
}

@Composable
private fun NfcUidDisplayScreen(
    uid: String?,
    executionState: ExecutionState,
    onCreateTrigger: () -> Unit,
    onCreateAutomation: () -> Unit,
) {
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
            else -> {
                // Show UID display for manual creation flows
                if (uid != null) {
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
                        text = uid,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Case 2: Tag not registered → offer to create trigger + automation
                    if (executionState is ExecutionState.NoTrigger) {
                        Text(
                            text = stringResource(R.string.nfc_trigger_not_registered),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onCreateTrigger) {
                            Text(stringResource(R.string.nfc_trigger_create_automation))
                        }
                    }
                    // Case 3: Trigger exists but no automation → offer to create automation
                    else if (executionState is ExecutionState.NoAutomation) {
                        Text(
                            text = stringResource(R.string.nfc_trigger_no_automation),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onCreateAutomation) {
                            Text(stringResource(R.string.nfc_trigger_create_automation))
                        }
                    }
                    // Fallback hint
                    else {
                        Text(
                            text = stringResource(R.string.nfc_trigger_uid_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.nfc_trigger_no_tag),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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

/**
 * Sealed class representing the NFC trigger execution flow state.
 */
private sealed class ExecutionState {
    data object Idle : ExecutionState()
    data object Searching : ExecutionState()
    data object Executing : ExecutionState()
    data object NoTrigger : ExecutionState()    // Case 2: Tag not in repo
    data object NoAutomation : ExecutionState() // Case 3: Trigger exists, no automation
    data object Error : ExecutionState()
}

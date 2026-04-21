package net.canvoki.vokibot

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationEditor(
    modifier: Modifier = Modifier,
    editingId: String?,
    triggerPickResult: Pair<String, String>?,
    commandPickResult: String?,
    onTriggerConsumed: () -> Unit,
    onCommandConsumed: () -> Unit,
    onRequestTrigger: () -> Unit,
    onRequestAddCommand: () -> Unit,
    onSave: (Automation) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    var name by rememberSaveable { mutableStateOf("") }
    var triggerType by rememberSaveable { mutableStateOf("") }
    var triggerId by rememberSaveable { mutableStateOf("") }
    var commandIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var originalState by remember { mutableStateOf<Triple<String, String, List<String>>?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val isDirty = originalState == null ||
            name != originalState!!.first ||
            triggerId != originalState!!.second ||
            commandIds != originalState!!.third

    LaunchedEffect(editingId) {
        name = ""
        triggerType = ""
        triggerId = ""
        commandIds = emptyList()

        if (editingId != null) {
            repository.automation.load(editingId)?.let { existing ->
                name = existing.name
                triggerType = existing.triggerType
                triggerId = existing.triggerId
                commandIds = existing.commandIds
            }
        }
        originalState = Triple(name, triggerId, commandIds)
    }

    LaunchedEffect(triggerPickResult) {
        triggerPickResult?.let { (type, id) ->
            triggerType = type
            triggerId = id
            onTriggerConsumed()
        }
    }

    LaunchedEffect(commandPickResult) {
        commandPickResult?.let { id ->
            if (!commandIds.contains(id)) {
                commandIds = commandIds + id
            }
            onCommandConsumed()
        }
    }

    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.automation_discard_title)) },
            text = { Text(stringResource(R.string.automation_discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                    Text(stringResource(R.string.automation_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.automation_discard_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = {
                val automation = Automation(name.trim(), triggerType, triggerId, commandIds)
                repository.automation.save(automation)
                onSave(automation)
            },
            enabled = triggerId.isNotBlank() && commandIds.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.End),
        ) {
            Text(stringResource(R.string.automation_done))
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.automation_name_label)) },
            placeholder = { Text(stringResource(R.string.automation_name_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Text(stringResource(R.string.automation_trigger_label), style = MaterialTheme.typography.titleMedium)

        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onRequestTrigger),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_flash_on),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    val triggerName = remember(triggerId) {
                        if (triggerType == "nfc" && triggerId.isNotBlank()) {
                            repository.nfcTrigger.load(triggerId)?.displayName
                        } else null
                    }
                    Text(
                        text = triggerName ?: stringResource(R.string.automation_trigger_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (triggerId.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    )
                    if (triggerId.isBlank()) {
                        Text(
                            stringResource(R.string.automation_trigger_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.automation_commands_label), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onRequestAddCommand) {
                Icon(
                    painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.automation_add_command_desc),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (commandIds.isEmpty()) {
            Text(
                stringResource(R.string.automation_commands_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val commandNames = remember(commandIds) {
                    commandIds.map { id -> repository.command.load(id)?.displayName ?: id }
                }
                commandNames.forEachIndexed { index, cmdName ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cmdName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                commandIds = commandIds.filterIndexed { i, _ -> i != index }
                            }) {
                                Icon(
                                    painterResource(R.drawable.ic_delete),
                                    contentDescription = stringResource(R.string.automation_remove_command_desc),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

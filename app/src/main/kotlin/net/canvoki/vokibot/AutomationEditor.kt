package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    editingId: String? = null,
    triggerPickResult: Pair<String, String>? = null,
    commandPickResult: String? = null,
    onRequestTrigger: () -> Unit,
    onRequestAddCommand: () -> Unit,
    onSave: (Automation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    var name by rememberSaveable { mutableStateOf("") }
    var triggerType by rememberSaveable { mutableStateOf("") }
    var triggerId by rememberSaveable { mutableStateOf("") }
    var commandIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(editingId) {
        if (editingId != null) {
            repository.automation.load(editingId)?.let { existing ->
                name = existing.name
                triggerType = existing.triggerType
                triggerId = existing.triggerId
                commandIds = existing.commandIds
            }
        }
    }

    LaunchedEffect(triggerPickResult) {
        triggerPickResult?.let { (type, id) ->
            triggerType = type
            triggerId = id
        }
    }

    LaunchedEffect(commandPickResult) {
        commandPickResult?.let { id ->
            if (!commandIds.contains(id)) {
                commandIds = commandIds + id
            }
        }
    }

    val triggerDisplayName =
        remember(triggerType, triggerId) {
            if (triggerType == "nfc") repository.nfcTrigger.load(triggerId)?.displayName else null
        }
    val commandDisplayNames =
        remember(commandIds) {
            commandIds.map { id -> repository.command.load(id)?.displayName ?: id }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.automation_name_label)) },
            placeholder = { Text(stringResource(R.string.automation_name_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRequestTrigger),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nfc),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.automation_trigger_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = triggerDisplayName ?: stringResource(R.string.automation_trigger_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color =
                            if (triggerDisplayName != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.automation_commands_label),
            style = MaterialTheme.typography.titleMedium,
        )

        if (commandIds.isEmpty()) {
            Text(
                text = stringResource(R.string.automation_commands_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commandDisplayNames.forEachIndexed { index, cmdName ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${index + 1}. $cmdName",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = {
                                commandIds = commandIds.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = stringResource(R.string.automation_remove_command_desc),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onRequestAddCommand,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.automation_add_command))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && triggerId.isNotBlank() && commandIds.isNotEmpty()) {
                    val automation = Automation(name.trim(), triggerType, triggerId, commandIds)
                    repository.automation.save(automation)
                    onSave(automation)
                }
            },
            enabled = name.isNotBlank() && triggerId.isNotBlank() && commandIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.automation_save))
        }
    }
}

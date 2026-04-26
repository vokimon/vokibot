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
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.spike.StackNavigatorState
import net.canvoki.shared.component.spike.StackedScreen

@Serializable
data class AutomationEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun render(
        nav: StackNavigatorState,
    ) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }

        var name by rememberSaveable { mutableStateOf("") }
        var triggerType by rememberSaveable { mutableStateOf("") }
        var triggerId by rememberSaveable { mutableStateOf("") }
        var commandIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
        var isDirty by rememberSaveable { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }
        var lastLoadedId by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(editingId) {
            if (editingId == lastLoadedId) return@LaunchedEffect

            name = ""
            triggerType = ""
            triggerId = ""
            commandIds = emptyList()
            isDirty = false

            if (editingId != null) {
                repository.automation.load(editingId)?.let { existing ->
                    name = existing.name
                    triggerType = existing.triggerType
                    triggerId = existing.triggerId
                    commandIds = existing.commandIds
                }
            }
            lastLoadedId = editingId
        }

        LaunchedEffect(isDirty) {
            nav.onBack(this@AutomationEditor, enabled = isDirty) {
                showDiscardDialog = true
            }
        }

        ConfirmDialog(
            show = showDiscardDialog,
            title = stringResource(R.string.automation_discard_title),
            text = stringResource(R.string.automation_discard_message),
            confirmText = stringResource(R.string.automation_discard_confirm),
            dismissText = stringResource(R.string.automation_discard_cancel),
            onConfirm = {
                isDirty = false
                showDiscardDialog = false
                nav.back()
            },
            onDismiss = {
                showDiscardDialog = false
            },
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextButton(
                onClick = {
                    val automation = Automation(name.trim(), triggerType, triggerId, commandIds)
                    repository.automation.save(automation)
                    isDirty = false
                    nav.back()
                },
                enabled = triggerId.isNotBlank() && commandIds.isNotEmpty(),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.automation_done))
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isDirty = true
                },
                label = { Text(stringResource(R.string.automation_name_label)) },
                placeholder = { Text(stringResource(R.string.automation_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Text(stringResource(R.string.automation_trigger_label), style = MaterialTheme.typography.titleMedium)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier =
                    Modifier.fillMaxWidth().clickable(onClick = {
                        nav.push(TriggerList) { result: Pair<String, String>? ->
                            result?.let { (type, id) ->
                                triggerType = type
                                triggerId = id
                                isDirty = true
                            }
                        }
                    }),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_flash_on),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        val triggerName =
                            remember(triggerId) {
                                if (triggerType == "nfc" && triggerId.isNotBlank()) {
                                    repository.nfcTrigger.load(triggerId)?.displayName
                                } else {
                                    null
                                }
                            }
                        Text(
                            text = triggerName ?: stringResource(R.string.automation_trigger_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color =
                                if (triggerId.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        )
                        if (triggerId.isBlank()) {
                            Text(
                                stringResource(R.string.automation_trigger_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.automation_commands_label), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    nav.push(CommandList) { result: String? ->
                        result?.let { id ->
                            if (!commandIds.contains(id)) {
                                commandIds = commandIds + id
                                isDirty = true
                            }
                        }
                    }
                }) {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.automation_add_command_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            if (commandIds.isEmpty()) {
                Text(
                    stringResource(R.string.automation_commands_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val commandNames =
                        remember(commandIds) {
                            commandIds.map { id -> repository.command.load(id)?.displayName ?: id }
                        }
                    commandNames.forEachIndexed { index, cmdName ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(cmdName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    commandIds = commandIds.filterIndexed { i, _ -> i != index }
                                    isDirty = true
                                }) {
                                    Icon(
                                        painterResource(R.drawable.ic_delete),
                                        contentDescription = stringResource(R.string.automation_remove_command_desc),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

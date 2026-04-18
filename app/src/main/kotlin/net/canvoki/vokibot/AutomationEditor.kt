package net.canvoki.vokibot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@SerialName("automation")
data class Automation(
    val name: String,
    val triggerType: String,
    val triggerId: String,
    val commandIds: List<String>,
) : StorableEntity {
    override val id: String
        get() = name.replace(Regex("[^a-zA-Z0-9_.-]"), "_").take(64).ifBlank { "automation" }
    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        private val json = Json {
            explicitNulls = false
            encodeDefaults = true
            classDiscriminator = "type"
        }
        fun fromJson(jsonString: String): Automation = json.decodeFromString(serializer(), jsonString)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationEditor(
    triggerSelection: Pair<String, String>? = null,
    commandSelections: List<String> = emptyList(),
    onRequestTrigger: () -> Unit,
    onRequestAddCommand: () -> Unit,
    onRemoveCommand: (Int) -> Unit,
    onSave: (Automation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    // Local form state (survives rotation)
    var name by rememberSaveable { mutableStateOf("") }
    var triggerType by rememberSaveable { mutableStateOf("") }
    var triggerId by rememberSaveable { mutableStateOf("") }

    // Sync external trigger selection into local state
    LaunchedEffect(triggerSelection) {
        triggerSelection?.let { (type, id) ->
            triggerType = type
            triggerId = id
        }
    }

    // Resolve display names
    val triggerDisplayName = remember(triggerType, triggerId) {
        if (triggerType == "nfc") repository.nfcTrigger.load(triggerId)?.displayName else null
    }
    val commandDisplayNames = remember(commandSelections) {
        commandSelections.map { id -> repository.command.load(id)?.displayName ?: id }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Name ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.automation_name_label)) },
            placeholder = { Text(stringResource(R.string.automation_name_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        // ── Trigger ───────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRequestTrigger),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nfc),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.automation_trigger_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = triggerDisplayName ?: stringResource(R.string.automation_trigger_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (triggerDisplayName != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Commands ──────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.automation_commands_label),
            style = MaterialTheme.typography.titleMedium
        )

        if (commandSelections.isEmpty()) {
            Text(
                text = stringResource(R.string.automation_commands_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commandDisplayNames.forEachIndexed { index, cmdName ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. $cmdName",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveCommand(index) }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = stringResource(R.string.automation_remove_command_desc),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onRequestAddCommand,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.automation_add_command))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Save ──────────────────────────────────────────────────────
        Button(
            onClick = {
                if (name.isNotBlank() && triggerId.isNotBlank() && commandSelections.isNotEmpty()) {
                    onSave(Automation(name.trim(), triggerType, triggerId, commandSelections))
                }
            },
            enabled = name.isNotBlank() && triggerId.isNotBlank() && commandSelections.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.automation_save))
        }
    }
}

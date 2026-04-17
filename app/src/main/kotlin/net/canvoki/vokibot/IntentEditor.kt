package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch
import net.canvoki.shared.usermessage.UserMessage

@Composable
fun ActivityHeader(packageName: String, component: PublicComponent) {
    Row {
        Image(
            painter =
                component.icon?.let {
                    BitmapPainter(it.toBitmap().asImageBitmap())
                } ?: painterResource(R.drawable.ic_brand),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(component.label)
            Text(formatComponentName(packageName, component.name))
        }
    }
}

@Composable
fun IntentActionSelector(
    supportedActions: List<ActionDefinition>,
    onSelected: (ActionDefinition?) -> Unit,
    onCustomChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    var selected by remember { mutableStateOf<ActionDefinition?>(null) }
    var custom by remember { mutableStateOf("") }

    val actionsToShow =
        if (supportedActions.isNotEmpty()) {
            supportedActions
        } else {
            StandardActions.all()
        }

    LaunchedEffect(actionsToShow) {
        if (selected == null) {
            selected = actionsToShow.firstOrNull()
            onSelected(selected)
        }
    }

    Column {
        Text(stringResource(R.string.intent_editor_action_label))

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(12.dp),
        ) {
            Text(
                text = selected?.label ?: stringResource(R.string.intent_editor_custom_or_none),
                modifier = Modifier.weight(1f),
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down),
                contentDescription = null,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actionsToShow.forEach { action ->

                DropdownMenuItem(
                    text = { Text(action.label) },
                    onClick = {
                        selected = action
                        expanded = false
                        onSelected(action)
                    },
                )
            }

            DropdownMenuItem(
                text = { Text(stringResource(R.string.intent_editor_custom_or_none)) },
                onClick = {
                    selected = null
                    expanded = false
                    onSelected(null)
                },
            )
        }

        if (selected == null) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = custom,
                onValueChange = {
                    custom = it
                    onCustomChanged(it)
                },
                label = { Text(stringResource(R.string.intent_editor_action_string_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun IntentEditor(
    packageName: String,
    component: PublicComponent,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FileDataRepository.fromContext(context) }

    val actionsToShow = remember(component.actions) {
        val mapped = component.actions.mapNotNull { actionStr ->
            StandardActions.all().find { it.action == actionStr }
        }
        if (mapped.isNotEmpty()) mapped else StandardActions.all()
    }

    var selectedAction by remember { mutableStateOf<ActionDefinition?>(null) }
    var customAction by remember { mutableStateOf("") }
    var extrasState by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    var showNameDialog by remember { mutableStateOf(false) }
    var proposedName by remember { mutableStateOf(component.label) }
    var showOverwriteDialog by remember { mutableStateOf(false) }

    val commandSavedMsg = stringResource(R.string.intent_editor_command_saved, proposedName)
    val commandOverwrittenMsg = stringResource(R.string.intent_editor_command_overwritten, proposedName)
    val overwriteMsg = stringResource(R.string.intent_editor_overwrite_message, proposedName)

    fun buildCommand(displayName: String): LaunchActivityCommand {
        val actionStr = selectedAction?.action ?: customAction.takeIf { it.isNotBlank() }
        val typedExtras = extrasState.mapValues { (_, v) ->
            when (v) {
                is String -> ExtraValue.StringValue(v)
                is Int -> ExtraValue.IntValue(v)
                is Boolean -> ExtraValue.BooleanValue(v)
                else -> ExtraValue.StringValue(v?.toString() ?: "")
            }
        }
        return LaunchActivityCommand(
            displayName = displayName,
            packageName = packageName,
            className = component.name,
            action = actionStr,
            extras = typedExtras,
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            ActivityHeader(packageName = packageName, component = component)

            Spacer(modifier = Modifier.height(16.dp))

            IntentActionSelector(
                supportedActions = actionsToShow,
                onSelected = { selectedAction = it },
                onCustomChanged = { customAction = it },
            )

            selectedAction?.let { action ->

                Spacer(modifier = Modifier.height(16.dp))

                action.extras.forEach { spec ->

                    when (spec.type) {
                        ExtraType.STRING -> {
                            val value = extrasState[spec.key] as? String ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it)
                                },
                                label = { Text(spec.label) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ExtraType.INT -> {
                            val value = (extrasState[spec.key] as? Int)?.toString() ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it.toIntOrNull())
                                },
                                label = { Text(spec.label) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ExtraType.BOOLEAN -> {
                            val value = extrasState[spec.key] as? Boolean ?: false

                            Row {
                                Text(spec.label, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = value,
                                    onCheckedChange = {
                                        extrasState = extrasState + (spec.key to it)
                                    },
                                )
                            }
                        }

                        ExtraType.STRING_ARRAY -> {
                            val value = extrasState[spec.key] as? String ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it)
                                },
                                label = { Text("${spec.label} (comma separated)") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ExtraType.URI,
                        ExtraType.URI_LIST,
                        -> {
                            val value = extrasState[spec.key] as? String ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it)
                                },
                                label = { Text(spec.label) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = {
                    val command = buildCommand(component.label)
                    scope.launch {
                        try {
                            command.execute(context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.intent_editor_try))
            }
            Button(
                onClick = {
                    showNameDialog = true
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.intent_editor_save))
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text(stringResource(R.string.intent_editor_save_command_title)) },
            text = {
                OutlinedTextField(
                    value = proposedName,
                    onValueChange = { proposedName = it },
                    label = { Text(stringResource(R.string.intent_editor_command_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val command = buildCommand(proposedName)
                        if (repository.existsCommand(proposedName)) {
                            showOverwriteDialog = true
                        } else {
                            repository.saveCommand(proposedName, command)
                            UserMessage.Info(commandSavedMsg).post()
                        }
                        showNameDialog = false
                    },
                ) {
                    Text(stringResource(R.string.intent_editor_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(stringResource(R.string.intent_editor_cancel))
                }
            },
        )
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text(stringResource(R.string.intent_editor_overwrite_title)) },
            text = { Text(overwriteMsg) },
            confirmButton = {
                Button(
                    onClick = {
                        repository.saveCommand(proposedName, buildCommand(proposedName))
                        UserMessage.Info(commandOverwrittenMsg).post()
                        showOverwriteDialog = false
                    },
                ) {
                    Text(stringResource(R.string.intent_editor_replace))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteDialog = false }) {
                    Text(stringResource(R.string.intent_editor_cancel))
                }
            },
        )
    }
}

private fun formatComponentName(packageName: String, fullName: String): String {
    val prefix = "$packageName."
    return if (fullName.startsWith(prefix)) fullName.substring(packageName.length) else fullName
}

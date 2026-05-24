package net.canvoki.vokibot

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.shared.usermessage.UserMessage

@Composable
fun ActivityHeader(
    packageName: String,
    component: PublicComponent,
) {
    Row {
        Image(
            painter = drawableToPainter(component.icon),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column {
            Text(component.label)
            Text(formatComponentName(packageName, component.name))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.tertiary,
        style = MaterialTheme.typography.titleSmall,
    )
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
        SectionHeader(stringResource(R.string.intent_editor_action_label))

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

@Serializable
data class IntentEditor(
    val packageName: String,
    val componentName: String,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        var currentComponent by remember { mutableStateOf<PublicComponent?>(null) }
        val context = LocalContext.current

        LaunchedEffect(packageName, componentName) {
            if (currentComponent == null) {
                currentComponent =
                    queryPublicComponents(context, packageName, exportedOnly = true)
                        .components
                        .find { it.name == componentName }
            }
        }

        currentComponent?.let { component ->
            IntentEditor(
                packageName = packageName,
                component = component,
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

    val actionsToShow =
        remember(component.actions) {
            val mapped =
                component.actions.mapNotNull { actionStr ->
                    StandardActions.all().find { it.action == actionStr }
                }
            if (mapped.isNotEmpty()) mapped else StandardActions.all()
        }

    var selectedAction by remember { mutableStateOf<ActionDefinition?>(null) }
    var customAction by remember { mutableStateOf("") }
    var extrasState by rememberSaveable(
        stateSaver = ExtraValueMapSaver,
    ) { mutableStateOf(emptyMap<String, ExtraValue>()) }
    var showNameDialog by remember { mutableStateOf(false) }
    var proposedName by remember { mutableStateOf(component.label) }
    var pendingCommand by remember { mutableStateOf<LaunchActivityCommand?>(null) }
    var showOverwriteDialog by remember { mutableStateOf(false) }
    var confirmName by remember { mutableStateOf<String?>(null) }
    var customExtraSpecs by rememberSaveable(stateSaver = ExtraSpecListSaver) { mutableStateOf(listOf<ExtraSpec>()) }

    val allSpecs = (selectedAction?.extras ?: emptyList()) + customExtraSpecs

    val confirmMsg =
        confirmName?.let {
            stringResource(R.string.intent_editor_command_saved, it)
        }

    val overwriteMsg =
        pendingCommand?.let {
            stringResource(R.string.intent_editor_command_overwritten, it.displayName)
        }

    LaunchedEffect(confirmMsg) {
        confirmMsg?.let {
            UserMessage.Info(it).post()
            confirmName = null
        }
    }

    LaunchedEffect(overwriteMsg) {
        overwriteMsg?.let {
            UserMessage.Info(it).post()
            showOverwriteDialog = false
            pendingCommand = null
        }
    }

    LaunchedEffect(selectedAction) {
        val actionExtras = selectedAction?.extras ?: emptyList()
        customExtraSpecs = computeNewCustomSpecs(extrasState, actionExtras)
        extrasState = rebuildExtras(extrasState, actionExtras, customExtraSpecs)
    }

    fun buildCommand(displayName: String): LaunchActivityCommand {
        val actionStr = selectedAction?.action ?: customAction.takeIf { it.isNotBlank() }
        return LaunchActivityCommand(
            displayName = displayName,
            packageName = packageName,
            className = component.name,
            action = actionStr,
            extras = extrasState,
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

            Spacer(modifier = Modifier.height(16.dp))
            ExtrasEditor(
                specs = allSpecs,
                extras = extrasState,
                onExtraChanged = { key, value -> extrasState = extrasState + (key to value) },
                onAddExtra = { spec ->
                    customExtraSpecs = customExtraSpecs + spec
                    extrasState = extrasState + (spec.key to spec.defaultValue())
                },
            )
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

    InputDialog(
        show = showNameDialog,
        title = stringResource(R.string.intent_editor_save_command_title),
        label = stringResource(R.string.intent_editor_command_name_label),
        value = proposedName,
        confirmText = stringResource(R.string.intent_editor_save),
        dismissText = stringResource(R.string.intent_editor_cancel),
        onDismiss = { showNameDialog = false },
        onConfirm = { name ->
            val command = buildCommand(name)
            if (repository.existsCommand(command.id)) {
                pendingCommand = command
                showOverwriteDialog = true
            } else {
                repository.saveCommand(command)
                confirmName = name
            }
            showNameDialog = false
        },
    )

    ConfirmDialog(
        show = showOverwriteDialog,
        title = stringResource(R.string.intent_editor_overwrite_title),
        text = stringResource(R.string.intent_editor_overwrite_message, pendingCommand?.displayName ?: ""),
        confirmText = stringResource(R.string.intent_editor_replace),
        dismissText = stringResource(R.string.intent_editor_cancel),
        onConfirm = {
            pendingCommand?.let { repository.saveCommand(it) }
        },
        onDismiss = {
            showOverwriteDialog = false
            pendingCommand = null
        },
    )
}

private fun formatComponentName(
    packageName: String,
    fullName: String,
): String {
    val prefix = "$packageName."
    return if (fullName.startsWith(prefix)) fullName.substring(packageName.length) else fullName
}

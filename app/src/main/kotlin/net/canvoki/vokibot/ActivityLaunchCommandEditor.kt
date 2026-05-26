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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

@Serializable
data class ActivityLaunchCommandEditor(
    val commandId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        var isDirty by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }
        var packageName by remember { mutableStateOf<String?>(null) }
        var componentName by remember { mutableStateOf<String?>(null) }
        var selectedAction by remember { mutableStateOf<ActionDefinition?>(null) }
        var customAction by remember { mutableStateOf("") }
        var currentComponent by remember { mutableStateOf<PublicComponent?>(null) }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val repository = remember { FileDataRepository.fromContext(context) }
        var extrasState by rememberSaveable(stateSaver = ExtraValueMapSaver) {
            mutableStateOf(emptyMap<String, ExtraValue>())
        }
        var customExtraSpecs by rememberSaveable(stateSaver = ExtraSpecListSaver) {
            mutableStateOf(listOf<ExtraSpec>())
        }
        var intentData by remember { mutableStateOf<String?>(null) }
        var intentMime by remember { mutableStateOf<String?>(null) }

        val allSpecs = (selectedAction?.extras ?: emptyList()) + customExtraSpecs

        LaunchedEffect(commandId) {
            if (commandId != null) {
                val saved = repository.loadCommand(commandId) as? LaunchActivityCommand
                saved?.let {
                    packageName = it.packageName
                    componentName = it.className
                    intentData = it.dataUri
                    intentMime = it.dataMimeType
                    extrasState = it.extras
                    it.action?.let { action ->
                        StandardActions
                            .all()
                            .find { a -> a.action == action }
                            ?.let { selectedAction = it }
                            ?: run { customAction = action }
                    }
                }
            }
        }

        LaunchedEffect(packageName, componentName) {
            if (packageName != null && componentName != null) {
                currentComponent =
                    queryPublicComponents(context, packageName!!)
                        .components
                        .find { it.name == componentName }
            }
        }

        val actionsToShow =
            remember(currentComponent?.actions) {
                val mapped =
                    currentComponent?.let {
                        it.actions.mapNotNull { actionStr ->
                            StandardActions.all().find { it.action == actionStr }
                        }
                    } ?: emptyList<ActionDefinition>()

                if (mapped.isNotEmpty()) mapped else StandardActions.all()
            }

        LaunchedEffect(isDirty) {
            nav.onBack(this@ActivityLaunchCommandEditor, enabled = isDirty) {
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
                nav.pop()
            },
            onDismiss = {
                showDiscardDialog = false
            },
        )

        LaunchedEffect(selectedAction) {
            val actionExtras = selectedAction?.extras ?: emptyList()
            customExtraSpecs = computeNewCustomSpecs(extrasState, actionExtras)
            extrasState = rebuildExtras(extrasState, actionExtras, customExtraSpecs)
        }

        fun buildCommand(component: PublicComponent): LaunchActivityCommand {
            val actionStr = selectedAction?.action ?: customAction.takeIf { it.isNotBlank() }
            return LaunchActivityCommand(
                id = ApplicationCommand.resolveId(commandId),
                displayName = component.label,
                packageName = packageName!!,
                className = component.name,
                action = actionStr,
                dataUri = intentData,
                dataMimeType = intentMime,
                extras = extrasState,
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            EditorHeader(
                icon = painterResource(LaunchActivityCommand.iconRes),
                title = stringResource(LaunchActivityCommand.labelRes),
                actionText = stringResource(R.string.automation_done),
                action = {
                    currentComponent?.let {
                        val command = buildCommand(it)
                        repository.saveCommand(command)
                        nav.pop()
                    }
                },
            )

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val component = currentComponent
                if (component != null) {
                    ActivityHeader(
                        packageName = packageName!!,
                        component = component,
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier =
                            Modifier.fillMaxWidth().clickable {
                                nav.push(AppSelector) { selection ->
                                    selection?.let {
                                        isDirty = true
                                        packageName = it.packageName
                                        componentName = it.componentName
                                    }
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.intent_editor_select_app_and_screen),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                IntentActionSelector(
                    supportedActions = actionsToShow,
                    selectedAction = selectedAction,
                    onSelected = { action ->
                        if (action != selectedAction) {
                            isDirty = true
                        }
                        selectedAction = action
                    },
                    onCustomChanged = {
                        customAction = it
                        isDirty = true
                    },
                )
                val dataUriRequired = selectedAction?.probeStrategy == ProbeStrategy.REQUIRES_URI
                IntentDataEditor(
                    dataUri = intentData,
                    mimeType = intentMime,
                    onDataChanged = {
                        intentData = it
                        isDirty = true
                    },
                    onMimeChanged = {
                        intentMime = it
                        isDirty = true
                    },
                    dataUriRequired = dataUriRequired,
                    allowedSchemes = selectedAction?.allowedSchemes,
                )
                ExtrasEditor(
                    specs = allSpecs,
                    extras = extrasState,
                    onExtraChanged = { key, value ->
                        extrasState = extrasState + (key to value)
                        isDirty = true
                    },
                    onAddExtra = { spec ->
                        isDirty = true
                        customExtraSpecs = customExtraSpecs + spec
                        extrasState = extrasState + (spec.key to spec.defaultValue())
                    },
                )
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            component?.let {
                                val command = buildCommand(it)
                                try {
                                    command.execute(context)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    enabled = component != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.intent_editor_try))
                }
            }
        }
    }
}

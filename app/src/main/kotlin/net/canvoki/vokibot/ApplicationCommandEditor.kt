package net.canvoki.vokibot

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import net.canvoki.shared.usermessage.UserMessage
import net.canvoki.vokibot.common.EditorHeader

private fun formatComponentName(
    packageName: String,
    fullName: String,
): String {
    val prefix = "$packageName."
    return if (fullName.startsWith(prefix)) fullName.substring(packageName.length) else fullName
}

@Composable
private fun ComponentSelector(
    packageName: String?,
    component: PublicComponent?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth().clickable(onClick = onSelect),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (component != null) {
                Image(
                    painter = drawableToPainter(component.icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = component.label,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        formatComponentName(packageName!!, component.name),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
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
}

@Serializable
data class ApplicationCommandEditor(
    val commandId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        var isDirty by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }
        var packageName by remember { mutableStateOf<String?>(null) }
        var componentName by remember { mutableStateOf<String?>(null) }
        var actionStr by remember { mutableStateOf<String?>(null) }
        var componentType by remember { mutableStateOf(ComponentType.ACTIVITY) }
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

        val allSpecs = (StandardActions.get(actionStr)?.extras ?: emptyList()) + customExtraSpecs
        val runErrorFallback = stringResource(R.string.command_run_error_fallback)

        LaunchedEffect(commandId) {
            if (commandId != null) {
                when (val saved = repository.loadCommand(commandId)) {
                    is LaunchActivityCommand -> {
                        componentType = ComponentType.ACTIVITY
                        packageName = saved.packageName
                        componentName = saved.className
                        actionStr = saved.action
                        intentData = saved.dataUri
                        intentMime = saved.dataMimeType
                        extrasState = saved.extras
                    }
                    is SendBroadcastCommand -> {
                        componentType = ComponentType.RECEIVER
                        packageName = saved.packageName
                        componentName = saved.className
                        actionStr = saved.action
                        intentData = saved.dataUri
                        extrasState = saved.extras
                    }
                    is StartServiceCommand -> {
                        componentType = ComponentType.SERVICE
                        packageName = saved.packageName
                        componentName = saved.className
                        actionStr = saved.action
                        extrasState = saved.extras
                    }
                    is AccessProviderCommand -> {
                        componentType = ComponentType.PROVIDER
                        packageName = saved.packageName
                        intentData = saved.path
                        intentMime = saved.mimeType
                        extrasState = saved.extras
                    }
                    else -> {}
                }
            }
        }

        LaunchedEffect(packageName, componentName) {
            if (packageName != null && componentName != null) {
                currentComponent =
                    queryPublicComponents(context, packageName!!)
                        .components
                        .find { it.name == componentName }
                componentType = currentComponent?.type ?: ComponentType.ACTIVITY
            }
        }

        val actionsToShow =
            remember(currentComponent?.actions) {
                val mapped =
                    currentComponent?.let {
                        it.actions.mapNotNull { actionStr ->
                            StandardActions.all().find { a -> a.action == actionStr }
                        }
                    } ?: emptyList<ActionDefinition>()

                if (mapped.isNotEmpty()) mapped else StandardActions.all()
            }

        LaunchedEffect(currentComponent) {
            if (currentComponent != null && commandId == null && actionStr == null) {
                actionStr = actionsToShow.firstOrNull()?.action
            }
        }

        LaunchedEffect(isDirty) {
            nav.onBack(this@ApplicationCommandEditor, enabled = isDirty) {
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

        LaunchedEffect(actionStr) {
            val actionDef = StandardActions.get(actionStr)
            val actionExtras = actionDef?.extras ?: emptyList()
            customExtraSpecs = computeNewCustomSpecs(extrasState, actionExtras)
            extrasState = rebuildExtras(extrasState, actionExtras, customExtraSpecs)
        }

        fun buildCommand(component: PublicComponent): ApplicationCommand =
            when (componentType) {
                ComponentType.ACTIVITY ->
                    LaunchActivityCommand(
                        id = ApplicationCommand.resolveId(commandId),
                        displayName = component.label,
                        packageName = packageName!!,
                        className = component.name,
                        action = actionStr,
                        dataUri = intentData,
                        dataMimeType = intentMime,
                        extras = extrasState,
                    )
                ComponentType.RECEIVER ->
                    SendBroadcastCommand(
                        id = ApplicationCommand.resolveId(commandId),
                        displayName = component.label,
                        packageName = packageName!!,
                        className = component.name,
                        action = actionStr ?: "",
                        dataUri = intentData,
                        extras = extrasState,
                    )
                ComponentType.SERVICE ->
                    StartServiceCommand(
                        id = ApplicationCommand.resolveId(commandId),
                        displayName = component.label,
                        packageName = packageName!!,
                        className = component.name,
                        action = actionStr,
                        extras = extrasState,
                    )
                ComponentType.PROVIDER ->
                    AccessProviderCommand(
                        id = ApplicationCommand.resolveId(commandId),
                        displayName = component.label,
                        packageName = packageName!!,
                        authority = component.authorities.firstOrNull() ?: "",
                        operation = ProviderOperation.QUERY,
                        path = intentData,
                        mimeType = intentMime,
                        extras = extrasState,
                    )
            }

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val commandTypeMeta =
                when (componentType) {
                    ComponentType.ACTIVITY -> LaunchActivityCommand
                    ComponentType.RECEIVER -> SendBroadcastCommand
                    ComponentType.SERVICE -> StartServiceCommand
                    ComponentType.PROVIDER -> LaunchActivityCommand
                }
            EditorHeader(
                icon = painterResource(commandTypeMeta.iconRes),
                title = stringResource(commandTypeMeta.labelRes),
                actionText = stringResource(R.string.automation_done),
                action = {
                    currentComponent?.let {
                        val command = buildCommand(it)
                        repository.saveCommand(command)
                        nav.pop()
                    }
                },
            )

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (currentComponent?.type == ComponentType.PROVIDER) {
                    Text(stringResource(R.string.not_yet_implemented))
                } else {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ComponentSelector(
                            packageName = packageName,
                            component = currentComponent,
                            onSelect = {
                                nav.push(AppList) { selection ->
                                    selection?.let {
                                        isDirty = true
                                        packageName = it.packageName
                                        componentName = it.componentName
                                    }
                                }
                            },
                        )
                        IntentActionSelector(
                            supportedActions = actionsToShow,
                            action = actionStr,
                            onActionChanged = {
                                actionStr = it
                                isDirty = true
                            },
                        )
                        if (componentType != ComponentType.SERVICE) {
                            val dataUriRequired =
                                StandardActions.get(actionStr)?.probeStrategy == ProbeStrategy.REQUIRES_URI
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
                                allowedSchemes = StandardActions.get(actionStr)?.allowedSchemes,
                                showMime = componentType == ComponentType.ACTIVITY,
                            )
                        }
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
                                    currentComponent?.let {
                                        val command = buildCommand(it)
                                        try {
                                            command.execute(context)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            UserMessage.Info(e.message ?: runErrorFallback).post()
                                        }
                                    }
                                }
                            },
                            enabled = currentComponent != null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_play_arrow),
                                contentDescription = null,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.intent_editor_try))
                        }
                    }
                }
            }
        }
    }
}

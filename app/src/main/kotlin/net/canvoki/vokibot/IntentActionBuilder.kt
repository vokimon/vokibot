package net.canvoki.vokibot

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigator
import net.canvoki.shared.component.rememberStackNavigatorState

@Serializable
sealed class BuilderScreen {
    @Serializable
    data object AutomationEditor : BuilderScreen()

    @Serializable
    data object TriggerList : BuilderScreen()

    @Serializable
    data object NfcTriggerEditor: BuilderScreen()

    @Serializable
    data object CommandList : BuilderScreen()

    @Serializable
    data object AppList : BuilderScreen()

    @Serializable
    data class ComponentList(
        val packageName: String,
    ) : BuilderScreen()

    @Serializable
    data class IntentEditor(
        val packageName: String,
        val componentName: String,
    ) : BuilderScreen()
}

@Composable
fun IntentActionBuilder() {
    val nav =
        rememberStackNavigatorState<BuilderScreen>(
            BuilderScreen.AutomationEditor,
        )
    val appListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var currentComponent by remember { mutableStateOf<PublicComponent?>(null) }
    var selectedTrigger by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedCommands by remember { mutableStateOf<List<String>>(emptyList()) }

    StackNavigator(state = nav) { screen ->

        when (screen) {
            is BuilderScreen.AutomationEditor -> AutomationEditor(
                triggerSelection = selectedTrigger,
                commandSelections = selectedCommands,
                onRequestTrigger = { nav.push(BuilderScreen.TriggerList) },
                onRequestAddCommand = { nav.push(BuilderScreen.CommandList) },
                onRemoveCommand = { index ->
                    selectedCommands = selectedCommands.toMutableList().apply { removeAt(index) }
                },
                onSave = { automation -> nav.back() }
            )

            is BuilderScreen.TriggerList -> TriggerList(
                onNewTrigger = { typeTag ->
                    when (typeTag) {
                        "nfc" -> nav.push(BuilderScreen.NfcTriggerEditor)
                        else -> {}
                    }
                },
                onTriggerSelected = { type, id ->
                    selectedTrigger = type to id
                    nav.back()
                },
            )

            is BuilderScreen.NfcTriggerEditor -> NfcTriggerEditor(
                onSaved = { nav.back() },
            )

            is BuilderScreen.CommandList -> CommandList(
                onLaunchAppSelected = { nav.push(BuilderScreen.AppList) },
                onCommandSelected = {
                    selectedCommands = selectedCommands + it
                    nav.back()
                }
            )

            is BuilderScreen.AppList -> {
                AppList(
                    listState = appListState,
                    onSelected = { app ->
                        nav.push(
                            BuilderScreen.ComponentList(app.packageName),
                        )
                    },
                )
            }

            is BuilderScreen.ComponentList -> {
                AppComponentList(
                    packageName = screen.packageName,
                    onSelected = { component ->
                        currentComponent = component
                        nav.push(
                            BuilderScreen.IntentEditor(
                                packageName = screen.packageName,
                                componentName = component.name,
                            ),
                        )
                    },
                )
            }

            is BuilderScreen.IntentEditor -> {
                val context = LocalContext.current
                if (currentComponent == null) {
                    LaunchedEffect(screen.packageName, screen.componentName) {
                        // Double-check inside coroutine in case state changed during launch
                        if (currentComponent == null) {
                            currentComponent = queryPublicComponents(
                                context,
                                screen.packageName,
                                exportedOnly = true
                            ).components.find { it.name == screen.componentName }
                        }
                    }
                }
                currentComponent?.let {
                    IntentEditor(
                        packageName = screen.packageName,
                        component = it,
                    )
                }
            }
        }
    }
}

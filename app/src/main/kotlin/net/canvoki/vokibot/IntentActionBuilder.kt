package net.canvoki.vokibot

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigator
import net.canvoki.shared.component.rememberStackNavigatorState

@Serializable
sealed class BuilderScreen {
    @Serializable data object AutomationList : BuilderScreen()

    @Serializable data class AutomationEditor(
        val editingId: String? = null,
    ) : BuilderScreen()

    @Serializable data object TriggerList : BuilderScreen()

    @Serializable data object NfcTriggerEditor : BuilderScreen()

    @Serializable data object CommandList : BuilderScreen()

    @Serializable data object AppList : BuilderScreen()

    @Serializable data class ComponentList(
        val packageName: String,
    ) : BuilderScreen()

    @Serializable data class IntentEditor(
        val packageName: String,
        val componentName: String,
    ) : BuilderScreen()
}

@Composable
fun IntentActionBuilder(initialStack: List<BuilderScreen> = listOf(BuilderScreen.AutomationList)) {
    if (initialStack.isEmpty()) return

    val nav = rememberStackNavigatorState<BuilderScreen>(initialStack.first())
    val appListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var currentComponent by remember { mutableStateOf<PublicComponent?>(null) }
    var triggerResult by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }
    var commandResult by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        initialStack.drop(1).forEach { screen ->
            nav.push(screen)
        }
    }

    StackNavigator(state = nav) { screen ->
        when (screen) {
            is BuilderScreen.AutomationList ->
                AutomationList(
                    onNewAutomation = {
                        nav.push(BuilderScreen.AutomationEditor(editingId = null))
                    },
                    onAutomationSelected = { id ->
                        nav.push(BuilderScreen.AutomationEditor(editingId = id))
                    },
                )

            is BuilderScreen.AutomationEditor ->
                AutomationEditor(
                    editingId = screen.editingId,
                    triggerPickResult = triggerResult,
                    commandPickResult = commandResult,
                    onTriggerConsumed = { triggerResult = null },
                    onCommandConsumed = { commandResult = null },
                    onRequestTrigger = { nav.push(BuilderScreen.TriggerList) },
                    onRequestAddCommand = { nav.push(BuilderScreen.CommandList) },
                    onSave = { nav.back() },
                    onBack = { nav.back() },
                )

            is BuilderScreen.TriggerList ->
                TriggerList(
                    onNewTrigger = { typeTag ->
                        if (typeTag == "nfc") nav.push(BuilderScreen.NfcTriggerEditor)
                    },
                    onTriggerSelected = { type, id ->
                        triggerResult = type to id
                        nav.back()
                    },
                )

            is BuilderScreen.NfcTriggerEditor ->
                NfcTriggerEditor(
                    onSaved = { nav.back() },
                )

            is BuilderScreen.CommandList ->
                CommandList(
                    onLaunchAppSelected = { nav.push(BuilderScreen.AppList) },
                    onCommandSelected = {
                        commandResult = it
                        nav.back()
                    },
                )

            is BuilderScreen.AppList ->
                AppList(
                    listState = appListState,
                    onSelected = { app -> nav.push(BuilderScreen.ComponentList(app.packageName)) },
                )

            is BuilderScreen.ComponentList ->
                AppComponentList(
                    packageName = screen.packageName,
                    onSelected = { component ->
                        currentComponent = component
                        nav.push(BuilderScreen.IntentEditor(screen.packageName, component.name))
                    },
                )

            is BuilderScreen.IntentEditor -> {
                val context = LocalContext.current
                if (currentComponent == null) {
                    LaunchedEffect(screen.packageName, screen.componentName) {
                        if (currentComponent == null) {
                            currentComponent =
                                queryPublicComponents(context, screen.packageName, exportedOnly = true)
                                    .components
                                    .find { it.name == screen.componentName }
                        }
                    }
                }
                currentComponent?.let {
                    IntentEditor(packageName = screen.packageName, component = it)
                }
            }
        }
    }
}

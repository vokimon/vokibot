package net.canvoki.vokibot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.spike.Navigator
import net.canvoki.shared.component.spike.Screen
import net.canvoki.shared.component.spike.rememberStackNavigatorState
import net.canvoki.shared.component.spike.StackNavigator

/**
 * Application-specific screen definitions.
 *
 * Sealed for exhaustiveness in `when` expressions.
 * Extends the library [Screen] base class to enable cross-module navigation.
 *
 * @param R The result type returned when this screen completes.
 */
@Serializable
sealed class BuilderScreen<R> : Screen<R>() {
    @Serializable data object AutomationList : BuilderScreen<Unit>()
    @Serializable data class AutomationEditor(val editingId: String? = null) : BuilderScreen<Unit>()
    @Serializable data object TriggerList : BuilderScreen<Pair<String, String>>()
    @Serializable data object NfcTriggerEditor : BuilderScreen<Unit>()
    @Serializable data object CommandList : BuilderScreen<String>()
    @Serializable data object AppList : BuilderScreen<Unit>()
    @Serializable data class AppComponentList(val packageName: String) : BuilderScreen<Unit>()
    @Serializable data class IntentEditor(val packageName: String, val componentName: String) : BuilderScreen<Unit>()
}

typealias ScreenNavigator = Navigator<BuilderScreen<*>>

@Composable
fun IntentActionBuilder(
    initialStack: List<BuilderScreen<*>> = listOf(BuilderScreen.AutomationList)
) {
    if (initialStack.isEmpty()) return

    val navState = rememberStackNavigatorState<BuilderScreen<*>>(initialStack.first())

    LaunchedEffect(Unit) {
        initialStack.drop(1).forEach { screen ->
            navState.push(screen)
        }
    }

    @Suppress("UNCHECKED_CAST")
    StackNavigator(state = navState) { screen, nav ->
        when (screen) {
            is BuilderScreen.AutomationList -> AutomationList(nav)
            is BuilderScreen.AutomationEditor -> AutomationEditor(nav, screen.editingId)
            is BuilderScreen.TriggerList -> TriggerList(nav)
            is BuilderScreen.NfcTriggerEditor -> NfcTriggerEditor(nav)
            is BuilderScreen.CommandList -> CommandList(nav)
            is BuilderScreen.AppList -> AppList(nav)
            is BuilderScreen.AppComponentList -> AppComponentList(nav, screen.packageName)
            is BuilderScreen.IntentEditor -> IntentEditor(nav, screen.packageName, screen.componentName)
        }
    }
}

package net.canvoki.vokibot

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import net.canvoki.shared.component.StackNavigator
import net.canvoki.shared.component.rememberStackNavigatorState

sealed class BuilderScreen {
    data object AppList : BuilderScreen()

    data class ComponentList(
        val packageName: String,
    ) : BuilderScreen()

    data class IntentEditor(
        val packageName: String,
        val componentName: String,
        val component: PublicComponent,
    ) : BuilderScreen()
}

@Composable
fun IntentActionBuilder() {
    val nav =
        rememberStackNavigatorState<BuilderScreen>(
            BuilderScreen.AppList,
        )
    val appListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    StackNavigator(state = nav) { screen ->

        when (screen) {
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
                        nav.push(
                            BuilderScreen.IntentEditor(
                                packageName = screen.packageName,
                                componentName = component.name,
                                component = component,
                            ),
                        )
                    },
                )
            }

            is BuilderScreen.IntentEditor -> {
                IntentEditor(
                    packageName = screen.packageName,
                    component = screen.component,
                )
            }
        }
    }
}

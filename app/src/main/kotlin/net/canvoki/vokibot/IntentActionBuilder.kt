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
            BuilderScreen.CommandList,
        )
    val appListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var currentComponent by remember { mutableStateOf<PublicComponent?>(null) }

    StackNavigator(state = nav) { screen ->

        when (screen) {
            is BuilderScreen.CommandList -> CommandList(
                onLaunchAppSelected = { nav.push(BuilderScreen.AppList) }
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

package net.canvoki.vokibot

import androidx.compose.runtime.Composable
import net.canvoki.shared.component.StackNavigator
import net.canvoki.shared.component.rememberStackNavigatorState

sealed class BuilderScreen {
    data object AppList : BuilderScreen()

    data class ActivityList(
        val packageName: String,
    ) : BuilderScreen()

    data class IntentEditor(
        val packageName: String,
        val activityName: String,
    ) : BuilderScreen()
}

@Composable
fun IntentActionBuilder() {
    val nav =
        rememberStackNavigatorState<BuilderScreen>(
            BuilderScreen.AppList,
        )

    StackNavigator(state = nav) { screen ->

        when (screen) {
            is BuilderScreen.AppList -> {
                AppList(
                    onSelected = { app ->
                        nav.push(
                            BuilderScreen.ActivityList(app.packageName),
                        )
                    },
                )
            }

            is BuilderScreen.ActivityList -> {
                ActivityList(
                    packageName = screen.packageName,
                    onSelected = { component ->
                        nav.push(
                            BuilderScreen.IntentEditor(
                                packageName = screen.packageName,
                                activityName = component.name,
                            ),
                        )
                    },
                )
            }

            is BuilderScreen.IntentEditor -> {
                IntentEditor(
                    packageName = screen.packageName,
                    activityName = screen.activityName,
                )
            }
        }
    }
}

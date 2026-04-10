package net.canvoki.vokibot

import androidx.compose.runtime.Composable
import net.canvoki.shared.component.StackNavigator
import net.canvoki.shared.component.rememberStackNavigatorState

sealed class BuilderScreen {

    data object AppList : BuilderScreen()

    data class AppIntents(val packageName: String) : BuilderScreen()

    data class ExtrasEditor(
        val packageName: String,
        val activityName: String
    ) : BuilderScreen()
}

@Composable
fun IntentActionBuilder(
    onActionBuilt: (AppIntentItem) -> Unit
) {

    val nav = rememberStackNavigatorState<BuilderScreen>(
        BuilderScreen.AppList
    )

    StackNavigator(state = nav) { screen ->

        when (screen) {

            is BuilderScreen.AppList -> {
                AppList(
                    onSelected = { app ->
                        nav.push(
                            BuilderScreen.AppIntents(app.packageName)
                        )
                    }
                )
            }

            is BuilderScreen.AppIntents -> {
                AppIntentList(
                    packageName = screen.packageName,
                    onSelected = { intent ->
                        nav.push(
                            BuilderScreen.ExtrasEditor(
                                packageName = screen.packageName,
                                activityName = intent.activityName
                            )
                        )
                    }
                )
            }

            is BuilderScreen.ExtrasEditor -> {
                IntentExtrasEditor(
                    packageName = screen.packageName,
                    activityName = screen.activityName,
                    extras = emptyList(),
                    baseIntent = android.content.Intent()
                )
            }
        }
    }
}

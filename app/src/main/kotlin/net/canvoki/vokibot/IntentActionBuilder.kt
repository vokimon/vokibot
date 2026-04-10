package net.canvoki.vokibot

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private sealed class BuilderScreen {

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
    var screen by remember {
        mutableStateOf<BuilderScreen>(BuilderScreen.AppList)
    }
    var isForward by remember {
        mutableStateOf(true)
    }

    BackHandler(enabled = screen != BuilderScreen.AppList) {
        screen = when (screen) {
            is BuilderScreen.ExtrasEditor -> BuilderScreen.AppIntents((screen as BuilderScreen.ExtrasEditor).packageName)
            is BuilderScreen.AppIntents -> BuilderScreen.AppList
            else -> BuilderScreen.AppList
        }
        isForward = false
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {

            val enter = slideInHorizontally {
                if (isForward) it else -it
            } + fadeIn()

            val exit = slideOutHorizontally {
                if (isForward) -it else it
            } + fadeOut()

            enter togetherWith exit
        },
        label = "IntentActionBuilder"
    ) { target ->

        when (target) {

            is BuilderScreen.AppList -> {
                AppList(
                    onSelected = { app ->
                        isForward = true
                        screen = BuilderScreen.AppIntents(app.packageName)
                    }
                )
            }

            is BuilderScreen.AppIntents -> {
                AppIntentList(
                    packageName = target.packageName,
                    onSelected = { intent ->
                            isForward = true
                        screen = BuilderScreen.ExtrasEditor(
                            packageName = target.packageName,
                            activityName = intent.activityName
                        )
                    }
                )
            }

            is BuilderScreen.ExtrasEditor -> {
                IntentExtrasEditor(
                    packageName = target.packageName,
                    activityName = target.activityName,
                    extras = emptyList(),
                    baseIntent = android.content.Intent()
                )
            }
        }
    }
}

package net.canvoki.vokibot

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private sealed class BuilderScreen {
    data object AppList : BuilderScreen()
    data class AppIntents(val packageName: String) : BuilderScreen()
}

@Composable
fun IntentActionBuilder(
    onActionBuilt: (AppIntentItem) -> Unit
) {
    var screen by remember {
        mutableStateOf<BuilderScreen>(BuilderScreen.AppList)
    }

    BackHandler(enabled = screen is BuilderScreen.AppIntents) {
        screen = BuilderScreen.AppList
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
        },
        label = "IntentActionBuilder"
    ) { target ->

        when (target) {

            is BuilderScreen.AppList -> {
                AppList(
                    onSelected = { app ->
                        screen = BuilderScreen.AppIntents(app.packageName)
                    }
                )
            }

            is BuilderScreen.AppIntents -> {
                AppIntentList(
                    packageName = target.packageName,
                    onSelected = { intent ->
                        onActionBuilt(intent)
                    }
                )
            }
        }
    }
}

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

class StackNavigation<T>(
    initial: T
) {

    private var stack by mutableStateOf(listOf(initial))

    private var _isForward by mutableStateOf(true)
    val isForward: Boolean get() = _isForward

    val screen: T get() = stack.last()

    fun navigateTo(screen: T) {
        _isForward = true
        stack = stack + screen
    }

    fun back() {
        if (stack.size > 1) {
            _isForward = false
            stack = stack.dropLast(1)
        }
    }
}

@Composable
fun rememberStackNavigation(
    initial: BuilderScreen
): StackNavigation<BuilderScreen> {
    return remember { StackNavigation(initial) }
}


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
    val nav = rememberStackNavigation(BuilderScreen.AppList)

    BackHandler(enabled = nav.screen != BuilderScreen.AppList) {
        nav.back()
    }

    AnimatedContent(
        targetState = nav.screen,
        transitionSpec = {

            val enter = slideInHorizontally {
                if (nav.isForward) it else -it
            } + fadeIn()

            val exit = slideOutHorizontally {
                if (nav.isForward) -it else it
            } + fadeOut()

            enter togetherWith exit
        },
        label = "IntentActionBuilder"
    ) { target ->

        when (target) {

            is BuilderScreen.AppList -> {
                AppList(
                    onSelected = { app ->
                        nav.navigateTo(BuilderScreen.AppIntents(app.packageName))
                    }
                )
            }

            is BuilderScreen.AppIntents -> {
                AppIntentList(
                    packageName = target.packageName,
                    onSelected = { intent ->
                        nav.navigateTo(
                            screen = BuilderScreen.ExtrasEditor(
                                packageName = target.packageName,
                                activityName = intent.activityName
                            )
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

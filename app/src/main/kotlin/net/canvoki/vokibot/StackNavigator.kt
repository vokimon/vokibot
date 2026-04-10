package net.canvoki.shared.component

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

/**
 * Holds a stack-based navigation state.
 *
 * This class represents a simple navigation model based on a stack of screens.
 * Each new screen is pushed on top of the stack, and back navigation pops it.
 *
 * This is designed to be used together with [StackNavigator] in Jetpack Compose.
 *
 * @param T The type representing a screen in the navigation stack.
 */
class StackNavigatorState<T>(
    initial: T
) {

    private var stack by mutableStateOf(listOf(initial))
    private var _isForward by mutableStateOf(true)

    /**
     * The current (top) screen in the navigation stack.
     */
    val current: T get() = stack.last()

    /**
     * Returns true if there is more than one screen in the stack.
     * Used to determine whether back navigation is allowed.
     */
    val canGoBack: Boolean get() = stack.size > 1

    /**
     * Indicates whether the last navigation action was forward (push)
     * or backward (pop). Used to drive transition direction.
     */
    val isForward: Boolean get() = _isForward

    /**
     * Pushes a new screen onto the navigation stack.
     *
     * @param screen The screen to navigate to.
     */
    fun push(screen: T) {
        _isForward = true
        stack = stack + screen
    }

    /**
     * Pops the current screen from the stack if possible.
     *
     * If the stack contains only one screen, this operation does nothing.
     */
    fun back() {
        if (canGoBack) {
            _isForward = false
            stack = stack.dropLast(1)
        }
    }
}

/**
 * Creates and remembers a [StackNavigatorState] instance.
 *
 * This should be used instead of manually instantiating the state
 * to ensure proper Compose state retention across recompositions.
 *
 * @param initial The initial screen of the navigation stack.
 */
@Composable
fun <T> rememberStackNavigatorState(initial: T): StackNavigatorState<T> {
    return remember { StackNavigatorState(initial) }
}

/**
 * A Compose navigation container based on a stack model.
 *
 * This component handles:
 * - Rendering the current screen
 * - Back navigation via system back button
 * - Animated transitions between screens
 * - Automatic forward/back transition direction handling
 *
 * It is designed as a lightweight alternative to NavHost for
 * simple stack-based navigation flows.
 *
 * @param state The navigation state controlling the stack.
 * @param content Composable content for rendering each screen.
 */
@Composable
fun <T> StackNavigator(
    state: StackNavigatorState<T>,
    content: @Composable (T) -> Unit
) {

    BackHandler(enabled = state.canGoBack) {
        state.back()
    }

    AnimatedContent(
        targetState = state.current,
        transitionSpec = {
            val enter = slideInHorizontally {
                if (state.isForward) it else -it
            } + fadeIn()

            val exit = slideOutHorizontally {
                if (state.isForward) -it else it
            } + fadeOut()

            enter togetherWith exit
        },
        label = "StackNavigator"
    ) { screen ->
        content(screen)
    }
}

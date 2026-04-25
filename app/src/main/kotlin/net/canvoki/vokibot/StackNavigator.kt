package net.canvoki.shared.component.spike

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * Base class for navigation screens.
 *
 * Defined as [open] to allow cross-module inheritance.
 * Client modules should define a [sealed] subclass for exhaustiveness checking.
 *
 * @param R The type of value returned when this screen completes via [Navigator.back].
 *          Use [Unit] for screens that do not return data.
 */
@Serializable
open class Screen<R>

/**
 * Navigation state holding the stack, transient transition flags, and result callbacks.
 *
 * @param T The concrete screen type (must extend [Screen]).
 */
class StackNavigatorState<T : Screen<*>>(initial: T) {
    constructor(fullStack: List<T>) : this(fullStack.last()) { stack = fullStack }

    var stack by mutableStateOf(listOf(initial))
        internal set

    internal var pushed: T? by mutableStateOf(null)
    internal var backed: T? by mutableStateOf(null)
    private val callbacks = mutableMapOf<Any, (Any?) -> Unit>()

    val current: T get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    internal fun <R> registerCallback(screen: T, callback: (R?) -> Unit) {
        callbacks[screen] = { result ->
            @Suppress("UNCHECKED_CAST")
            callback(result as R?)
        }
    }

    internal fun <R> invokeCallback(screen: T, result: R?) {
        callbacks[screen]?.invoke(result)
        callbacks.remove(screen)
    }

    /** Push without result callback (ignore return value). */
    fun push(screen: Screen<*>) {
        if (pushed != null || backed != null) return
        @Suppress("UNCHECKED_CAST")
        pushed = screen as T
    }

    /** Push with typed result callback. */
    fun <R> push(screen: Screen<R>, resultCallback: (R?) -> Unit) {
        if (pushed != null || backed != null) return
        @Suppress("UNCHECKED_CAST")
        registerCallback(screen as T, resultCallback)
        pushed = screen as T
    }

    /** Back without result (returns null to parent). */
    fun back() {
        back<Unit>(null)
    }

    /** Back with typed result. */
    fun <R> back(result: R?) {
        if (!canGoBack || pushed != null || backed != null) return
        invokeCallback(current, result)
        backed = current
        stack = stack.dropLast(1)
    }

    internal fun endPush() {
        pushed?.let { stack = stack + it }
        pushed = null
    }

    internal fun endBack() {
        backed = null
    }
}

/**
 * Navigation facade passed to screens.
 *
 * Provides overloads for simple navigation (push/back without results)
 * and typed navigation (push/back with result callbacks).
 */
class Navigator<T : Screen<*>> internal constructor(
    private val state: StackNavigatorState<T>
) {
    fun push(screen: Screen<*>) { state.push(screen) }

    fun <R> push(screen: Screen<R>, resultCallback: (R?) -> Unit) {
        state.push(screen, resultCallback)
    }

    fun back() { state.back() }

    fun <R> back(result: R?) { state.back(result) }

    val canGoBack: Boolean get() = state.canGoBack
}

@Composable
fun <T : Screen<*>> rememberStackNavigatorState(initial: T): StackNavigatorState<T> =
    remember { StackNavigatorState(initial) }

private data class SlideFade(
    val startOffset: Float,
    val endOffset: Float,
    val startAlpha: Float,
    val endAlpha: Float,
) {
    fun offset(t: Float) = startOffset + (endOffset - startOffset) * t
    fun alpha(t: Float) = startAlpha + (endAlpha - startAlpha) * t
}

private fun fromRightIn() = SlideFade(1f, 0f, 0f, 1f)
private fun toLeftOut() = SlideFade(0f, -1f, 1f, 0f)
private fun fromLeftIn() = SlideFade(-1f, 0f, 0f, 1f)
private fun toRightOut() = SlideFade(0f, 1f, 1f, 0f)

private enum class ScreenRole {
    ENTER_PUSH, EXIT_PUSH, ENTER_BACK, EXIT_BACK, IDLE_TOP, IDLE_BACKGROUND
}

@Composable
fun <T : Screen<*>> StackNavigator(
    state: StackNavigatorState<T>,
    content: @Composable (T, Navigator<T>) -> Unit
) {
    BackHandler(enabled = state.canGoBack) {
        state.back<Unit>(null)
    }

    var widthPx by remember { mutableStateOf(-1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { widthPx = it.width.toFloat() }
    ) {
        val screens = buildList {
            addAll(state.stack)
            state.pushed?.let { add(it) }
            state.backed?.let { add(it) }
        }

        screens.forEach { screen ->
            val role = when {
                screen == state.pushed -> ScreenRole.ENTER_PUSH
                screen == state.current && state.pushed != null -> ScreenRole.EXIT_PUSH
                screen == state.backed -> ScreenRole.EXIT_BACK
                screen == state.current && state.backed != null -> ScreenRole.ENTER_BACK
                screen == state.current -> ScreenRole.IDLE_TOP
                else -> ScreenRole.IDLE_BACKGROUND
            }

            val transition = when (role) {
                ScreenRole.ENTER_PUSH -> fromRightIn()
                ScreenRole.EXIT_PUSH -> toLeftOut()
                ScreenRole.ENTER_BACK -> fromLeftIn()
                ScreenRole.EXIT_BACK -> toRightOut()
                else -> null
            }

            val anim = remember(screen, state.pushed, state.backed) { Animatable(0f) }

            LaunchedEffect(screen, state.pushed, state.backed) {
                transition?.let {
                    anim.snapTo(0f)
                    anim.animateTo(1f, tween(durationMillis = 300, easing = FastOutSlowInEasing))
                    if (role == ScreenRole.ENTER_PUSH) state.endPush()
                    if (role == ScreenRole.EXIT_BACK) state.endBack()
                }
            }

            val offsetX = when {
                widthPx < 0f -> 0f
                transition != null -> transition.offset(anim.value) * widthPx
                role == ScreenRole.IDLE_BACKGROUND -> -widthPx
                else -> 0f
            }

            val alpha = transition?.alpha(anim.value) ?: if (role == ScreenRole.IDLE_BACKGROUND) 0f else 1f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
            ) {
                content(screen, Navigator(state))
            }
        }
    }
}

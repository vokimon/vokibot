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
 * Base class for stack-based navigation screens.
 *
 * Subclasses must implement [render] to provide their UI.
 * Screen data should be passed via constructor parameters (must be [Serializable]).
 *
 * @param R The type of value returned when this screen completes via [StackNavigatorState.back].
 *          Use [Unit] for screens that do not return data.
 */
@Serializable
abstract class StackedScreen<R> {
    /**
     * Renders the screen UI.
     * Access screen data via `this` (e.g., `this.editingId`).
     * Register back behavior via `nav.onBack(this, ...)`
     */
    @Composable
    abstract fun render(nav: StackNavigatorState)
}

/**
 * Navigation state holding the stack, transient transition flags, and result callbacks.
 *
 * This class is passed directly to screens. Internal members are hidden by Kotlin's
 * [internal] visibility, so screens only see the public navigation API.
 */
class StackNavigatorState(initial: StackedScreen<*>) {
    constructor(fullStack: List<StackedScreen<*>>) : this(fullStack.last()) { stack = fullStack }

    var stack by mutableStateOf(listOf(initial))
        internal set

    internal var pushed: StackedScreen<*>? by mutableStateOf(null)
    internal var backed: StackedScreen<*>? by mutableStateOf(null)
    private val callbacks = mutableMapOf<Any, (Any?) -> Unit>()
    private val screenHandlers = mutableMapOf<StackedScreen<*>, () -> Unit>()

    val current: StackedScreen<*> get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    internal fun <R> registerCallback(screen: StackedScreen<*>, callback: (R?) -> Unit) {
        callbacks[screen] = { result ->
            @Suppress("UNCHECKED_CAST")
            callback(result as R?)
        }
    }

    internal fun <R> invokeCallback(screen: StackedScreen<*>, result: R?) {
        callbacks[screen]?.invoke(result)
        callbacks.remove(screen)
    }

    /**
     * Registers a custom handler for the system back button.
     *
     * Handlers are keyed explicitly to the [screen] parameter, preventing background
     * recompositions from interfering with the active top screen.
     *
     * @param screen The screen instance that owns this back behavior.
     * @param enabled Whether the custom handler should intercept back presses.
     * @param handler Logic to execute instead of default navigation.
     */
    fun onBack(screen: StackedScreen<*>, enabled: Boolean = true, handler: () -> Unit = {}) {
        if (enabled) screenHandlers[screen] = handler else screenHandlers.remove(screen)
    }

    internal fun handleBack(default: () -> Unit) {
        screenHandlers[current]?.invoke() ?: default()
    }

    /** Push without result callback (ignore return value). */
    fun push(screen: StackedScreen<*>) {
        if (pushed != null || backed != null) return
        pushed = screen
    }

    /** Push with typed result callback. */
    fun <R> push(screen: StackedScreen<R>, resultCallback: (R?) -> Unit) {
        if (pushed != null || backed != null) return
        @Suppress("UNCHECKED_CAST")
        registerCallback(screen, resultCallback)
        pushed = screen
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

@Composable
fun rememberStackNavigatorState(initial: StackedScreen<*>): StackNavigatorState =
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
fun StackNavigator(initial: StackedScreen<*>, vararg additional: StackedScreen<*>) {
    val state = remember {
        StackNavigatorState(buildList {
            add(initial)
            addAll(additional)
        })
    }

    BackHandler(enabled = state.canGoBack) {
        state.handleBack { state.back<Unit>(null) }
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
                screen.render(state)
            }
        }
    }
}

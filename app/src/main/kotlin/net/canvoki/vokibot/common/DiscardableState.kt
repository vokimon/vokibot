package net.canvoki.vokibot.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.R
import net.canvoki.vokibot.common.ConfirmDialog

/**
 * State holder for the discard-on-back pattern used by editors.
 *
 * Tracks whether the editor has unsaved changes and provides [markDirty] to
 * signal modifications. Used together with [rememberDiscardableState].
 */
class DiscardableState {
    var isDirty by mutableStateOf(false)
        internal set

    /** Marks the editor state as dirty (unsaved changes exist). */
    fun markDirty() {
        isDirty = true
    }

    companion object {
        val Saver =
            Saver<DiscardableState, Boolean>(
                save = { it.isDirty },
                restore = { saved -> DiscardableState().also { if (saved) it.markDirty() } },
            )
    }
}

/**
 * Registers a back-press handler that shows a confirmation dialog when
 * the editor has unsaved changes.
 *
 * Usage:
 * ```
 * val discardState = rememberDiscardableState(screen = this@Editor, nav = nav)
 * discardState.markDirty()  // on every user edit
 * discardState.isDirty = false  // on successful save or load
 * ```
 *
 * @param screen The [StackedScreen] instance that owns this back behavior.
 * @param nav The navigation state used to register the back handler and pop.
 * @return A [DiscardableState] that tracks the dirty flag.
 */
@Composable
fun rememberDiscardableState(
    screen: StackedScreen<*>,
    nav: StackNavigatorState,
): DiscardableState {
    val state = rememberSaveable(saver = DiscardableState.Saver) { DiscardableState() }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isDirty) {
        nav.onBack(screen, enabled = state.isDirty) {
            showDialog = true
        }
    }

    ConfirmDialog(
        show = showDialog,
        title = stringResource(R.string.discard_dialog_title),
        text = stringResource(R.string.discard_dialog_message),
        confirmText = stringResource(R.string.discard_dialog_confirm),
        dismissText = stringResource(R.string.discard_dialog_cancel),
        onConfirm = {
            showDialog = false
            state.isDirty = false
            nav.pop()
        },
        onDismiss = { showDialog = false },
    )

    return state
}

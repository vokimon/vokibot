package net.canvoki.vokibot.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed class ExecutionState {
    data object Idle : ExecutionState()

    data object Searching : ExecutionState()

    data object Executing : ExecutionState()

    data object NoTrigger : ExecutionState()

    data object NoAutomation : ExecutionState()

    data class Error(
        val message: String,
    ) : ExecutionState()
}

@Composable
fun TriggerDispatcher(
    uid: String?,
    missingTriggerInfoMessage: String,
) {
    var state by remember { mutableStateOf<ExecutionState>(ExecutionState.Idle) }
    LaunchedEffect(Unit) {
        state = ExecutionState.Searching
    }
    Loading("Activating trigger...")
}

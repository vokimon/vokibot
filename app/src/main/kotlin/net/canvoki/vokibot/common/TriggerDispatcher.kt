package net.canvoki.vokibot.common

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.canvoki.vokibot.Automation
import net.canvoki.vokibot.AutomationEditorActivity
import net.canvoki.vokibot.FileDataRepository
import net.canvoki.vokibot.R
import net.canvoki.vokibot.Trigger

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
    triggerId: String?,
    onDone: () -> Unit,
    onCreateAutomation: (triggerId: String) -> Unit,
    onCreateTriggerAndAction: () -> Unit,
    iconRes: Int,
    description: String,
    title: String,
) {
    val badInputError = stringResource(R.string.trigger_dispatcher_bad_input)
    val searchingText = stringResource(R.string.trigger_dispatcher_searching)
    val executingText = stringResource(R.string.trigger_dispatcher_executing)
    val notRegisteredHelp = stringResource(R.string.trigger_dispatcher_not_registered)
    val noAutomationHelp = stringResource(R.string.trigger_dispatcher_no_automation)
    val createAutomationText = stringResource(R.string.trigger_dispatcher_create_automation)

    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    var executionState by remember { mutableStateOf<ExecutionState>(ExecutionState.Idle) }
    var trigger by remember { mutableStateOf<Trigger?>(null) }

    LaunchedEffect(triggerId) {
        if (triggerId == null) {
            executionState = ExecutionState.Error(badInputError)
            return@LaunchedEffect
        }

        executionState = ExecutionState.Searching

        trigger = repository.trigger.load(id = triggerId)
        val triggerNotNull = trigger

        if (triggerNotNull == null) {
            executionState = ExecutionState.NoTrigger
            return@LaunchedEffect
        }

        executionState = ExecutionState.Executing
        val hadAutomations =
            Automation.executeByTrigger(repository, triggerNotNull.id, context) {
                (context as? ComponentActivity)?.runOnUiThread { onDone() }
            }
        if (!hadAutomations) {
            executionState = ExecutionState.NoAutomation
            return@LaunchedEffect
        }
    }

    when (executionState) {
        is ExecutionState.Idle,
        is ExecutionState.Searching,
        -> {
            Loading(searchingText)
        }
        is ExecutionState.Executing -> {
            Loading(executingText)
        }
        is ExecutionState.Error -> {
            ErrorSplash(text = (executionState as? ExecutionState.Error)?.message ?: "...")
        }
        is ExecutionState.NoTrigger -> {
            NotAutomatedYet(
                iconRes = iconRes,
                title = title,
                subtitle = description,
                help = notRegisteredHelp,
                actionText = createAutomationText,
                action = {
                    onCreateTriggerAndAction()
                },
            )
        }
        is ExecutionState.NoAutomation -> {
            NotAutomatedYet(
                iconRes = iconRes,
                title = title,
                subtitle = description,
                help = noAutomationHelp,
                actionText = createAutomationText,
                action = {
                    onCreateAutomation(triggerId!!)
                },
            )
        }
    }
}

fun editAutomationForTrigger(
    context: Context,
    triggerId: String,
) {
    val editorIntent =
        Intent(context, AutomationEditorActivity::class.java).apply {
            putExtra("trigger_id", triggerId)
        }
    context.startActivity(editorIntent)
}

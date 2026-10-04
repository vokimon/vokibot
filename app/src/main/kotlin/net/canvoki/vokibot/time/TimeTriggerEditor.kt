package net.canvoki.vokibot.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.FileDataRepository
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.rememberDiscardableState
import java.time.LocalDateTime

@Serializable
data class TimeTriggerEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        TimeTriggerEditor(nav, this, editingId)
    }
}

@Composable
fun TimeTriggerEditor(
    nav: StackNavigatorState,
    editor: TimeTriggerEditor,
    editingId: String?,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    val scope = rememberCoroutineScope()
    var isSaving by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(editingId) {
        if (editingId == null) discardState.markDirty()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        EditorHeader(
            icon = painterResource(TimeTrigger.iconRes),
            title = "Time",
            actionText = "Done",
            actionEnabled = !isSaving,
            actionIsRunning = isSaving,
            action = {
                isSaving = true
                scope.launch {
                    val existing = editingId?.let { repository.trigger.load(it) as? TimeTrigger }
                    val trigger =
                        existing
                            ?: TimeTrigger(startAt = LocalDateTime.now(), displayName = "")
                    repository.trigger.save(trigger)
                    isSaving = false
                    discardState.isDirty = false
                    nav.pop()
                }
            },
        )

        Text(
            text = editingId ?: "new",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

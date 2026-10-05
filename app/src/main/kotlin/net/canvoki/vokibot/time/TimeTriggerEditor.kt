package net.canvoki.vokibot.time

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.FileDataRepository
import net.canvoki.vokibot.R
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.MagicTextField
import net.canvoki.vokibot.common.rememberDiscardableState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TIME_PATTERN = "HH:mm"
private const val DATE_PATTERN = "EEE d MMM yyyy"

private val StartAtSaver =
    Saver<LocalDateTime, String>(
        save = { it.toString() },
        restore = { LocalDateTime.parse(it) },
    )

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
private fun ValueEditRow(
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
        )
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
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
    var displayName by rememberSaveable { mutableStateOf("") }
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    var startAt by rememberSaveable(stateSaver = StartAtSaver) {
        mutableStateOf(LocalDateTime.now())
    }

    LaunchedEffect(editingId) {
        if (editingId == null) {
            discardState.markDirty()
        } else if (!hasLoaded) {
            val existing = repository.trigger.load(editingId) as? TimeTrigger
            existing?.let {
                displayName = it.displayName
                startAt = it.startAt
            }
            hasLoaded = true
            discardState.isDirty = false
        }
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
            actionEnabled = displayName.isNotBlank() && !isSaving,
            actionIsRunning = isSaving,
            action = {
                if (displayName.isNotBlank()) {
                    isSaving = true
                    val trigger =
                        TimeTrigger(
                            id = editingId,
                            startAt = startAt,
                            displayName = displayName.trim(),
                        )
                    scope.launch {
                        repository.trigger.save(trigger)
                        isSaving = false
                        discardState.isDirty = false
                        nav.pop()
                    }
                }
            },
        )

        MagicTextField(
            value = displayName,
            onValueChange = {
                displayName = it
                discardState.markDirty()
            },
            onMagicClick = {},
            label = { Text("Name") },
            placeholder = { Text("e.g. Morning coffee") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        ValueEditRow(
            value = startAt.format(DateTimeFormatter.ofPattern(TIME_PATTERN)),
            onClick = {},
        )
        ValueEditRow(
            value = startAt.format(DateTimeFormatter.ofPattern(DATE_PATTERN)),
            onClick = {},
        )
    }
}

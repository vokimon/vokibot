package net.canvoki.vokibot.time

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import net.canvoki.vokibot.common.MissingPermissionBanner
import net.canvoki.vokibot.common.rememberDiscardableState
import net.canvoki.vokibot.common.rememberPermissionState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
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
@OptIn(ExperimentalMaterial3Api::class)
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
    var recurrence by rememberSaveable { mutableStateOf(Recurrence.None) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(editingId) {
        if (editingId == null) {
            discardState.markDirty()
        } else if (!hasLoaded) {
            val existing = repository.trigger.load(editingId) as? TimeTrigger
            existing?.let {
                displayName = it.displayName
                startAt = it.startAt
                recurrence = it.recurrence
            }
            hasLoaded = true
            discardState.isDirty = false
        }
    }

    if (showTimePicker) {
        val timePickerState =
            rememberTimePickerState(
                initialHour = startAt.hour,
                initialMinute = startAt.minute,
            )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        startAt = startAt.toLocalDate().atTime(timePickerState.hour, timePickerState.minute)
                        discardState.markDirty()
                        showTimePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    startAt
                        .toLocalDate()
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli(),
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            startAt = date.atTime(startAt.toLocalTime())
                            discardState.markDirty()
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
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
                            recurrence = recurrence,
                        )
                    scope.launch {
                        repository.trigger.save(trigger)
                        trigger.schedule(context)
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
            onMagicClick = {
                displayName =
                    TimeTrigger(editingId, startAt, displayName, recurrence)
                        .description(context)
                discardState.markDirty()
            },
            label = { Text("Name") },
            placeholder = { Text("e.g. Morning coffee") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            Recurrence.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = recurrence == entry,
                    onClick = {
                        recurrence = entry
                        discardState.markDirty()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, Recurrence.entries.size),
                    label = {
                        Text(
                            when (entry) {
                                Recurrence.None -> "Once"
                                Recurrence.Daily -> "Daily"
                            },
                        )
                    },
                )
            }
        }

        ValueEditRow(
            value = startAt.format(DateTimeFormatter.ofPattern(TIME_PATTERN)),
            onClick = { showTimePicker = true },
        )
        if (recurrence == Recurrence.None) {
            ValueEditRow(
                value = startAt.format(DateTimeFormatter.ofPattern(DATE_PATTERN)),
                onClick = { showDatePicker = true },
            )
        }

        MissingPermissionBanner(
            state = rememberPermissionState(Manifest.permission.SYSTEM_ALERT_WINDOW),
            message =
                "To let automations open other apps while VokiBot is " +
                    "closed, allow VokiBot the 'Display over other apps' permission " +
                    "in the settings list",
        )
    }
}

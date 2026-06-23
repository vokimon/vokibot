package net.canvoki.vokibot

import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.TryCommandButton
import net.canvoki.vokibot.common.rememberDiscardableState

@Serializable
data class ChangeSettingValueCommandEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        ChangeSettingValueCommandEditor(nav, this, editingId)
    }
}

@Composable
fun ChangeSettingValueCommandEditor(
    nav: StackNavigatorState,
    editor: ChangeSettingValueCommandEditor,
    editingId: String?,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    var isSaving by rememberSaveable { mutableStateOf(false) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    var setting by remember { mutableStateOf<String>(Settings.System.SCREEN_BRIGHTNESS_MODE) }
    val settingTitle = "Adaptive Brightness"
    val settingHelp = "When enabled the screen brightness will adapt to environmental light"
    var value by remember { mutableStateOf<ExtraValue>(ExtraValue.BooleanValue(false)) }

    fun buildCommand() =
        ChangeSettingValueCommand(
            id = editingId,
            key = setting,
            value = value,
        )
    val isReadyToRun = true // TODO
    val isReadyToSave = true // TODO

    LaunchedEffect(editingId) {
        if (editingId != null && !hasLoaded) {
            val existing =
                repository.command.load(editingId) as? ChangeSettingValueCommand
            existing?.let {
                value = it.value
            }
            hasLoaded = true
        }
        discardState.isDirty = false
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
            icon = painterResource(ChangeSettingValueCommand.iconRes),
            title = stringResource(ChangeSettingValueCommand.labelRes),
            actionText = stringResource(R.string.bluetooth_device_editor_save),
            actionEnabled = isReadyToSave && !isSaving,
            actionIsRunning = isSaving,
            action = {
                if (isReadyToSave) {
                    isSaving = true
                    val command = buildCommand()
                    repository.command.save(command)
                    isSaving = false
                    nav.pop()
                }
            },
        )

        OutlinedTextField(
            value = settingTitle,
            onValueChange = {},
            label = { Text("Setting to change") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: push SettingPickerScreen */ },
        )

        Text(
            text = settingHelp,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp),
        )

        value.Editor(
            spec = ExtraSpec(key = "Value to set", type = ExtraType.Boolean),
            onChanged = { value = it },
        )

        TryCommandButton(
            enabled = isReadyToRun,
            buildCommand = { buildCommand() },
        )
    }
}

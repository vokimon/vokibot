package net.canvoki.vokibot

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.OneTimeNotice
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.ExtraValueSaver
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.MissingPermissionBanner
import net.canvoki.vokibot.common.TryCommandButton
import net.canvoki.vokibot.common.rememberDiscardableState
import net.canvoki.vokibot.common.rememberPermissionState

@Serializable
data class ChangeSettingCommandEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        ChangeSettingCommandEditor(nav, this, editingId)
    }
}

@Composable
fun SelectButton(
    text: String?,
    label: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier =
                Modifier.fillMaxWidth().clickable { onClick() },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text ?: stringResource(R.string.select_button_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        text?.let { MaterialTheme.colorScheme.onSurfaceVariant }
                            ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_drop_down),
                    contentDescription = null,
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .offset(x = 8.dp, y = 4.dp)
                    .padding(horizontal = 4.dp),
        )
    }
}

@Composable
fun ChangeSettingCommandEditor(
    nav: StackNavigatorState,
    editor: ChangeSettingCommandEditor,
    editingId: String?,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    var isSaving by rememberSaveable { mutableStateOf(false) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    var hasLoaded by rememberSaveable { mutableStateOf(false) }
    var settingKey by rememberSaveable { mutableStateOf<String?>(null) }
    val settingSpec = settingKey?.let { id -> SettingSpec.get(id) }
    val settingTitle = settingSpec?.let { stringResource(it.name) }
    val writeSettingsPerm = rememberPermissionState("android.permission.WRITE_SETTINGS")
    var rawEdit by rememberSaveable { mutableStateOf(false) }
    var value by rememberSaveable(stateSaver = ExtraValueSaver) {
        mutableStateOf<ExtraValue>(ExtraValue.BooleanValue(false))
    }

    fun buildCommand(): ChangeSettingCommand {
        require(settingKey != null)
        return ChangeSettingCommand(
            id = editingId,
            key = settingKey!!,
            value = value,
        )
    }
    val isReadyToRun = writeSettingsPerm.isGranted && settingKey != null
    val isReadyToSave = settingKey != null

    LaunchedEffect(editingId) {
        if (editingId != null && !hasLoaded) {
            val existing =
                repository.command.load(editingId) as? ChangeSettingCommand
            existing?.let {
                settingKey = it.key
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
            icon = painterResource(ChangeSettingCommand.iconRes),
            title = stringResource(ChangeSettingCommand.labelRes),
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

        SelectButton(
            text = settingTitle,
            label = stringResource(R.string.change_setting_field_setting),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                nav.push(SettingList) { result ->
                    result?.let {
                        settingKey = it
                        value = SettingSpec
                            .get(it)
                            ?.type
                            ?.defaultValue()
                            ?: ExtraValue.StringValue("")
                        rawEdit = false
                        discardState.markDirty()
                    }
                }
            },
        )

        if (settingSpec != null) {
            Text(
                text = stringResource(settingSpec.description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp),
            )

            if (rawEdit) {
                OutlinedTextField(
                    value = settingSpec.type.toStoredSetting(value),
                    onValueChange = { raw ->
                        value = settingSpec.type.fromStoredSetting(raw)
                        discardState.markDirty()
                    },
                    label = { Text(stringResource(R.string.change_setting_field_value)) },
                    supportingText = { Text(stringResource(settingSpec.rawHelp)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OneTimeNotice(
                    noticeId = "raw_edit_warning_5", // TODO: clean up for production
                    title = stringResource(R.string.change_setting_raw_edit),
                    message = stringResource(R.string.change_setting_raw_edit_warning),
                )
            } else {
                settingSpec.type.Editor(
                    label = stringResource(R.string.change_setting_field_value),
                    value = value,
                    onChanged = {
                        value = it
                        discardState.markDirty()
                    },
                )
            }

            FilterChip(
                selected = rawEdit,
                onClick = { rawEdit = !rawEdit },
                label = { Text(stringResource(R.string.change_setting_raw_edit)) },
                leadingIcon = {
                    if (rawEdit) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                },
                modifier = Modifier.align(Alignment.End),
            )

            MissingPermissionBanner(
                state = writeSettingsPerm,
                message = stringResource(R.string.change_setting_permission_required),
            )
        }

        TryCommandButton(
            enabled = isReadyToRun,
            buildCommand = { buildCommand() },
        )
    }
}

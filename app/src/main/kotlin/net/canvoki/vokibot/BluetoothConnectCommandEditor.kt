package net.canvoki.vokibot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
data class BluetoothConnectCommandEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        BluetoothConnectCommandEditor(nav, this, editingId)
    }
}

@Composable
fun BluetoothConnectCommandEditor(
    nav: StackNavigatorState,
    editor: BluetoothConnectCommandEditor,
    editingId: String?,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    var name by rememberSaveable { mutableStateOf("") }
    var mac by rememberSaveable { mutableStateOf("") }
    var selectedAction by rememberSaveable { mutableStateOf(ConnectionAction.CONNECT) }
    var affectedRoles by remember { mutableStateOf(emptySet<DisconnectableRole>()) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    val btUsability = rememberBluetoothUsabilityState()

    fun buildCommand() =
        BluetoothConnectCommand(
            id = editingId,
            deviceName = name.trim(),
            macAddress = mac.trim(),
            action = selectedAction,
            affectedRoles = affectedRoles,
        )

    LaunchedEffect(editingId) {
        if (editingId != null && !hasLoaded) {
            val existing =
                repository.command.load(editingId) as? BluetoothConnectCommand
            existing?.let {
                name = it.deviceName
                mac = it.macAddress
                selectedAction = it.action
                affectedRoles = it.affectedRoles
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
            icon = painterResource(BluetoothConnectCommand.iconRes),
            title = stringResource(BluetoothConnectCommand.labelRes),
            actionText = stringResource(R.string.bluetooth_device_editor_save),
            actionEnabled = name.isNotBlank() && mac.isNotBlank() && !isSaving,
            actionIsRunning = isSaving,
            action = {
                if (name.isNotBlank() && mac.isNotBlank()) {
                    isSaving = true
                    val command = buildCommand()
                    repository.command.save(command)
                    isSaving = false
                    nav.pop()
                }
            },
        )

        BluetoothNameField(
            value = name,
            onValueChange = {
                name = it
                discardState.markDirty()
            },
        )

        BluetoothMacField(
            value = mac,
            onValueChange = {
                mac = it
                discardState.markDirty()
            },
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ConnectionAction.entries.forEachIndexed { index, action ->
                SegmentedButton(
                    selected = selectedAction == action,
                    onClick = {
                        selectedAction = action
                        discardState.markDirty()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, ConnectionAction.entries.size),
                    label = {
                        Text(
                            when (action) {
                                ConnectionAction.CONNECT -> stringResource(R.string.bluetooth_editor_action_connect)
                                ConnectionAction.DISCONNECT ->
                                    stringResource(
                                        R.string.bluetooth_editor_action_disconnect,
                                    )
                            },
                        )
                    },
                )
            }
        }

        if (selectedAction == ConnectionAction.DISCONNECT) {
            BluetoothProfileSelector(
                affectedRoles = affectedRoles,
                onAffectedRolesChange = {
                    affectedRoles = it
                    discardState.markDirty()
                },
            )
        }

        TryCommandButton(
            enabled = name.isNotBlank() && mac.isNotBlank() && btUsability.isUsable,
            buildCommand = { buildCommand() },
        )

        BluetoothDeviceChooser(
            state = btUsability,
            onDeviceSelected = { deviceName, macAddress ->
                name = deviceName
                mac = macAddress
                discardState.markDirty()
            },
        )
    }
}

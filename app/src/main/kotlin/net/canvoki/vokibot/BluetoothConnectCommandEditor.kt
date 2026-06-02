package net.canvoki.vokibot

import android.Manifest
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import net.canvoki.vokibot.common.rememberPermissionState

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
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    val connectPermState =
        rememberPermissionState(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else null,
        )

    val bluetoothAdapter =
        remember {
            val manager = context.getSystemService(BluetoothManager::class.java)
            manager?.adapter
        }

    fun buildCommand() =
        BluetoothConnectCommand(
            deviceName = name.trim(),
            macAddress = mac.trim(),
            action = selectedAction,
        )

    val bondedDevices =
        remember(connectPermState.isGranted) {
            if (!connectPermState.isGranted) {
                emptyList()
            } else {
                bluetoothAdapter
                    ?.bondedDevices
                    ?.sortedBy { it.name?.lowercase() } ?: emptyList()
            }
        }

    LaunchedEffect(editingId) {
        if (editingId != null && !hasLoaded) {
            val existing =
                repository.command.load(editingId) as? BluetoothConnectCommand
            existing?.let {
                name = it.deviceName
                mac = it.macAddress
                selectedAction = it.action
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
                                ConnectionAction.CONNECT -> "Connect"
                                ConnectionAction.DISCONNECT -> "Disconnect"
                            },
                        )
                    },
                )
            }
        }

        TryCommandButton(
            enabled = name.isNotBlank() && mac.isNotBlank() && connectPermState.isGranted,
            buildCommand = { buildCommand() },
        )

        if (bluetoothAdapter != null) {
            HorizontalDivider()
            if (connectPermState.isGranted) {
                PairedDevicesList(
                    devices = bondedDevices,
                    onDeviceSelected = { deviceName, macAddress ->
                        name = deviceName
                        mac = macAddress
                        discardState.markDirty()
                    },
                )
            } else {
                PermissionBanner(onGrantClicked = { connectPermState.request() })
            }
        }
    }
}

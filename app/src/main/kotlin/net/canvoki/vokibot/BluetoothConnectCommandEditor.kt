package net.canvoki.vokibot

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.WarningBanner
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
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    fun checkConnectPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= 31) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    var connectPermissionGranted by remember { mutableStateOf(checkConnectPermission()) }
    var permissionDenied by remember { mutableStateOf(false) }

    val bluetoothAdapter =
        remember {
            val manager = context.getSystemService(BluetoothManager::class.java)
            manager?.adapter
        }

    fun buildCommand() =
        BluetoothConnectCommand(
            deviceName = name.trim(),
            macAddress = mac.trim(),
            action = ConnectionAction.CONNECT,
        )

    val bondedDevices =
        remember(connectPermissionGranted) {
            if (!connectPermissionGranted) {
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
                repository.trigger.load(editingId) as? BluetoothDeviceTrigger
            existing?.let {
                name = it.name
                mac = it.macAddress
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

        if (bluetoothAdapter != null) {
            HorizontalDivider()
            if (connectPermissionGranted) {
                PairedDevicesList(
                    devices = bondedDevices,
                    onDeviceSelected = { deviceName, macAddress ->
                        name = deviceName
                        mac = macAddress
                        discardState.markDirty()
                    },
                )
            } else {
                PermissionBanner(
                    permissionDenied=permissionDenied,
                    onPermissionResponse = { granted ->
                        connectPermissionGranted = granted
                        if (!granted) permissionDenied = true
                    },
                )
            }
        }
    }
}

package net.canvoki.vokibot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import net.canvoki.vokibot.common.WarningBanner
import net.canvoki.vokibot.common.rememberPermissionState

@Composable
fun PermissionBanner(onGrantClicked: () -> Unit) {
    WarningBanner(
        message = stringResource(R.string.bluetooth_device_editor_permission_warning),
        buttonText = stringResource(R.string.bluetooth_device_editor_grant_permission),
        onClick = onGrantClicked,
    )
}

private fun isBluetoothEnabled(adapter: BluetoothAdapter): Boolean =
    try {
        val service = BluetoothAdapter::class.java.getDeclaredMethod("getService").invoke(adapter)
        service?.javaClass?.getDeclaredMethod("isEnabled")?.invoke(service) as? Boolean ?: false
    } catch (_: Exception) {
        @Suppress("DEPRECATION")
        adapter.isEnabled
    }

data class BluetoothUsabilityState(
    val isPermissionGranted: Boolean,
    val adapter: BluetoothAdapter?,
    val requestPermission: () -> Unit,
    val isEnabled: Boolean,
) {
    val isAdapterAvailable: Boolean get() = adapter != null
    val isUsable: Boolean get() = isAdapterAvailable && isPermissionGranted && isEnabled
}

@Composable
fun rememberBluetoothUsabilityState(): BluetoothUsabilityState {
    val connectPermState =
        rememberPermissionState(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else null,
        )
    val context = LocalContext.current
    val adapter =
        remember {
            val manager = context.getSystemService(BluetoothManager::class.java)
            manager?.adapter
        }

    var isEnabled by remember(adapter) {
        mutableStateOf(adapter?.let { isBluetoothEnabled(it) } ?: false)
    }

    DisposableEffect(adapter) {
        if (adapter == null) return@DisposableEffect onDispose {}
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent,
                ) {
                    if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                        isEnabled = isBluetoothEnabled(adapter)
                    }
                }
            }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    return BluetoothUsabilityState(
        isPermissionGranted = connectPermState.isGranted,
        adapter = adapter,
        requestPermission = { connectPermState.request() },
        isEnabled = isEnabled,
    )
}

@Composable
fun BluetoothDeviceChooser(
    state: BluetoothUsabilityState = rememberBluetoothUsabilityState(),
    onDeviceSelected: (name: String, mac: String) -> Unit,
) {
    val context = LocalContext.current
    val bondedDevices =
        remember(state.isPermissionGranted, state.isEnabled, state.adapter) {
            if (!state.isPermissionGranted || !state.isEnabled || state.adapter == null) {
                emptyList()
            } else {
                @Suppress("MissingPermission")
                state.adapter
                    .bondedDevices
                    ?.sortedBy { it.name?.lowercase() } ?: emptyList()
            }
        }

    HorizontalDivider()
    if (!state.isAdapterAvailable) {
        WarningBanner(
            message = stringResource(R.string.bluetooth_device_editor_no_bluetooth),
        )
    } else if (!state.isEnabled) {
        WarningBanner(
            message = stringResource(R.string.bluetooth_device_editor_disabled),
            buttonText = stringResource(R.string.bluetooth_device_editor_button_enable),
            onClick = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            },
        )
    } else if (!state.isPermissionGranted) {
        PermissionBanner(onGrantClicked = state.requestPermission)
    } else {
        PairedDevicesList(
            devices = bondedDevices,
            onDeviceSelected = onDeviceSelected,
        )
    }
}

@Composable
fun BluetoothNameField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.bluetooth_device_editor_name_label)) },
        placeholder = { Text(stringResource(R.string.bluetooth_device_editor_name_placeholder)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
}

@Composable
fun BluetoothMacField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.bluetooth_device_editor_mac_label)) },
        placeholder = { Text("AA:BB:CC:DD:EE:FF") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
    )
}

@Composable
fun PairedDevicesList(
    devices: List<BluetoothDevice>,
    onDeviceSelected: (name: String, mac: String) -> Unit,
) {
    Text(
        text = stringResource(R.string.bluetooth_device_editor_paired_devices),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Column {
        if (devices.isEmpty()) {
            Text(
                text = stringResource(R.string.bluetooth_device_editor_no_paired_devices),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        devices.forEachIndexed { index, device ->
            if (index > 0) HorizontalDivider()
            val deviceName = device.safeDisplayName()
            BluetoothDeviceItem(
                device = device,
                deviceName = deviceName,
                onClick = { onDeviceSelected(deviceName, device.address) },
            )
        }
    }
}

@Composable
private fun BluetoothDeviceItem(
    device: BluetoothDevice,
    deviceName: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Icon(
                painter = painterResource(bluetoothDeviceIcon(device)),
                contentDescription = null,
                modifier = Modifier.size(40.dp).padding(end = 12.dp),
                tint = tint,
            )
            Column {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text =
                        bluetoothDeviceLabelRes(device)?.let {
                            "${device.address} - ${stringResource(it)}"
                        } ?: device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

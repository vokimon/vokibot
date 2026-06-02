package net.canvoki.vokibot

import android.bluetooth.BluetoothDevice
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

@Composable
fun PermissionBanner(onGrantClicked: () -> Unit) {
    WarningBanner(
        message = stringResource(R.string.bluetooth_device_editor_permission_warning),
        buttonText = stringResource(R.string.bluetooth_device_editor_grant_permission),
        onClick = onGrantClicked,
    )
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

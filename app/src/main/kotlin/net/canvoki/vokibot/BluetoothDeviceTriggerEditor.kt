package net.canvoki.vokibot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

@Composable
private fun permissionRequestLauncher(onResult: (Boolean) -> Unit) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult,
    )

@Serializable
data class BluetoothDeviceTriggerEditor(
    val triggerId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }

        var name by rememberSaveable { mutableStateOf("") }
        var mac by rememberSaveable { mutableStateOf("") }
        var isSaving by rememberSaveable { mutableStateOf(false) }
        var isDirty by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }
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

        val connectPermissionLauncher =
            permissionRequestLauncher { granted ->
                connectPermissionGranted = granted
            }

        fun checkScanPermission(): Boolean {
            val permission =
                if (Build.VERSION.SDK_INT >= 31) {
                    Manifest.permission.BLUETOOTH_SCAN
                } else {
                    Manifest.permission.ACCESS_FINE_LOCATION
                }
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        var scanPermissionGranted by remember { mutableStateOf(checkScanPermission()) }

        val scanPermissionLauncher =
            permissionRequestLauncher { granted ->
                scanPermissionGranted = granted
            }

        val bluetoothAdapter =
            remember {
                val manager = context.getSystemService(BluetoothManager::class.java)
                manager?.adapter
            }
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

        LaunchedEffect(triggerId) {
            if (triggerId != null && !hasLoaded) {
                val existing =
                    repository.trigger.load(triggerId) as? BluetoothDeviceTrigger
                existing?.let {
                    name = it.name
                    mac = it.macAddress
                }
                hasLoaded = true
            }
            isDirty = false
        }

        LaunchedEffect(isDirty) {
            nav.onBack(this@BluetoothDeviceTriggerEditor, enabled = isDirty) {
                showDiscardDialog = true
            }
        }

        ConfirmDialog(
            show = showDiscardDialog,
            title = "Discard changes?",
            text = "Unsaved changes will be lost.",
            confirmText = "Discard",
            dismissText = "Cancel",
            onConfirm = {
                isDirty = false
                showDiscardDialog = false
                nav.pop()
            },
            onDismiss = { showDiscardDialog = false },
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                icon = painterResource(R.drawable.ic_bluetooth),
                title = "Bluetooth Device",
                actionText = "Save",
                actionEnabled = name.isNotBlank() && mac.isNotBlank() && !isSaving,
                actionIsRunning = isSaving,
                action = {
                    if (name.isNotBlank() && mac.isNotBlank()) {
                        isSaving = true
                        val trigger =
                            BluetoothDeviceTrigger(
                                name = name.trim(),
                                macAddress = mac.trim(),
                            )
                        repository.trigger.save(trigger)
                        isSaving = false
                        nav.pop()
                    }
                },
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isDirty = true
                },
                label = { Text("Device name") },
                placeholder = { Text("e.g. Car hands-free") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = mac,
                onValueChange = {
                    mac = it
                    isDirty = true
                },
                label = { Text("MAC address") },
                placeholder = { Text("AA:BB:CC:DD:EE:FF") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
            )

            if (bluetoothAdapter != null) {
                HorizontalDivider()
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Paired devices",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    if (!connectPermissionGranted) {
                        TextButton(
                            onClick = {
                                connectPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            },
                        ) {
                            Text("Grant permission")
                        }
                    }
                }
                if (connectPermissionGranted) {
                    Column {
                        bondedDevices.forEachIndexed { index, device ->
                            if (index > 0) HorizontalDivider()
                            val deviceName = device.alias ?: device.name ?: device.address
                            BluetoothDeviceItem(
                                device = device,
                                deviceName = deviceName,
                                onClick = {
                                    name = deviceName
                                    mac = device.address
                                    isDirty = true
                                },
                            )
                        }
                    }

                    HorizontalDivider()
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Other nearby devices",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = {
                                val permission =
                                    when {
                                        Build.VERSION.SDK_INT >= 31 -> Manifest.permission.BLUETOOTH_SCAN
                                        else -> Manifest.permission.ACCESS_FINE_LOCATION
                                    }
                                if (!scanPermissionGranted) {
                                    scanPermissionLauncher.launch(permission)
                                }
                                // TODO: start discovery — next step
                            },
                        ) {
                            Text("Scan")
                        }
                    }
                }
            }
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
                    text = bluetoothDeviceLabelRes(device)?.let {
                        "${device.address} - ${stringResource(it)}"
                    } ?: device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
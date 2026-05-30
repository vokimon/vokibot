package net.canvoki.vokibot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

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
            modifier = Modifier.fillMaxSize().padding(8.dp),
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
        }
    }
}

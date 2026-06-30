package net.canvoki.vokibot.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.FileDataRepository
import net.canvoki.vokibot.R
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.rememberDiscardableState

@Serializable
data class BluetoothDeviceTriggerEditor(
    val triggerId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        BluetoothDeviceTriggerEditor(nav, this, triggerId)
    }
}

@Composable
fun BluetoothDeviceTriggerEditor(
    nav: StackNavigatorState,
    editor: BluetoothDeviceTriggerEditor,
    editingId: String?,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }

    var name by rememberSaveable { mutableStateOf("") }
    var mac by rememberSaveable { mutableStateOf("") }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val discardState = rememberDiscardableState(screen = editor, nav = nav)
    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    fun buildTrigger() =
        BluetoothDeviceTrigger(
            name = name.trim(),
            macAddress = mac.trim(),
        )

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
            icon = painterResource(BluetoothDeviceTrigger.iconRes),
            title = stringResource(BluetoothDeviceTrigger.labelRes),
            actionText = stringResource(R.string.bluetooth_device_editor_save),
            actionEnabled = name.isNotBlank() && mac.isNotBlank() && !isSaving,
            actionIsRunning = isSaving,
            action = {
                if (name.isNotBlank() && mac.isNotBlank()) {
                    isSaving = true
                    val trigger = buildTrigger()
                    repository.trigger.save(trigger)
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

        BluetoothDeviceChooser(
            onDeviceSelected = { deviceName, macAddress ->
                name = deviceName
                mac = macAddress
                discardState.markDirty()
            },
        )
    }
}

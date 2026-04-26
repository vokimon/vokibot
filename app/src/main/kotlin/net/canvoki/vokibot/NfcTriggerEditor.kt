package net.canvoki.vokibot

import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.spike.StackNavigatorState
import net.canvoki.shared.component.spike.StackedScreen

@Serializable
data object NfcTriggerEditor : StackedScreen<Unit>() {
    @Composable
    override fun render(nav: StackNavigatorState) {
        NfcTriggerEditor(
            onSaved = { nav.pop() },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcTriggerEditor(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val repository = remember { FileDataRepository.fromContext(context) }

    var displayName by rememberSaveable { mutableStateOf("") }
    var uid by rememberSaveable { mutableStateOf("") }
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var scanSuccess by remember { mutableStateOf(false) }
    var isNfcAvailable by rememberSaveable { mutableStateOf(true) }
    var isNfcEnabled by rememberSaveable { mutableStateOf(true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            val existing = repository.nfcTrigger.load(uid)
            if (existing != null && displayName.isBlank()) {
                displayName = existing.displayName
            }
        }
    }

    LaunchedEffect(Unit) {
        if (nfcAdapter == null) {
            isNfcAvailable = false
        } else if (!nfcAdapter.isEnabled) {
            isNfcEnabled = false
        }
    }

    DisposableEffect(isScanning) {
        val callback =
            NfcAdapter.ReaderCallback { tag ->
                val hexUid = tag.id.joinToString(":") { "%02X".format(it) }
                activity?.runOnUiThread {
                    uid = hexUid
                    isScanning = false
                    scanSuccess = true
                    scope.launch {
                        delay(2000)
                        scanSuccess = false
                    }
                }
            }

        if (isScanning && nfcAdapter != null && nfcAdapter.isEnabled) {
            nfcAdapter.enableReaderMode(
                activity,
                callback,
                NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null,
            )
        }

        onDispose {
            if (isScanning && nfcAdapter != null) {
                nfcAdapter.disableReaderMode(activity)
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!isNfcAvailable) {
            Text(
                text = stringResource(R.string.nfc_not_available),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else if (!isNfcEnabled) {
            Text(
                text = stringResource(R.string.nfc_not_enabled),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.nfc_trigger_name_label)) },
                placeholder = { Text(stringResource(R.string.nfc_trigger_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = uid,
                onValueChange = { uid = it },
                label = { Text(stringResource(R.string.nfc_trigger_uid_label)) },
                placeholder = { Text("04:AB:12:CD:56:78:90") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                trailingIcon = {
                    IconButton(onClick = { isScanning = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_nfc),
                            contentDescription = stringResource(R.string.nfc_trigger_scan),
                        )
                    }
                },
            )

            Button(
                onClick = { isScanning = !isScanning },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    if (isScanning) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
            ) {
                Icon(
                    painter =
                        painterResource(
                            if (isScanning) R.drawable.ic_close else R.drawable.ic_nfc,
                        ),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                        if (isScanning) {
                            stringResource(R.string.nfc_trigger_scanning)
                        } else {
                            stringResource(R.string.nfc_trigger_scan)
                        },
                )
            }

            if (isScanning) {
                Text(
                    text = stringResource(R.string.nfc_trigger_scanning_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (scanSuccess) {
                Text(
                    text = stringResource(R.string.nfc_trigger_scan_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (displayName.isNotBlank() && uid.isNotBlank()) {
                        isSaving = true
                        val trigger =
                            NfcTrigger(
                                displayName = displayName.trim(),
                                uid = uid.trim(),
                            )
                        repository.nfcTrigger.save(trigger)
                        isSaving = false
                        onSaved()
                    }
                },
                enabled = displayName.isNotBlank() && uid.isNotBlank() && !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.nfc_trigger_save))
                }
            }
        }
    }
}

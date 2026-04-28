package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen

@Serializable
data object NfcTriggerEditor : StackedScreen<Unit>() {
    @Composable
    override fun render(nav: StackNavigatorState) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val activity = context as? ComponentActivity
        val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
        val repository = remember { FileDataRepository.fromContext(context) }

        var displayName by rememberSaveable { mutableStateOf("") }
        var uid by rememberSaveable { mutableStateOf("") }
        var scanSuccess by remember { mutableStateOf(false) }
        var isNfcAvailable by rememberSaveable { mutableStateOf(true) }
        var isNfcEnabled by rememberSaveable { mutableStateOf(true) }
        var isSaving by rememberSaveable { mutableStateOf(false) }

        fun checkNfcState() {
            isNfcAvailable = nfcAdapter != null
            isNfcEnabled = nfcAdapter?.isEnabled == true
        }

        // Initial check + periodic polling to sync with system settings
        LaunchedEffect(Unit) {
            checkNfcState()
            while (true) {
                delay(2000)
                checkNfcState()
            }
        }

        LaunchedEffect(uid) {
            if (uid.isNotBlank()) {
                val existing = repository.nfcTrigger.load(uid)
                if (existing != null && displayName.isBlank()) {
                    displayName = existing.displayName
                } else {
                    displayName = "NFC $uid"
                }
            }
        }

        // Enable reader mode only when hardware is present and enabled
        DisposableEffect(isNfcEnabled, isNfcAvailable, nfcAdapter) {
            val callback =
                NfcAdapter.ReaderCallback { tag ->
                    val hexUid = tag.id.joinToString(":") { "%02X".format(it) }
                    activity?.runOnUiThread {
                        uid = hexUid
                        scanSuccess = true
                        scope.launch {
                            delay(1500)
                            scanSuccess = false
                        }
                    }
                }

            if (isNfcAvailable && isNfcEnabled && nfcAdapter != null && activity != null) {
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
                if (nfcAdapter != null && activity != null) {
                    nfcAdapter.disableReaderMode(activity)
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header: always visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_nfc),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.nfc_editor_header),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                TextButton(
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
                            nav.pop()
                        }
                    },
                    enabled = displayName.isNotBlank() && uid.isNotBlank() && !isSaving,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                        ),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    } else {
                        Text(text = stringResource(R.string.nfc_editor_save))
                    }
                }
            }

            // Editable fields: always visible
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.nfc_editor_name_label)) },
                placeholder = { Text(stringResource(R.string.nfc_editor_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            OutlinedTextField(
                value = uid,
                onValueChange = { uid = it },
                label = { Text(stringResource(R.string.nfc_editor_uid_label)) },
                placeholder = { Text("04:AB:12:CD:56:78:90") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
            )

            // Dynamic status messages: centered
            if (!isNfcAvailable) {
                Text(
                    text = stringResource(R.string.nfc_editor_not_available),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            } else if (!isNfcEnabled) {
                TextButton(
                    onClick = { configNfc(context) },
                ) {
                    Text(
                        text = stringResource(R.string.nfc_editor_enable_to_autodetect),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.nfc_editor_autodetect_hint),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                if (scanSuccess) {
                    Text(
                        text = stringResource(R.string.nfc_editor_scan_success),
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

fun configNfc(context: Context) {
    Intent(Settings.ACTION_NFC_SETTINGS)
        .apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.takeIf { context.packageManager.resolveActivity(it, 0) != null }
        ?.let { context.startActivity(it) }
}

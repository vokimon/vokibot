package net.canvoki.vokibot

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox

class NfcTriggerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContent {
            NfcActivityScreen(
                intent = intent,
                onBack = { finish() }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the content with the new intent
        setContent {
            NfcActivityScreen(
                intent = intent,
                onBack = { finish() }
            )
        }
    }
}


@Composable
private fun NfcActivityScreen(
    intent: Intent,
    onBack: () -> Unit,
) {
    AppScaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.nfc_trigger_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.nfc_trigger_back)
                        )
                    }
                }
            )
        }
    ) {
        WatermarkBox(
            watermark = painterResource(R.drawable.ic_brand),
        ) {
            NfcUidDisplayScreen(
                intent = intent,
            )
        }
    }
}


@Composable
private fun NfcUidDisplayScreen(
    intent: Intent,
) {
    val uid = remember(intent) { extractUidFromIntent(intent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uid != null) {
            Icon(
                painter = painterResource(R.drawable.ic_nfc),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.nfc_trigger_detected),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uid,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.nfc_trigger_uid_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = stringResource(R.string.nfc_trigger_no_tag),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun extractUidFromIntent(intent: Intent): String? {
    return if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
        intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.id?.joinToString(":") { "%02X".format(it) }
    } else {
        null
    }
}

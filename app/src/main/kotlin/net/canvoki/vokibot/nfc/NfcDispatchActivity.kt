package net.canvoki.vokibot.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox
import net.canvoki.vokibot.FileDataRepository
import net.canvoki.vokibot.R
import net.canvoki.vokibot.common.InputDialog
import net.canvoki.vokibot.common.TriggerDispatcher
import net.canvoki.vokibot.common.editAutomationForTrigger

class NfcDispatchActivity : ComponentActivity() {
    private val currentIntent = mutableStateOf<Intent?>(null)

    private fun extractInfoFromIntent(intent: Intent): String? =
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            val tag =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                }
            tag?.id?.joinToString(":") { "%02X".format(it) }
        } else {
            null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent
        val context = this

        setContent {
            val intent = currentIntent.value ?: return@setContent
            val uid = remember(intent) { extractInfoFromIntent(intent) }
            val triggerId = remember(uid) { uid?.let { NfcTrigger.idFromUid(it) } }
            val repository = remember { FileDataRepository.fromContext(context) }
            var showNameDialog by remember { mutableStateOf(false) }
            val trigger =
                remember(triggerId) {
                    triggerId?.let {
                        repository.trigger.load(triggerId)
                    }
                }

            AppScaffold {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    TriggerDispatcher(
                        triggerId = triggerId,
                        description = trigger?.getTitle(context) ?: uid ?: "???",
                        onDone = { finish() },
                        onCreateAutomation = { id ->
                            editAutomationForTrigger(context = context, id)
                            finish()
                        },
                        onCreateTriggerAndAction = { showNameDialog = true },
                        iconRes = NfcTrigger.iconRes,
                        title = stringResource(R.string.nfc_dispatcher_title),
                    )
                }
            }
            InputDialog(
                show = showNameDialog,
                title = stringResource(R.string.nfc_trigger_name_dialog_title),
                label = stringResource(R.string.nfc_editor_name_label),
                placeholder = stringResource(R.string.nfc_editor_name_placeholder),
                confirmText = stringResource(R.string.nfc_trigger_name_dialog_confirm),
                dismissText = stringResource(R.string.nfc_trigger_name_dialog_cancel),
                onDismiss = { showNameDialog = false },
                onConfirm = { value ->
                    if (uid != null) {
                        val newTrigger =
                            NfcTrigger(
                                uid = uid,
                                displayName = value,
                            )
                        repository.trigger.save(newTrigger)
                        showNameDialog = false
                        editAutomationForTrigger(context = context, newTrigger.id)
                        finish()
                    }
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent
    }
}

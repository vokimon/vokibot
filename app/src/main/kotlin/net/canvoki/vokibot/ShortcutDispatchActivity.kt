package net.canvoki.vokibot

import android.content.Intent
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
import net.canvoki.shared.log
import net.canvoki.vokibot.common.TriggerDispatcher
import net.canvoki.vokibot.common.editAutomationForTrigger

class ShortcutDispatchActivity : ComponentActivity() {
    companion object {
        const val ACTION_TRIGGER = "net.canvoki.vokibot.ACTION_SHORTCUT_TRIGGER"
        const val EXTRA_TRIGGER_ID = "TRIGGER_ID"
    }

    private val currentIntent = mutableStateOf<Intent?>(null)

    fun extractShortcutIdFromIntent(intent: Intent): String? {
        if (intent.action != ACTION_TRIGGER) {
            log("ShortcutDispatchActivity: Wrong action ${intent.action}")
            return null
        }

        val triggerId = intent.getStringExtra(EXTRA_TRIGGER_ID)
        if (triggerId.isNullOrBlank()) {
            log("ShortcutDispatchActivity: Missing trigger ID")
            return null
        }
        return triggerId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent.value = intent
        val context = this

        setContent {
            val intent = currentIntent.value ?: return@setContent
            val uid = remember(intent) { extractShortcutIdFromIntent(intent) }
            val triggerId = remember(uid) { uid?.let { ShortcutTrigger.idFromUid(it) } }
            val repository = remember { FileDataRepository.fromContext(context) }

            AppScaffold {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    TriggerDispatcher(
                        triggerId = triggerId,
                        description = uid ?: "??",
                        onDone = { finish() },
                        onCreateAutomation = { id ->
                            editAutomationForTrigger(context = context, id)
                            finish()
                        },
                        onCreateTriggerAndAction = {
                            if (uid != null) {
                                val newTrigger = ShortcutTrigger.fromExistingShortcut(context, uid)
                                    ?: ShortcutTrigger(
                                        id = uid,
                                        displayName = "TODO",
                                    )
                                repository.trigger.save(newTrigger)
                                editAutomationForTrigger(context = context, newTrigger.id)
                            }
                        },
                        iconRes = ShortcutTrigger.iconRes,
                        badInputError = stringResource(R.string.nfc_trigger_no_tag),
                        searchingText = stringResource(R.string.nfc_trigger_searching),
                        executingText = stringResource(R.string.nfc_trigger_executing),
                        notRegisteredTitle = stringResource(R.string.nfc_trigger_detected),
                        notRegisteredHelp = stringResource(R.string.nfc_trigger_not_registered),
                        noAutomationHelp = stringResource(R.string.nfc_trigger_no_automation),
                        createAutomationText = stringResource(R.string.nfc_trigger_create_automation),
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent
    }

    private fun executeAutomation(triggerId: String): Boolean {
        val repo = FileDataRepository.fromContext(this)
        val trigger = repo.trigger.load(triggerId)
        if (trigger == null) {
            log("ShortcutDispatchActivity: Trigger not found - $triggerId")
            return false
        }
        if (!Automation.executeByTrigger(repo, triggerId, this) {
                runOnUiThread {
                    //finish()
                }
            }
        ) {
            log("ShortcutDispatchActivity: No automations linked to '${trigger.getTitle(this)}'")
            return false
        }
        return true
    }
}

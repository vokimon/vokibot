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
            val trigger = remember(triggerId) {
                triggerId?.let {
                    repository.trigger.load(triggerId)
                        ?: ShortcutTrigger.fromExistingShortcut(context, triggerId)
                }
            }

            AppScaffold {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    TriggerDispatcher(
                        triggerId = triggerId,
                        description = trigger?.getTitle(context) ?: "???",
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
                        badInputError = "Bad shortcut intent",
                        searchingText = "Searching trigger...",
                        executingText = "Executing automation...",
                        notRegisteredTitle = "Shortcut tapped",
                        notRegisteredHelp = "No trigger bound to this shortcut",
                        noAutomationHelp = "No automation bound to the trigger",
                        createAutomationText = "Automate",
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent.value = intent
    }
}

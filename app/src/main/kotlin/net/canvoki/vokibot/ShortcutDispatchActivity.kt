package net.canvoki.vokibot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import net.canvoki.shared.log

sealed class ExecutionState {
    data object Idle : ExecutionState()

    data object Searching : ExecutionState()

    data object Executing : ExecutionState()

    data object NoTrigger : ExecutionState()

    data object NoAutomation : ExecutionState()

    data class Error(
        val message: String,
    ) : ExecutionState()
}

class ShortcutDispatchActivity : ComponentActivity() {
    companion object {
        const val ACTION_TRIGGER = "net.canvoki.vokibot.ACTION_SHORTCUT_TRIGGER"
        const val EXTRA_TRIGGER_ID = "TRIGGER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleTrigger(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleTrigger(intent)
    }

    private fun handleTrigger(intent: Intent) {
        log("ShortcutDispatchActivity: Processing $intent")
        if (intent.action != ACTION_TRIGGER) {
            log("ShortcutDispatchActivity: Wrong action ${intent.action}")
            finish()
            return
        }

        val triggerId = intent.getStringExtra(EXTRA_TRIGGER_ID)
        if (triggerId.isNullOrBlank()) {
            log("ShortcutDispatchActivity: Missing trigger ID")
            finish()
            return
        }

        log("ShortcutDispatchActivity: Trigger tapped - $triggerId")

        try {
            if (executeAutomation(triggerId)) return
        } catch (e: Exception) {
            log("ShortcutDispatchActivity: Failed to process trigger $triggerId: $e")
            finish()
            return
        }
        // TODO: offer to create trigger and/or automation
        finish()
    }

    private fun executeAutomation(triggerId: String): Boolean {
        val repo = FileDataRepository.fromContext(this)
        val trigger = repo.trigger.load(triggerId)
        if (trigger == null) {
            log("ShortcutDispatchActivity: Trigger not found - $triggerId")
            return false
        }
        if (!Automation.executeByTrigger(repo, triggerId, this) {
                runOnUiThread { finish() }
            }
        ) {
            log("ShortcutDispatchActivity: No automations linked to '${trigger.getTitle(this)}'")
            return false
        }
        return true
    }
}

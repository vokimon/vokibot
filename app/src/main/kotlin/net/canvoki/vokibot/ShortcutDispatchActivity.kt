package net.canvoki.vokibot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.canvoki.shared.log

class ShortcutDispatchActivity : ComponentActivity() {
    companion object {
        const val ACTION_TRIGGER = "net.canvoki.vokibot.ACTION_SHORTCUT_TRIGGER"
        const val EXTRA_TRIGGER_ID = "TRIGGER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleTrigger(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleTrigger(intent)
        finish()
    }

    private fun handleTrigger(intent: Intent) {
        if (intent.action != ACTION_TRIGGER) return

        val triggerId = intent.getStringExtra(EXTRA_TRIGGER_ID)
        if (triggerId.isNullOrBlank()) {
            log("ShortcutDispatchActivity: Missing trigger ID")
            return
        }

        log("ShortcutDispatchActivity: Trigger tapped - $triggerId")

        try {
            val repo = FileDataRepository.fromContext(this)
            val trigger = repo.trigger.all().find { it.id == triggerId }
            if (trigger == null) {
                log("ShortcutDispatchActivity: Trigger not found - $triggerId")
                return
            }

            val automations = repo.automation.all().filter { it.triggerId == triggerId }
            if (automations.isEmpty()) {
                log("ShortcutDispatchActivity: No automations linked to '${trigger.title}'")
                return
            }

            val self = this
            log("ShortcutDispatchActivity: Dispatching ${automations.size} automation(s) for '${trigger.title}'")
            CoroutineScope(Dispatchers.IO).launch {
                automations.forEach { automation ->
                    automation.commandIds.forEach { cmdId ->
                        repo.command.load(cmdId)?.execute(self)
                    }
                }
                runOnUiThread {
                    self.finish()
                }
            }
        } catch (e: Exception) {
            log("ShortcutDispatchActivity: Failed to process trigger $triggerId: $e")
        }
    }
}

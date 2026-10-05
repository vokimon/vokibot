package net.canvoki.vokibot.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.canvoki.shared.log
import net.canvoki.vokibot.Automation
import net.canvoki.vokibot.FileDataRepository

class TimeTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val triggerId = TimeScheduler.triggerIdFrom(intent)
        if (triggerId == null) {
            log("TimeTriggerReceiver: missing trigger id in $intent")
            return
        }
        log("TimeTriggerReceiver: alarm fired for $triggerId")
        val repository = FileDataRepository.fromContext(context)
        if (!Automation.executeByTrigger(repository, triggerId, context)) {
            log("TimeTriggerReceiver: No automation for $triggerId")
        }
    }
}

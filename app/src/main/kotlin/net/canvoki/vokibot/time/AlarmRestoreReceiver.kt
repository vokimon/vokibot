package net.canvoki.vokibot.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.canvoki.shared.log

class AlarmRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        log("AlarmRestoreReceiver: restoring alarms (${intent.action})")
        TimeTrigger.scheduleAll(context)
    }
}

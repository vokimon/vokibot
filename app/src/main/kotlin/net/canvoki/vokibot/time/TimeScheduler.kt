package net.canvoki.vokibot.time

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

object TimeScheduler {
    private const val EXTRA_TRIGGER_ID = "trigger_id"

    fun schedule(
        context: Context,
        triggerId: String,
        triggerAt: LocalDateTime,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = newPendingIntent(context, triggerId)
        val triggerAtMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancel(
        context: Context,
        triggerId: String,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        existingPendingIntent(context, triggerId)?.let(alarmManager::cancel)
    }

    fun triggerIdFrom(intent: Intent): String? = intent.getStringExtra(EXTRA_TRIGGER_ID)

    private fun newPendingIntent(
        context: Context,
        triggerId: String,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            triggerId.hashCode(),
            receiverIntent(context, triggerId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun existingPendingIntent(
        context: Context,
        triggerId: String,
    ): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            triggerId.hashCode(),
            receiverIntent(context, triggerId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun receiverIntent(
        context: Context,
        triggerId: String,
    ): Intent = Intent(context, TimeTriggerReceiver::class.java).putExtra(EXTRA_TRIGGER_ID, triggerId)
}

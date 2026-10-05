package net.canvoki.vokibot.time

import android.content.Context
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.EntityMetadata
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.R
import net.canvoki.vokibot.StorableEntity
import net.canvoki.vokibot.Trigger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Triggers on time condition
 */
@Serializable
data class TimeTrigger(
    @Serializable(LocalDateTimeIsoSerializer::class)
    val startAt: LocalDateTime,
    val displayName: String,
    override val id: String = UUID.randomUUID().toString(),
) : Trigger() {
    override val type: String = typeKey

    // Secondary constructor accepts nullable id.
    // Because primary's id: String rejects nullable,
    // Kotlin resolves all id: String? calls to this secondary.
    // Params reordered (id first) to avoid JVM signature clash.
    constructor(
        id: String?,
        startAt: LocalDateTime,
        displayName: String,
    ) : this(
        startAt = startAt,
        displayName = displayName,
        id = id ?: UUID.randomUUID().toString(),
    )

    companion object : EntityMetadata {
        const val DESCRIPTION_FORMAT = "yyyy-MM-dd HH:mm"

        override val typeKey = "trigger_time"
        override val iconRes = R.drawable.ic_schedule
        override val entityClass = TimeTrigger::class
        override val labelRes = R.string.triggerlist_option_time
        override val helpRes = R.string.trigger_time_help

        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val editorFactory = { editingId: String? -> TimeTriggerEditor(editingId) }

        fun fromJson(jsonString: String): TimeTrigger = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
    }

    override val iconRes: Int get() = TimeTrigger.iconRes

    override fun onRemoved(context: Context) {
        TimeScheduler.cancel(context, id)
    }

    fun nextOccurrence(now: LocalDateTime): LocalDateTime? = if (startAt.isAfter(now)) startAt else null

    override fun getTitle(context: Context): String = displayName

    override val description: String get() = startAt.format(DateTimeFormatter.ofPattern(DESCRIPTION_FORMAT))

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

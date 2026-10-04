package net.canvoki.vokibot.time

import android.content.Context
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.NotYetImplementedEditor
import net.canvoki.vokibot.R
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
    val id: String = UUID.randomUUID().toString(),
) {
    val type: String = typeKey

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

    companion object {
        const val DESCRIPTION_FORMAT = "yyyy-MM-dd HH:mm"

        val typeKey = "trigger_time"
        val iconRes = R.drawable.ic_watch
        val entityClass = TimeTrigger::class
        val labelRes = R.string.triggerlist_option_time
        val helpRes = R.string.trigger_time_help

        val deserializer: (String) -> TimeTrigger = { jsonString -> fromJson(jsonString) }
        val editorFactory = { _: String? -> NotYetImplementedEditor }

        fun fromJson(jsonString: String): TimeTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    val iconRes: Int get() = TimeTrigger.iconRes

    fun getTitle(context: Context): String = displayName

    val description: String get() = startAt.format(DateTimeFormatter.ofPattern(DESCRIPTION_FORMAT))

    fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

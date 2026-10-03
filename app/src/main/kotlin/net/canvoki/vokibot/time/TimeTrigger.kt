package net.canvoki.vokibot.time

import kotlinx.serialization.Serializable
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Triggers on time condition
 */
@Serializable
data class TimeTrigger(
    val type: String = typeName,
    @Serializable(LocalDateTimeIsoSerializer::class)
    val startAt: LocalDateTime,
    val displayName: String,
    val id: String = UUID.randomUUID().toString(),
) {
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
        val typeName = "trigger_time"
        const val DESCRIPTION_FORMAT = "yyyy-MM-dd HH:mm"

        fun fromJson(jsonString: String): TimeTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    val iconRes: Int get() = R.drawable.ic_watch

    val description: String get() = startAt.format(DateTimeFormatter.ofPattern(DESCRIPTION_FORMAT))

    fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

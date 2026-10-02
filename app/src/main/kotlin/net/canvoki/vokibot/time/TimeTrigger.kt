package net.canvoki.vokibot.time

import kotlinx.serialization.Serializable
import net.canvoki.vokibot.JsonConfig
import java.time.LocalDateTime
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
    // Secondary constructor accepts nullable id.
    // Because primary's id: String rejects nullable,
    // Kotlin resolves all id: String? calls to this secondary.
    // Params reordered (id first) to avoid JVM signature clash.
    constructor(
        id: String?,
        startAt: LocalDateTime,
        displayName: String,
    ) : this(startAt, displayName, id ?: UUID.randomUUID().toString())

    companion object {
        fun fromJson(jsonString: String): TimeTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

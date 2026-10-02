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
    val id: String = UUID.randomUUID().toString(),
) {
    companion object {
        fun fromJson(jsonString: String): TimeTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

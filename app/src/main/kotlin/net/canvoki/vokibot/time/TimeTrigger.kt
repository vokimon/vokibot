package net.canvoki.vokibot.time

import net.canvoki.vokibot.JsonConfig
import java.util.UUID

/**
 * Triggers on time condition
 */
class TimeTrigger(
    val id: String = UUID.randomUUID().toString(),
) {
    companion object {
        fun fromJson(jsonString: String): TimeTrigger {
            JsonConfig.parseToJsonElement(jsonString)
            return TimeTrigger()
        }
    }

    fun toJson(): String = "{}"
}

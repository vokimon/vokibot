package net.canvoki.vokibot.time

import net.canvoki.vokibot.JsonConfig

/**
 * Triggers on time condition
 */
class TimeTrigger {
    companion object {
        fun fromJson(jsonString: String): TimeTrigger {
            JsonConfig.parseToJsonElement(jsonString)
            return TimeTrigger()
        }
    }

    fun toJson(): String = "{}"
}

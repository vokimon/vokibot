package net.canvoki.vokibot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@SerialName("automation")
data class Automation(
    val name: String,
    val triggerType: String,
    val triggerId: String,
    val commandIds: List<String>,
) : StorableEntity {
    override val id: String
        get() = name.replace(Regex("[^a-zA-Z0-9_.-]"), "_").take(64).ifBlank { "automation" }

    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        fun fromJson(jsonString: String): Automation = json.decodeFromString(serializer(), jsonString)
    }
}

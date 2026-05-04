package net.canvoki.vokibot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
@SerialName("automation")
data class Automation(
    override val id: String,
    val name: String,
    val triggerType: String,
    val triggerId: String,
    val commandIds: List<String>,
) : StorableEntity {
    constructor(
        name: String,
        triggerType: String,
        triggerId: String,
        commandIds: List<String>,
        id: String? = null,
    ) : this(id ?: UUID.randomUUID().toString(), name, triggerType, triggerId, commandIds)

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

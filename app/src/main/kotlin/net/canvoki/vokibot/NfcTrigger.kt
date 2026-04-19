package net.canvoki.vokibot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Represents an NFC tag trigger.
 * @param id Stable identifier used for filename and internal references
 * @param name Human-readable display name for UI lists
 * @param uid The actual NFC tag UID (e.g. "04:AB:12:CD:56:78:90")
 */
@Serializable
@SerialName("nfc_trigger")
data class NfcTrigger(
    val displayName: String,
    val uid: String,
) : StorableEntity {
    // TODO: Hack for non-polymorphic serialization
    val type = "nfc_trigger"

    override val id: String
        get() = toFileSystemId(uid)

    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        fun safeId(id: String) = id

        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        fun fromJson(jsonString: String): NfcTrigger = json.decodeFromString(serializer(), jsonString)
    }
}

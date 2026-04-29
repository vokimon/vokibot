package net.canvoki.vokibot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.canvoki.shared.log

/**
 * Represents an NFC tag trigger.
 * @param id Stable identifier used for filename and internal references
 * @param name Human-readable display name for UI lists
 * @param uid The actual NFC tag UID (e.g. "04:AB:12:CD:56:78:90")
 */
@Serializable
@SerialName(NfcTrigger.TYPE)
data class NfcTrigger(
    val displayName: String,
    val uid: String,
) : Trigger() {
    // TODO: Hack for non-polymorphic serialization
    override val type = NfcTrigger.TYPE

    override val id: String
        get() = toFileSystemId(uid)

    override val title: String
        get() = displayName

    override val description: String
        get() = uid

    override val iconRes: Int
        get() = R.drawable.ic_nfc

    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "trigger_nfc"

        fun safeId(id: String) = id

        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        fun register() {
            Trigger.register(TYPE) { jsonString -> fromJson(jsonString) }
        }

        fun fromJson(jsonString: String): NfcTrigger = json.decodeFromString(serializer(), jsonString)
    }
}

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
@SerialName(NfcTrigger.TYPE)
data class NfcTrigger(
    val displayName: String,
    val uid: String,
) : Trigger() {
    override val type = NfcTrigger.TYPE
    override val id: String get() = idFromUid(uid)
    override val title: String get() = displayName
    override val description: String get() = uid
    override val iconRes: Int get() = R.drawable.ic_nfc

    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "trigger_nfc"
        const val ID_PREFIX = "nfc_"

        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = NfcTrigger::class,
                labelRes = R.string.triggerlist_option_nfc,
                iconRes = R.drawable.ic_nfc,
                editorFactory = { triggerId -> NfcTriggerEditor(triggerId) },
                deserializer = { jsonString -> fromJson(jsonString) },
            )

        fun safeId(id: String) = id

        fun idFromUid(uid: String) = "$ID_PREFIX${toFileSystemId(uid)}"

        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        fun register() {
            Trigger.register(
                typeInfo = TYPE_INFO,
                factory = { jsonString -> fromJson(jsonString) },
            )
        }

        fun fromJson(jsonString: String): NfcTrigger = json.decodeFromString(serializer(), jsonString)
    }
}

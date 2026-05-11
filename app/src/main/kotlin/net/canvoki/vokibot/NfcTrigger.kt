package net.canvoki.vokibot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Triggers when an NFC tag aproaches the device
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
    companion object {
        const val TYPE = "trigger_nfc"
        const val ICON = R.drawable.ic_nfc
        const val ID_PREFIX = "nfc_"

        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = NfcTrigger::class,
                labelRes = R.string.triggerlist_option_nfc,
                iconRes = ICON,
                editorFactory = { triggerId -> NfcTriggerEditor(triggerId) },
                deserializer = { jsonString -> fromJson(jsonString) },
                helpRes = R.string.trigger_nfc_help,
            )

        fun idFromUid(uid: String) = "$ID_PREFIX${toFileSystemId(uid)}"

        fun register() = StorableEntity.register(TYPE_INFO)

        fun fromJson(jsonString: String): NfcTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type: String = TYPE
    override val iconRes: Int get() = ICON
    override val id: String get() = idFromUid(uid)
    override val title: String get() = displayName
    override val description: String get() = uid

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

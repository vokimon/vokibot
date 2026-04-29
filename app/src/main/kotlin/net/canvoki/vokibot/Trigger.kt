package net.canvoki.vokibot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Abstract base for all automation triggers.
 */
@Serializable
abstract class Trigger : StorableEntity {
    abstract val title: String
    abstract val description: String

    @get:DrawableRes
    abstract val iconRes: Int
    abstract val type: String

    companion object {
        private val factories = mutableMapOf<String, (String) -> Trigger>()
        private val typeInfos = mutableMapOf<String, TriggerTypeInfo>()

        private val json =
            Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

        /** Register a trigger type factory for polymorphic deserialization */
        fun register(
            typeKey: String,
            @StringRes labelRes: Int,
            @DrawableRes iconRes: Int,
            factory: (String) -> Trigger,
        ) {
            factories[typeKey] = factory
            typeInfos[typeKey] = TriggerTypeInfo(typeKey, labelRes, iconRes)
        }

        /** Get all registered trigger types for UI listing */
        fun getRegisteredTypes(): List<TriggerTypeInfo> = typeInfos.values.toList()

        /** Deserialize any registered Trigger from JSON */
        fun fromJson(jsonString: String): Trigger {
            val preview = json.decodeFromString<PreviewTrigger>(jsonString)
            val factory = factories[preview.type]
            if (factory == null) {
                log("Unknown trigger type: ${preview.type}")
                return UnknownTrigger(type = preview.type, json = jsonString)
            }
            return factory(jsonString)
        }

        init {
            NfcTrigger.register()
            WidgetTrigger.register()
        }
    }

    @Serializable
    data class PreviewTrigger(
        val type: String,
    )
}

/**
 * Metadata for a registered trigger type, used for UI generation.
 */
data class TriggerTypeInfo(
    val typeKey: String,
    @field:StringRes val labelRes: Int,
    @field:DrawableRes val iconRes: Int,
)

/**
 * Special trigger type for proving trigger types and report missing ones.
 */
@Serializable
data class UnknownTrigger(
    val json: String,
    override val type: String,
) : Trigger() {
    override val id: String = "unknown_${type}_${json.hashCode()}"

    override val title: String = "Unsupported Trigger"

    override val description: String = "`$type` not supported"

    override val iconRes: Int = android.R.drawable.ic_menu_help

    override fun toJson(): String = json
}

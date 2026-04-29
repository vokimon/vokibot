package net.canvoki.vokibot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Represents a Home Screen Widget trigger.
 * @param displayName Human-readable display name for UI lists
 * @param widgetId The Android AppWidget ID assigned by the system
 */
@Serializable
@SerialName(WidgetTrigger.TYPE)
data class WidgetTrigger(
    val displayName: String,
    val widgetId: Int,
) : Trigger() {
    override val type = TYPE
    override val id: String get() = toFileSystemId("widget_$widgetId")
    override val title: String get() = displayName
    override val description: String get() = "Widget ID: $widgetId"
    override val iconRes: Int get() = R.drawable.ic_brand

    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "trigger_widget"

        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        fun register() {
            Trigger.register(
                typeKey = TYPE,
                labelRes = R.string.triggerlist_option_widget,
                iconRes = R.drawable.ic_brand,
            ) { jsonString -> fromJson(jsonString) }
        }

        fun fromJson(jsonString: String): WidgetTrigger = json.decodeFromString(serializer(), jsonString)
    }
}

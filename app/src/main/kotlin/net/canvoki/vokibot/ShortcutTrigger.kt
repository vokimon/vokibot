package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
@SerialName(ShortcutTrigger.TYPE)
data class ShortcutTrigger(
    override val id: String = "${UUID.randomUUID()}",
    val displayName: String,
    val homeScreenIconRes: Int = R.drawable.ic_brand,
) : Trigger() {
    companion object {
        const val MAX_SHORT_LABEL_LENGTH = 10
        const val MAX_LONG_LABEL_LENGTH = 25
        const val TYPE = "trigger_shortcut"
        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        fun register() {
            Trigger.register(
                typeKey = TYPE,
                labelRes = R.string.triggerlist_option_shortcut,
                iconRes = R.drawable.ic_shortcut,
            ) { jsonString -> fromJson(jsonString) }
        }

        fun fromJson(jsonString: String): ShortcutTrigger = json.decodeFromString(serializer(), jsonString)
    }

    override val type = TYPE
    override val title: String get() = displayName
    override val description: String get() = "ID: ${id.takeLast(6)}"
    override val iconRes: Int get() = R.drawable.ic_shortcut

    override fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    fun pin(context: Context) {
        val intent =
            Intent(context, ShortcutDispatchActivity::class.java).apply {
                action = ShortcutDispatchActivity.ACTION_TRIGGER
                putExtra(ShortcutDispatchActivity.EXTRA_TRIGGER_ID, id)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        val shortcut =
            ShortcutInfoCompat
                .Builder(context, id)
                .setShortLabel(title.take(MAX_SHORT_LABEL_LENGTH))
                .setLongLabel(title.take(MAX_LONG_LABEL_LENGTH))
                .setIcon(IconCompat.createWithResource(context, homeScreenIconRes))
                .setIntent(intent)
                .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun update(context: Context) {
        val shortcut =
            ShortcutInfoCompat
                .Builder(context, id)
                .setShortLabel(title.take(MAX_SHORT_LABEL_LENGTH))
                .setLongLabel(title.take(MAX_LONG_LABEL_LENGTH))
                .setIcon(IconCompat.createWithResource(context, homeScreenIconRes))
                .build()
        ShortcutManagerCompat.updateShortcuts(context, listOf(shortcut))
    }

    fun disable(context: Context) {
        ShortcutManagerCompat.disableShortcuts(context, listOf(id), null)
    }
}

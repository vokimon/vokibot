package net.canvoki.vokibot.setting

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.Command
import net.canvoki.vokibot.EntityMetadata
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.R
import net.canvoki.vokibot.StorableEntity
import net.canvoki.vokibot.toFileSystemId
import net.canvoki.vokibot.resolveIntentIcon
import java.util.UUID

/**
 * Command to set a system setting value
 */
@Serializable
data class ChangeSettingCommand(
    override val id: String,
    val key: String,
    val value: String,
) : Command() {
    companion object : EntityMetadata {
        override val typeKey = "change_setting"
        override val entityClass = ChangeSettingCommand::class
        override val labelRes = R.string.command_change_setting_label
        override val iconRes = R.drawable.ic_toggle_on
        override val editorFactory = { id: String? -> ChangeSettingCommandEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_change_setting_help

        fun register() = StorableEntity.register(this)

        fun create(
            key: String,
            value: String,
            id: String? = null,
        ): ChangeSettingCommand =
            ChangeSettingCommand(
                id = id ?: "${typeKey}_${toFileSystemId(UUID.randomUUID().toString())}",
                key = key,
                value = value,
            )

        fun fromJson(jsonString: String): Command = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type = ChangeSettingCommand.typeKey
    override val iconRes = ChangeSettingCommand.iconRes

    override fun getTitle(context: Context): String = SettingSpec.get(key)?.let { context.getString(it.name) } ?: key

    override val description: String get() = value.toString()

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        val spec = SettingSpec.get(key)
        when (spec?.namespace) {
            SettingNamespace.SYSTEM -> Settings.System.putString(context.contentResolver, key, value)
            SettingNamespace.SECURE -> Settings.Secure.putString(context.contentResolver, key, value)
            SettingNamespace.GLOBAL -> Settings.Global.putString(context.contentResolver, key, value)
            null -> throw IllegalArgumentException("Unsupported setting: $key")
        }
    }

    override fun loadIcon(context: Context): Drawable =
        resolveIntentIcon(context, Intent(key)) ?: context.getDrawable(iconRes)!!
}

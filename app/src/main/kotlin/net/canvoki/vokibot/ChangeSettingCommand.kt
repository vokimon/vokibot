package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Command to set a system setting value
 */
@Serializable
data class ChangeSettingCommand(
    override val id: String,
    val key: String,
    val value: ExtraValue,
) : Command() {
    constructor(
        key: String,
        value: ExtraValue,
        id: String?,
    ) : this(
        id = id ?: "${typeKey}_${toFileSystemId(UUID.randomUUID().toString())}",
        key = key,
        value = value,
    )

    companion object : EntityMetadata {
        override val typeKey = "change_setting"
        override val entityClass = ChangeSettingCommand::class
        override val labelRes = R.string.command_change_setting_label
        override val iconRes = R.drawable.ic_toggle_on
        override val editorFactory = { id: String? -> ChangeSettingCommandEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_change_setting_help

        fun register() = StorableEntity.register(this)

        fun fromJson(jsonString: String): Command = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type = ChangeSettingCommand.typeKey
    override val iconRes = ChangeSettingCommand.iconRes

    override fun getTitle(context: Context): String = SettingSpec.get(key)?.let { context.getString(it.name) } ?: key

    override val description: String get() = key

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        val spec =
            SettingSpec.get(key)
                ?: throw IllegalArgumentException("Unsupported setting: $key")
        val stored = spec.type.toStoredSettingValue(value)
        log(stored)
        Settings.System.putString(
            context.contentResolver,
            key,
            stored,
        )
    }

    override fun loadIcon(context: Context): Drawable =
        resolveIntentIcon(context, Intent(key)) ?: context.getDrawable(iconRes)!!
}

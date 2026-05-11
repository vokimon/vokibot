package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.serialization.Serializable

/**
 * Command to open a settings page.
 * @param pageId the id of the page
 */
@Serializable
data class SettingsPageCommand(
    val pageId: String,
) : Command() {
    companion object : EntityMetadata {
        override val typeKey = "settings_page"
        override val entityClass = SettingsPageCommand::class
        override val labelRes = R.string.command_type_settings_page
        override val iconRes = R.drawable.ic_settings
        override val editorFactory = { pageId: String? -> SettingsPageCommandEditor(pageId) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_settings_page_help

        fun register() = StorableEntity.register(this)

        fun fromJson(jsonString: String): Command = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type = SettingsPageCommand.typeKey
    override val iconRes = SettingsPageCommand.iconRes
    override val id: String get() = "${type}_${toFileSystemId(pageId)}"
    override val title: String get() = pageName
    override val description: String get() = pageId

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    private val pageName: String
        get() =
            when (pageId) {
                Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS -> "Development"
                else -> pageId
            }

    override suspend fun execute(context: Context) {
        context.startActivity(Intent(pageId))
    }
}

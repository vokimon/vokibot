package net.canvoki.vokibot.settingspage

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.Command
import net.canvoki.vokibot.EntityMetadata
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.R
import net.canvoki.vokibot.StorableEntity
import net.canvoki.vokibot.apps.resolveIntentIcon
import net.canvoki.vokibot.toFileSystemId

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

    override fun getTitle(context: Context): String {
        val page = SETTINGS_PAGES.find { it.id == pageId }
        return page?.nameRes?.takeIf { it != 0 }?.let { context.getString(it) }
            ?: pageId
    }

    override val description: String get() = pageId

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        context.startActivity(Intent(pageId))
    }

    override fun loadIcon(context: Context): Drawable =
        resolveIntentIcon(context, Intent(pageId)) ?: context.getDrawable(iconRes)!!
}

package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Command to open a settings page.
 * @param pageId the id of the page
 */
@Serializable
@SerialName(SettingsPageCommand.TYPE)
data class SettingsPageCommand(
    val pageId: String,
) : Command() {
    companion object {
        const val TYPE = "settings_page"
        const val ICON = R.drawable.ic_settings

        val TYPE_INFO =
            EntityTypeInfo(
                entityClass = SettingsPageCommand::class,
                typeKey = TYPE,
                iconRes = ICON,
                labelRes = R.string.command_type_settings_page,
                editorFactory = { SettingsPageCommandEditor(it) },
                deserializer = { jsonString -> fromJson(jsonString) },
            )

        fun register() = StorableEntity.register(TYPE_INFO)

        fun fromJson(jsonString: String): Command = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type: String = TYPE
    override val iconRes: Int = ICON
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

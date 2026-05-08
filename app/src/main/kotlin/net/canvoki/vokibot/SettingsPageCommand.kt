package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(SettingsPageCommand.TYPE)
data class SettingsPageCommand(
    val pageId: String,
) : Command() {
    override val type: String = TYPE
    override val id: String get() = "${type}_${toFileSystemId(pageId)}"
    override val title: String get() = pageName
    override val description: String get() = pageId
    override val iconRes: Int = R.drawable.ic_settings

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

    companion object {
        const val TYPE = "settings_page"
        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = SettingsPageCommand::class,
                labelRes = R.string.command_type_settings_page,
                iconRes = R.drawable.ic_brand,
                editorFactory = { SettingsPageCommandEditor(it) },
                deserializer = { jsonString -> fromJson(jsonString) },
            )

        fun register() = StorableEntity.register(TYPE_INFO)

        fun fromJson(jsonString: String): Command = JsonConfig.decodeFromString(serializer(), jsonString)
    }
}

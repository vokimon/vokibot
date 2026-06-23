package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Command to set a system setting value
 */
@Serializable
data class ChangeSettingValueCommand(
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
        override val entityClass = ChangeSettingValueCommand::class
        override val labelRes = R.string.command_change_setting_value_label
        override val iconRes = R.drawable.ic_toggle_on
        override val editorFactory = { id: String? -> ChangeSettingValueCommandEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_change_setting_value_help

        fun register() = StorableEntity.register(this)

        fun fromJson(jsonString: String): Command = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type = ChangeSettingValueCommand.typeKey
    override val iconRes = ChangeSettingValueCommand.iconRes

    override fun getTitle(context: Context): String {
        val setting = SETTING_VALUES.find { it.id == key }
        return setting?.nameRes?.takeIf { it != 0 }?.let { context.getString(it) }
            ?: key
    }

    override val description: String get() = key

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        log(value.toStoredSettingValue())
        Settings.System.putString(
            context.contentResolver,
            key,
            value.toStoredSettingValue(),
        )
    }

    override fun loadIcon(context: Context): Drawable =
        resolveIntentIcon(context, Intent(key)) ?: context.getDrawable(iconRes)!!
}

data class SettingValue(
    val id: String,
    val categoryId: SettingCategory,
    val isMain: Boolean,
    @get:StringRes val nameRes: Int = 0,
) {
    @get:StringRes
    val labelRes: Int get() = categoryId.labelRes

    fun isAvailable(context: Context): Boolean = context.packageManager.resolveActivity(Intent(id), 0) != null
}

enum class SettingCategory {
    PANELS,
    NETWORK,
    DISPLAY,
    SOUND,
    APPS,
    SECURITY,
    PERSONAL,
    SYSTEM,
    ;

    @get:StringRes
    val labelRes: Int get() =
        when (this) {
            PANELS -> R.string.settings_page_category_panels
            NETWORK -> R.string.settings_page_category_network
            DISPLAY -> R.string.settings_page_category_display
            SOUND -> R.string.settings_page_category_sound
            APPS -> R.string.settings_page_category_apps
            SECURITY -> R.string.settings_page_category_security
            PERSONAL -> R.string.settings_page_category_personal
            SYSTEM -> R.string.settings_page_category_system
        }
}

val SETTING_VALUES: List<SettingValue> =
    listOf(
        // Panels
        SettingValue(
            Settings.Panel.ACTION_WIFI,
            SettingCategory.PANELS,
            true,
            R.string.settings_page_name_quick_wifi,
        ),
    )

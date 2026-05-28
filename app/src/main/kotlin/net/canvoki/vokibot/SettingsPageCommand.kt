package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.annotation.StringRes
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
        get() = SETTINGS_PAGES.find { it.id == pageId }?.name ?: pageId

    override suspend fun execute(context: Context) {
        context.startActivity(Intent(pageId))
    }

    override fun loadIcon(context: Context): Drawable =
        resolveIntentIcon(context, Intent(pageId)) ?: context.getDrawable(iconRes)!!
}

data class SettingsPage(
    val id: String,
    val name: String,
    val categoryId: SettingsPageCategory,
    val isMain: Boolean,
    @get:StringRes val nameRes: Int = 0,
) {
    @get:StringRes
    val labelRes: Int get() = categoryId.labelRes
}

enum class SettingsPageCategory {
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
            NETWORK -> R.string.settings_page_category_network
            DISPLAY -> R.string.settings_page_category_display
            SOUND -> R.string.settings_page_category_sound
            APPS -> R.string.settings_page_category_apps
            SECURITY -> R.string.settings_page_category_security
            PERSONAL -> R.string.settings_page_category_personal
            SYSTEM -> R.string.settings_page_category_system
        }
}

val SETTINGS_PAGES: List<SettingsPage> =
    listOf(
        // Network
        SettingsPage(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi", SettingsPageCategory.NETWORK, true),
        SettingsPage(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth", SettingsPageCategory.NETWORK, true),
        SettingsPage(Settings.ACTION_NFC_SETTINGS, "NFC", SettingsPageCategory.NETWORK, true),
        SettingsPage(Settings.ACTION_NETWORK_OPERATOR_SETTINGS, "Mobile networks", SettingsPageCategory.NETWORK, true),
        SettingsPage(Settings.ACTION_WIRELESS_SETTINGS, "Internet", SettingsPageCategory.NETWORK, true),
        SettingsPage(Settings.ACTION_AIRPLANE_MODE_SETTINGS, "Airplane mode", SettingsPageCategory.NETWORK, false),
        // OEM
        SettingsPage(Settings.ACTION_DATA_USAGE_SETTINGS, "Data usage", SettingsPageCategory.NETWORK, false),
        SettingsPage(Settings.ACTION_APN_SETTINGS, "APN", SettingsPageCategory.NETWORK, false), // OEM
        // Display
        SettingsPage(Settings.ACTION_DISPLAY_SETTINGS, "Display", SettingsPageCategory.DISPLAY, true),
        SettingsPage(Settings.ACTION_NIGHT_DISPLAY_SETTINGS, "Night light", SettingsPageCategory.DISPLAY, true),
        SettingsPage("android.settings.DARK_THEME_SETTINGS", "Dark theme", SettingsPageCategory.DISPLAY, true),
        SettingsPage(Settings.ACTION_AUTO_ROTATE_SETTINGS, "Auto-rotate", SettingsPageCategory.DISPLAY, false),
        SettingsPage("android.settings.FONT_SIZE_SETTINGS", "Font size", SettingsPageCategory.DISPLAY, false), // OEM
        SettingsPage("android.settings.WALLPAPER_SETTINGS", "Wallpaper", SettingsPageCategory.DISPLAY, false),
        SettingsPage(
            "android.settings.LIVE_DISPLAY_SETTINGS",
            "Display color temperature",
            SettingsPageCategory.DISPLAY,
            false,
        ), // OEM
        // Sound
        SettingsPage(Settings.ACTION_SOUND_SETTINGS, "Sound", SettingsPageCategory.SOUND, true),
        SettingsPage(
            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
            "Do Not Disturb",
            SettingsPageCategory.SOUND,
            false,
        ),
        SettingsPage(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS, "Zen mode", SettingsPageCategory.SOUND, false),
        // Apps
        SettingsPage(Settings.ACTION_APPLICATION_SETTINGS, "Apps", SettingsPageCategory.APPS, true),
        SettingsPage(Settings.ACTION_APP_NOTIFICATION_SETTINGS, "App notifications", SettingsPageCategory.APPS, false),
        SettingsPage(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "App permissions", SettingsPageCategory.APPS, false),
        SettingsPage(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS, "All apps", SettingsPageCategory.APPS, false),
        SettingsPage(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, "Installed apps", SettingsPageCategory.APPS, false),
        SettingsPage(
            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
            "Developer options",
            SettingsPageCategory.APPS,
            true,
        ),
        // Security
        SettingsPage(Settings.ACTION_SECURITY_SETTINGS, "Security", SettingsPageCategory.SECURITY, true),
        SettingsPage(Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location", SettingsPageCategory.SECURITY, true),
        SettingsPage("android.settings.LOCK_SCREEN_SETTINGS", "Lock screen", SettingsPageCategory.SECURITY, false),
        SettingsPage(Settings.ACTION_PRIVACY_SETTINGS, "Privacy", SettingsPageCategory.SECURITY, false),
        SettingsPage("android.settings.CREDENTIAL_SETTINGS", "Credentials", SettingsPageCategory.SECURITY, false),
        // OEM
        SettingsPage(
            "android.settings.TRUSTED_CREDENTIALS_SETTINGS",
            "Trusted certificates",
            SettingsPageCategory.SECURITY,
            false,
        ),
        // Personal
        SettingsPage(Settings.ACTION_DATE_SETTINGS, "Date & time", SettingsPageCategory.PERSONAL, true),
        SettingsPage(Settings.ACTION_LOCALE_SETTINGS, "Language", SettingsPageCategory.PERSONAL, true),
        SettingsPage("android.settings.USER_SETTINGS", "Users", SettingsPageCategory.PERSONAL, false),
        SettingsPage("android.settings.ACCOUNT_SYNC_SETTINGS", "Accounts", SettingsPageCategory.PERSONAL, false),
        SettingsPage(Settings.ACTION_SYNC_SETTINGS, "Sync", SettingsPageCategory.PERSONAL, false),
        SettingsPage(Settings.ACTION_VOICE_INPUT_SETTINGS, "Voice input", SettingsPageCategory.PERSONAL, false),
        // System
        SettingsPage(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility", SettingsPageCategory.SYSTEM, true),
        SettingsPage(Settings.ACTION_DEVICE_INFO_SETTINGS, "About device", SettingsPageCategory.SYSTEM, false),
        SettingsPage(Settings.ACTION_MEMORY_CARD_SETTINGS, "Storage", SettingsPageCategory.SYSTEM, false),
        SettingsPage(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "Internal storage", SettingsPageCategory.SYSTEM, false),
        SettingsPage(Settings.ACTION_BATTERY_SAVER_SETTINGS, "Battery", SettingsPageCategory.SYSTEM, false),
        SettingsPage("android.settings.NOTIFICATION_SETTINGS", "Notifications", SettingsPageCategory.SYSTEM, false),
        SettingsPage(Settings.ACTION_SETTINGS, "All settings", SettingsPageCategory.SYSTEM, false),
        SettingsPage(Settings.ACTION_QUICK_LAUNCH_SETTINGS, "Quick launch", SettingsPageCategory.SYSTEM, false), // OEM
    )

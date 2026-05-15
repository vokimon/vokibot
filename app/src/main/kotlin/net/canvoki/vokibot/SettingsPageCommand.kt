package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
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
    val category: String,
    val isMain: Boolean,
)

val SETTINGS_PAGES: List<SettingsPage> =
    listOf(
        // Network
        SettingsPage(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi", "Network", true),
        SettingsPage(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth", "Network", true),
        SettingsPage(Settings.ACTION_NFC_SETTINGS, "NFC", "Network", true),
        SettingsPage(Settings.ACTION_NETWORK_OPERATOR_SETTINGS, "Mobile networks", "Network", true),
        SettingsPage(Settings.ACTION_WIRELESS_SETTINGS, "Internet", "Network", true),
        SettingsPage(Settings.ACTION_AIRPLANE_MODE_SETTINGS, "Airplane mode", "Network", false), // OEM
        SettingsPage(Settings.ACTION_DATA_USAGE_SETTINGS, "Data usage", "Network", false),
        SettingsPage(Settings.ACTION_APN_SETTINGS, "APN", "Network", false), // OEM
        // Display
        SettingsPage(Settings.ACTION_DISPLAY_SETTINGS, "Display", "Display", true),
        SettingsPage(Settings.ACTION_NIGHT_DISPLAY_SETTINGS, "Night light", "Display", true),
        SettingsPage("android.settings.DARK_THEME_SETTINGS", "Dark theme", "Display", true),
        SettingsPage(Settings.ACTION_AUTO_ROTATE_SETTINGS, "Auto-rotate", "Display", false),
        SettingsPage("android.settings.FONT_SIZE_SETTINGS", "Font size", "Display", false), // OEM
        SettingsPage("android.settings.WALLPAPER_SETTINGS", "Wallpaper", "Display", false),
        SettingsPage("android.settings.LIVE_DISPLAY_SETTINGS", "Display color temperature", "Display", false), // OEM
        // Sound
        SettingsPage(Settings.ACTION_SOUND_SETTINGS, "Sound", "Sound", true),
        SettingsPage(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, "Do Not Disturb", "Sound", false),
        SettingsPage(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS, "Zen mode", "Sound", false),
        // Apps
        SettingsPage(Settings.ACTION_APPLICATION_SETTINGS, "Apps", "Apps", true),
        SettingsPage(Settings.ACTION_APP_NOTIFICATION_SETTINGS, "App notifications", "Apps", false),
        SettingsPage(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "App permissions", "Apps", false),
        SettingsPage(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS, "All apps", "Apps", false),
        SettingsPage(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, "Installed apps", "Apps", false),
        SettingsPage(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, "Developer options", "Apps", true),
        // Security
        SettingsPage(Settings.ACTION_SECURITY_SETTINGS, "Security", "Security", true),
        SettingsPage(Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location", "Security", true),
        SettingsPage("android.settings.LOCK_SCREEN_SETTINGS", "Lock screen", "Security", false),
        SettingsPage(Settings.ACTION_PRIVACY_SETTINGS, "Privacy", "Security", false),
        SettingsPage("android.settings.CREDENTIAL_SETTINGS", "Credentials", "Security", false), // OEM
        SettingsPage("android.settings.TRUSTED_CREDENTIALS_SETTINGS", "Trusted certificates", "Security", false),
        // Personal
        SettingsPage(Settings.ACTION_DATE_SETTINGS, "Date & time", "Personal", true),
        SettingsPage(Settings.ACTION_LOCALE_SETTINGS, "Language", "Personal", true),
        SettingsPage("android.settings.USER_SETTINGS", "Users", "Personal", false),
        SettingsPage("android.settings.ACCOUNT_SYNC_SETTINGS", "Accounts", "Personal", false),
        SettingsPage(Settings.ACTION_SYNC_SETTINGS, "Sync", "Personal", false),
        SettingsPage(Settings.ACTION_VOICE_INPUT_SETTINGS, "Voice input", "Personal", false),
        // System
        SettingsPage(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility", "System", true),
        SettingsPage(Settings.ACTION_DEVICE_INFO_SETTINGS, "About device", "System", false),
        SettingsPage(Settings.ACTION_MEMORY_CARD_SETTINGS, "Storage", "System", false),
        SettingsPage(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "Internal storage", "System", false),
        SettingsPage(Settings.ACTION_BATTERY_SAVER_SETTINGS, "Battery", "System", false),
        SettingsPage("android.settings.NOTIFICATION_SETTINGS", "Notifications", "System", false),
        SettingsPage(Settings.ACTION_SETTINGS, "All settings", "System", false),
        SettingsPage(Settings.ACTION_QUICK_LAUNCH_SETTINGS, "Quick launch", "System", false), // OEM
    )

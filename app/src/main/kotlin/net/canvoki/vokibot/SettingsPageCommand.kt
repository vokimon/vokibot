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
    val category: String,
    val isMain: Boolean,
    @get:StringRes val nameRes: Int = 0,
) {
    @get:StringRes
    val categoryRes: Int get() = categoryResFor(category)
}

@StringRes
fun categoryResFor(category: String): Int =
    when (category) {
        SettingsPageCat.NETWORK -> R.string.settings_page_category_network
        SettingsPageCat.DISPLAY -> R.string.settings_page_category_display
        SettingsPageCat.SOUND -> R.string.settings_page_category_sound
        SettingsPageCat.APPS -> R.string.settings_page_category_apps
        SettingsPageCat.SECURITY -> R.string.settings_page_category_security
        SettingsPageCat.PERSONAL -> R.string.settings_page_category_personal
        SettingsPageCat.SYSTEM -> R.string.settings_page_category_system
        else -> 0
    }

object SettingsPageCat {
    const val NETWORK = "Network"
    const val DISPLAY = "Display"
    const val SOUND = "Sound"
    const val APPS = "Apps"
    const val SECURITY = "Security"
    const val PERSONAL = "Personal"
    const val SYSTEM = "System"
}

val SETTINGS_PAGES: List<SettingsPage> =
    listOf(
        // Network
        SettingsPage(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi", SettingsPageCat.NETWORK, true),
        SettingsPage(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth", SettingsPageCat.NETWORK, true),
        SettingsPage(Settings.ACTION_NFC_SETTINGS, "NFC", SettingsPageCat.NETWORK, true),
        SettingsPage(Settings.ACTION_NETWORK_OPERATOR_SETTINGS, "Mobile networks", SettingsPageCat.NETWORK, true),
        SettingsPage(Settings.ACTION_WIRELESS_SETTINGS, "Internet", SettingsPageCat.NETWORK, true),
        SettingsPage(Settings.ACTION_AIRPLANE_MODE_SETTINGS, "Airplane mode", SettingsPageCat.NETWORK, false), // OEM
        SettingsPage(Settings.ACTION_DATA_USAGE_SETTINGS, "Data usage", SettingsPageCat.NETWORK, false),
        SettingsPage(Settings.ACTION_APN_SETTINGS, "APN", SettingsPageCat.NETWORK, false), // OEM
        // Display
        SettingsPage(Settings.ACTION_DISPLAY_SETTINGS, "Display", SettingsPageCat.DISPLAY, true),
        SettingsPage(Settings.ACTION_NIGHT_DISPLAY_SETTINGS, "Night light", SettingsPageCat.DISPLAY, true),
        SettingsPage("android.settings.DARK_THEME_SETTINGS", "Dark theme", SettingsPageCat.DISPLAY, true),
        SettingsPage(Settings.ACTION_AUTO_ROTATE_SETTINGS, "Auto-rotate", SettingsPageCat.DISPLAY, false),
        SettingsPage("android.settings.FONT_SIZE_SETTINGS", "Font size", SettingsPageCat.DISPLAY, false), // OEM
        SettingsPage("android.settings.WALLPAPER_SETTINGS", "Wallpaper", SettingsPageCat.DISPLAY, false),
        SettingsPage(
            "android.settings.LIVE_DISPLAY_SETTINGS",
            "Display color temperature",
            SettingsPageCat.DISPLAY,
            false,
        ), // OEM
        // Sound
        SettingsPage(Settings.ACTION_SOUND_SETTINGS, "Sound", SettingsPageCat.SOUND, true),
        SettingsPage(
            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
            "Do Not Disturb",
            SettingsPageCat.SOUND,
            false,
        ),
        SettingsPage(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS, "Zen mode", SettingsPageCat.SOUND, false),
        // Apps
        SettingsPage(Settings.ACTION_APPLICATION_SETTINGS, "Apps", SettingsPageCat.APPS, true),
        SettingsPage(Settings.ACTION_APP_NOTIFICATION_SETTINGS, "App notifications", SettingsPageCat.APPS, false),
        SettingsPage(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "App permissions", SettingsPageCat.APPS, false),
        SettingsPage(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS, "All apps", SettingsPageCat.APPS, false),
        SettingsPage(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, "Installed apps", SettingsPageCat.APPS, false),
        SettingsPage(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, "Developer options", SettingsPageCat.APPS, true),
        // Security
        SettingsPage(Settings.ACTION_SECURITY_SETTINGS, "Security", SettingsPageCat.SECURITY, true),
        SettingsPage(Settings.ACTION_LOCATION_SOURCE_SETTINGS, "Location", SettingsPageCat.SECURITY, true),
        SettingsPage("android.settings.LOCK_SCREEN_SETTINGS", "Lock screen", SettingsPageCat.SECURITY, false),
        SettingsPage(Settings.ACTION_PRIVACY_SETTINGS, "Privacy", SettingsPageCat.SECURITY, false),
        SettingsPage("android.settings.CREDENTIAL_SETTINGS", "Credentials", SettingsPageCat.SECURITY, false), // OEM
        SettingsPage(
            "android.settings.TRUSTED_CREDENTIALS_SETTINGS",
            "Trusted certificates",
            SettingsPageCat.SECURITY,
            false,
        ),
        // Personal
        SettingsPage(Settings.ACTION_DATE_SETTINGS, "Date & time", SettingsPageCat.PERSONAL, true),
        SettingsPage(Settings.ACTION_LOCALE_SETTINGS, "Language", SettingsPageCat.PERSONAL, true),
        SettingsPage("android.settings.USER_SETTINGS", "Users", SettingsPageCat.PERSONAL, false),
        SettingsPage("android.settings.ACCOUNT_SYNC_SETTINGS", "Accounts", SettingsPageCat.PERSONAL, false),
        SettingsPage(Settings.ACTION_SYNC_SETTINGS, "Sync", SettingsPageCat.PERSONAL, false),
        SettingsPage(Settings.ACTION_VOICE_INPUT_SETTINGS, "Voice input", SettingsPageCat.PERSONAL, false),
        // System
        SettingsPage(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility", SettingsPageCat.SYSTEM, true),
        SettingsPage(Settings.ACTION_DEVICE_INFO_SETTINGS, "About device", SettingsPageCat.SYSTEM, false),
        SettingsPage(Settings.ACTION_MEMORY_CARD_SETTINGS, "Storage", SettingsPageCat.SYSTEM, false),
        SettingsPage(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "Internal storage", SettingsPageCat.SYSTEM, false),
        SettingsPage(Settings.ACTION_BATTERY_SAVER_SETTINGS, "Battery", SettingsPageCat.SYSTEM, false),
        SettingsPage("android.settings.NOTIFICATION_SETTINGS", "Notifications", SettingsPageCat.SYSTEM, false),
        SettingsPage(Settings.ACTION_SETTINGS, "All settings", SettingsPageCat.SYSTEM, false),
        SettingsPage(Settings.ACTION_QUICK_LAUNCH_SETTINGS, "Quick launch", SettingsPageCat.SYSTEM, false), // OEM
    )

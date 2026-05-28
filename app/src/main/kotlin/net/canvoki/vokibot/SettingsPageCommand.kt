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

    fun isAvailable(context: Context): Boolean = context.packageManager.resolveActivity(Intent(id), 0) != null
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
        SettingsPage(
            Settings.ACTION_WIFI_SETTINGS,
            "Wi-Fi",
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_wifi,
        ),
        SettingsPage(
            Settings.ACTION_BLUETOOTH_SETTINGS,
            "Bluetooth",
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_bluetooth,
        ),
        SettingsPage(
            Settings.ACTION_NFC_SETTINGS,
            "NFC",
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_nfc,
        ),
        SettingsPage(
            Settings.ACTION_NETWORK_OPERATOR_SETTINGS,
            "Mobile networks",
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_mobile_networks,
        ),
        SettingsPage(
            Settings.ACTION_WIRELESS_SETTINGS,
            "Internet",
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_internet,
        ),
        SettingsPage(
            Settings.ACTION_AIRPLANE_MODE_SETTINGS,
            "Airplane mode",
            SettingsPageCategory.NETWORK,
            false,
            R.string.settings_page_name_airplane_mode,
        ),
        // OEM
        SettingsPage(
            Settings.ACTION_DATA_USAGE_SETTINGS,
            "Data usage",
            SettingsPageCategory.NETWORK,
            false,
            R.string.settings_page_name_data_usage,
        ),
        SettingsPage(
            Settings.ACTION_APN_SETTINGS,
            "APN",
            SettingsPageCategory.NETWORK,
            false,
            R.string.settings_page_name_apn,
        ), // OEM
        // Display
        SettingsPage(
            Settings.ACTION_DISPLAY_SETTINGS,
            "Display",
            SettingsPageCategory.DISPLAY,
            true,
            R.string.settings_page_name_display,
        ),
        SettingsPage(
            Settings.ACTION_NIGHT_DISPLAY_SETTINGS,
            "Night light",
            SettingsPageCategory.DISPLAY,
            true,
            R.string.settings_page_name_night_light,
        ),
        SettingsPage(
            "android.settings.DARK_THEME_SETTINGS",
            "Dark theme",
            SettingsPageCategory.DISPLAY,
            true,
            R.string.settings_page_name_dark_theme,
        ),
        SettingsPage(
            Settings.ACTION_AUTO_ROTATE_SETTINGS,
            "Auto-rotate",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_auto_rotate,
        ),
        SettingsPage(
            "android.settings.FONT_SIZE_SETTINGS",
            "Font size",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_font_size,
        ), // OEM
        SettingsPage(
            "android.settings.WALLPAPER_SETTINGS",
            "Wallpaper",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_wallpaper,
        ),
        SettingsPage(
            "android.settings.LIVE_DISPLAY_SETTINGS",
            "Display color temperature",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_display_color_temperature,
        ), // OEM
        // Sound
        SettingsPage(
            Settings.ACTION_SOUND_SETTINGS,
            "Sound",
            SettingsPageCategory.SOUND,
            true,
            R.string.settings_page_name_sound,
        ),
        SettingsPage(
            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
            "Do Not Disturb",
            SettingsPageCategory.SOUND,
            false,
            R.string.settings_page_name_do_not_disturb,
        ),
        SettingsPage(
            Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS,
            "Zen mode",
            SettingsPageCategory.SOUND,
            false,
            R.string.settings_page_name_zen_mode,
        ),
        // Apps
        SettingsPage(
            Settings.ACTION_APPLICATION_SETTINGS,
            "Apps",
            SettingsPageCategory.APPS,
            true,
            R.string.settings_page_name_apps,
        ),
        SettingsPage(
            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
            "App notifications",
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_app_notifications,
        ),
        SettingsPage(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "App permissions",
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_app_permissions,
        ),
        SettingsPage(
            Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS,
            "All apps",
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_all_apps,
        ),
        SettingsPage(
            Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
            "Installed apps",
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_installed_apps,
        ),
        SettingsPage(
            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
            "Developer options",
            SettingsPageCategory.APPS,
            true,
            R.string.settings_page_name_developer_options,
        ),
        // Security
        SettingsPage(
            Settings.ACTION_SECURITY_SETTINGS,
            "Security",
            SettingsPageCategory.SECURITY,
            true,
            R.string.settings_page_name_security,
        ),
        SettingsPage(
            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            "Location",
            SettingsPageCategory.SECURITY,
            true,
            R.string.settings_page_name_location,
        ),
        SettingsPage(
            "android.settings.LOCK_SCREEN_SETTINGS",
            "Lock screen",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_lock_screen,
        ),
        SettingsPage(
            Settings.ACTION_PRIVACY_SETTINGS,
            "Privacy",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_privacy,
        ),
        SettingsPage(
            "android.settings.CREDENTIAL_SETTINGS",
            "Credentials",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_credentials,
        ),
        // OEM
        SettingsPage(
            "android.settings.TRUSTED_CREDENTIALS_SETTINGS",
            "Trusted certificates",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_trusted_certificates,
        ),
        // Personal
        SettingsPage(
            Settings.ACTION_DATE_SETTINGS,
            "Date & time",
            SettingsPageCategory.PERSONAL,
            true,
            R.string.settings_page_name_date_time,
        ),
        SettingsPage(
            Settings.ACTION_LOCALE_SETTINGS,
            "Language",
            SettingsPageCategory.PERSONAL,
            true,
            R.string.settings_page_name_language,
        ),
        SettingsPage(
            "android.settings.USER_SETTINGS",
            "Users",
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_users,
        ),
        SettingsPage(
            "android.settings.ACCOUNT_SYNC_SETTINGS",
            "Accounts",
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_accounts,
        ),
        SettingsPage(
            Settings.ACTION_SYNC_SETTINGS,
            "Sync",
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_sync,
        ),
        SettingsPage(
            Settings.ACTION_VOICE_INPUT_SETTINGS,
            "Voice input",
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_voice_input,
        ),
        // System
        SettingsPage(
            Settings.ACTION_ACCESSIBILITY_SETTINGS,
            "Accessibility",
            SettingsPageCategory.SYSTEM,
            true,
            R.string.settings_page_name_accessibility,
        ),
        SettingsPage(
            Settings.ACTION_DEVICE_INFO_SETTINGS,
            "About device",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_about_device,
        ),
        SettingsPage(
            Settings.ACTION_MEMORY_CARD_SETTINGS,
            "Storage",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_storage,
        ),
        SettingsPage(
            Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            "Internal storage",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_internal_storage,
        ),
        SettingsPage(
            Settings.ACTION_BATTERY_SAVER_SETTINGS,
            "Battery",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_battery,
        ),
        SettingsPage(
            "android.settings.NOTIFICATION_SETTINGS",
            "Notifications",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_notifications,
        ),
        SettingsPage(
            Settings.ACTION_SETTINGS,
            "All settings",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_all_settings,
        ),
        SettingsPage(
            Settings.ACTION_QUICK_LAUNCH_SETTINGS,
            "Quick launch",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_quick_launch,
        ), // OEM
    )

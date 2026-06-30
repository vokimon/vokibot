package net.canvoki.vokibot.settingspage

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import net.canvoki.vokibot.R


data class SettingsPage(
    val id: String,
    val categoryId: SettingsPageCategory,
    val isMain: Boolean,
    @get:StringRes val nameRes: Int = 0,
) {
    @get:StringRes
    val labelRes: Int get() = categoryId.labelRes

    fun isAvailable(context: Context): Boolean =
        context.packageManager.resolveActivity(Intent(id), 0) != null
}

enum class SettingsPageCategory {
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

val SETTINGS_PAGES: List<SettingsPage> =
    listOf(
        // Panels
        SettingsPage(
            Settings.Panel.ACTION_WIFI,
            SettingsPageCategory.PANELS,
            true,
            R.string.settings_page_name_quick_wifi,
        ),
        SettingsPage(
            Settings.Panel.ACTION_NFC,
            SettingsPageCategory.PANELS,
            true,
            R.string.settings_page_name_quick_nfc,
        ),
        SettingsPage(
            Settings.Panel.ACTION_INTERNET_CONNECTIVITY,
            SettingsPageCategory.PANELS,
            true,
            R.string.settings_page_name_quick_connection,
        ),
        SettingsPage(
            Settings.Panel.ACTION_VOLUME,
            SettingsPageCategory.PANELS,
            true,
            R.string.settings_page_name_quick_volume,
        ),
        // Network
        SettingsPage(
            Settings.ACTION_WIFI_SETTINGS,
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_wifi,
        ),
        SettingsPage(
            Settings.ACTION_BLUETOOTH_SETTINGS,
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_bluetooth,
        ),
        SettingsPage(
            Settings.ACTION_NFC_SETTINGS,
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_nfc,
        ),
        SettingsPage(
            Settings.ACTION_NETWORK_OPERATOR_SETTINGS,
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_mobile_networks,
        ),
        SettingsPage(
            Settings.ACTION_WIRELESS_SETTINGS,
            SettingsPageCategory.NETWORK,
            true,
            R.string.settings_page_name_internet,
        ),
        SettingsPage(
            Settings.ACTION_AIRPLANE_MODE_SETTINGS,
            SettingsPageCategory.NETWORK,
            false,
            R.string.settings_page_name_airplane_mode,
        ),
        // OEM
        SettingsPage(
            Settings.ACTION_DATA_USAGE_SETTINGS,
            SettingsPageCategory.NETWORK,
            false,
            R.string.settings_page_name_data_usage,
        ),
        SettingsPage(
            Settings.ACTION_APN_SETTINGS,
            SettingsPageCategory.NETWORK,
            false,
            R.string.settings_page_name_apn,
        ), // OEM
        // Display
        SettingsPage(
            Settings.ACTION_DISPLAY_SETTINGS,
            SettingsPageCategory.DISPLAY,
            true,
            R.string.settings_page_name_display,
        ),
        SettingsPage(
            Settings.ACTION_NIGHT_DISPLAY_SETTINGS,
            SettingsPageCategory.DISPLAY,
            true,
            R.string.settings_page_name_night_light,
        ),
        SettingsPage(
            "android.settings.DARK_THEME_SETTINGS",
            SettingsPageCategory.DISPLAY,
            true,
            R.string.settings_page_name_dark_theme,
        ),
        SettingsPage(
            Settings.ACTION_AUTO_ROTATE_SETTINGS,
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_auto_rotate,
        ),
        SettingsPage(
            "android.settings.FONT_SIZE_SETTINGS",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_font_size,
        ), // OEM
        SettingsPage(
            "android.settings.WALLPAPER_SETTINGS",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_wallpaper,
        ),
        SettingsPage(
            "android.settings.LIVE_DISPLAY_SETTINGS",
            SettingsPageCategory.DISPLAY,
            false,
            R.string.settings_page_name_display_color_temperature,
        ), // OEM
        // Sound
        SettingsPage(
            Settings.ACTION_SOUND_SETTINGS,
            SettingsPageCategory.SOUND,
            true,
            R.string.settings_page_name_sound,
        ),
        SettingsPage(
            Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
            SettingsPageCategory.SOUND,
            false,
            R.string.settings_page_name_do_not_disturb,
        ),
        SettingsPage(
            Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS,
            SettingsPageCategory.SOUND,
            false,
            R.string.settings_page_name_zen_mode,
        ),
        // Apps
        SettingsPage(
            Settings.ACTION_APPLICATION_SETTINGS,
            SettingsPageCategory.APPS,
            true,
            R.string.settings_page_name_apps,
        ),
        SettingsPage(
            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_app_notifications,
        ),
        SettingsPage(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_app_permissions,
        ),
        SettingsPage(
            Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS,
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_all_apps,
        ),
        SettingsPage(
            Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
            SettingsPageCategory.APPS,
            false,
            R.string.settings_page_name_installed_apps,
        ),
        SettingsPage(
            Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
            SettingsPageCategory.APPS,
            true,
            R.string.settings_page_name_developer_options,
        ),
        // Security
        SettingsPage(
            Settings.ACTION_SECURITY_SETTINGS,
            SettingsPageCategory.SECURITY,
            true,
            R.string.settings_page_name_security,
        ),
        SettingsPage(
            Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            SettingsPageCategory.SECURITY,
            true,
            R.string.settings_page_name_location,
        ),
        SettingsPage(
            "android.settings.LOCK_SCREEN_SETTINGS",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_lock_screen,
        ),
        SettingsPage(
            Settings.ACTION_PRIVACY_SETTINGS,
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_privacy,
        ),
        SettingsPage(
            "android.settings.CREDENTIAL_SETTINGS",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_credentials,
        ),
        // OEM
        SettingsPage(
            "android.settings.TRUSTED_CREDENTIALS_SETTINGS",
            SettingsPageCategory.SECURITY,
            false,
            R.string.settings_page_name_trusted_certificates,
        ),
        // Personal
        SettingsPage(
            Settings.ACTION_DATE_SETTINGS,
            SettingsPageCategory.PERSONAL,
            true,
            R.string.settings_page_name_date_time,
        ),
        SettingsPage(
            Settings.ACTION_LOCALE_SETTINGS,
            SettingsPageCategory.PERSONAL,
            true,
            R.string.settings_page_name_language,
        ),
        SettingsPage(
            "android.settings.USER_SETTINGS",
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_users,
        ),
        SettingsPage(
            "android.settings.ACCOUNT_SYNC_SETTINGS",
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_accounts,
        ),
        SettingsPage(
            Settings.ACTION_SYNC_SETTINGS,
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_sync,
        ),
        SettingsPage(
            Settings.ACTION_VOICE_INPUT_SETTINGS,
            SettingsPageCategory.PERSONAL,
            false,
            R.string.settings_page_name_voice_input,
        ),
        // System
        SettingsPage(
            Settings.ACTION_ACCESSIBILITY_SETTINGS,
            SettingsPageCategory.SYSTEM,
            true,
            R.string.settings_page_name_accessibility,
        ),
        SettingsPage(
            Settings.ACTION_DEVICE_INFO_SETTINGS,
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_about_device,
        ),
        SettingsPage(
            Settings.ACTION_MEMORY_CARD_SETTINGS,
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_storage,
        ),
        SettingsPage(
            Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_internal_storage,
        ),
        SettingsPage(
            Settings.ACTION_BATTERY_SAVER_SETTINGS,
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_battery,
        ),
        SettingsPage(
            "android.settings.NOTIFICATION_SETTINGS",
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_notifications,
        ),
        SettingsPage(
            Settings.ACTION_SETTINGS,
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_all_settings,
        ),
        SettingsPage(
            Settings.ACTION_QUICK_LAUNCH_SETTINGS,
            SettingsPageCategory.SYSTEM,
            false,
            R.string.settings_page_name_quick_launch,
        ), // OEM
    )

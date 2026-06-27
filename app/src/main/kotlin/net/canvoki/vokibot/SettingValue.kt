package net.canvoki.vokibot

import android.provider.Settings
import androidx.annotation.StringRes
import net.canvoki.vokibot.common.EnumOption
import net.canvoki.vokibot.common.FlagOption

data class SettingValue(
    val id: String,
    val category: SettingCategory,
    @get:StringRes
    val name: Int,
    @get:StringRes
    val description: Int,
    @get:StringRes
    val rawHelp: Int,
    val type: ExtraType,
)

enum class SettingCategory {
    DISPLAY,
    SOUND,
    CONNECTIVITY,
    ACCESSIBILITY,
    TEXT,
    TIME,
    DEVELOPER,
    POWER,
    DEVICE,
    ;

    @get:StringRes
    val labelRes: Int get() =
        when (this) {
            DISPLAY -> R.string.setting_category_display
            SOUND -> R.string.setting_category_sound
            CONNECTIVITY -> R.string.setting_category_connectivity
            ACCESSIBILITY -> R.string.setting_category_accessibility
            TEXT -> R.string.setting_category_text
            TIME -> R.string.setting_category_time
            DEVELOPER -> R.string.setting_category_developer
            POWER -> R.string.setting_category_power
            DEVICE -> R.string.setting_category_device
        }
}

val SETTING_VALUES: List<SettingValue> =
    listOf(
        // Display
        SettingValue(
            id = Settings.System.SCREEN_BRIGHTNESS_MODE,
            category = SettingCategory.DISPLAY,
            name = R.string.setting_screen_brightness_mode_name,
            description = R.string.setting_screen_brightness_mode_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.SCREEN_BRIGHTNESS,
            category = SettingCategory.DISPLAY,
            name = R.string.setting_screen_brightness_name,
            description = R.string.setting_screen_brightness_description,
            rawHelp = R.string.setting_screen_brightness_raw_help,
            type = ExtraType.String, // TODO: Int ranged 0..255
        ),
        SettingValue(
            id = Settings.System.SCREEN_OFF_TIMEOUT,
            category = SettingCategory.DISPLAY,
            name = R.string.setting_screen_off_timeout_name,
            description = R.string.setting_screen_off_timeout_description,
            rawHelp = R.string.setting_screen_off_timeout_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("15000", R.string.setting_duration_15_seconds),
                        EnumOption("30000", R.string.setting_duration_30_seconds),
                        EnumOption("60000", R.string.setting_duration_1_minute),
                        EnumOption("120000", R.string.setting_duration_2_minutes),
                        EnumOption("300000", R.string.setting_duration_5_minutes),
                        EnumOption("600000", R.string.setting_duration_10_minutes),
                        EnumOption("1800000", R.string.setting_duration_30_minutes),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.System.USER_ROTATION,
            category = SettingCategory.DISPLAY,
            name = R.string.setting_user_rotation_name,
            description = R.string.setting_user_rotation_description,
            rawHelp = R.string.setting_user_rotation_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0", R.string.setting_rotation_0),
                        EnumOption("1", R.string.setting_rotation_90),
                        EnumOption("2", R.string.setting_rotation_180),
                        EnumOption("3", R.string.setting_rotation_270),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.System.ACCELEROMETER_ROTATION,
            category = SettingCategory.DISPLAY,
            name = R.string.setting_accelerometer_rotation_name,
            description = R.string.setting_accelerometer_rotation_description,
            rawHelp = R.string.setting_accelerometer_rotation_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.FONT_SCALE,
            category = SettingCategory.DISPLAY,
            name = R.string.setting_font_scale_name,
            description = R.string.setting_font_scale_description,
            rawHelp = R.string.setting_font_scale_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0.5", R.string.setting_font_scale_0_5),
                        EnumOption("0.7", R.string.setting_font_scale_0_7),
                        EnumOption("0.85", R.string.setting_font_scale_0_85),
                        EnumOption("1.0", R.string.setting_font_scale_1_0),
                        EnumOption("1.15", R.string.setting_font_scale_1_15),
                        EnumOption("1.3", R.string.setting_font_scale_1_3),
                        EnumOption("1.5", R.string.setting_font_scale_1_5),
                        EnumOption("2.0", R.string.setting_font_scale_2_0),
                    ),
                ),
        ),
        // Sound
        SettingValue(
            id = Settings.System.RINGTONE,
            category = SettingCategory.SOUND,
            name = R.string.setting_ringtone_name,
            description = R.string.setting_ringtone_description,
            rawHelp = R.string.setting_audio_uri_raw_help,
            type = ExtraType.Uri,
        ),
        SettingValue(
            id = Settings.System.NOTIFICATION_SOUND,
            category = SettingCategory.SOUND,
            name = R.string.setting_notification_sound_name,
            description = R.string.setting_notification_sound_description,
            rawHelp = R.string.setting_audio_uri_raw_help,
            type = ExtraType.Uri,
        ),
        SettingValue(
            id = Settings.System.ALARM_ALERT,
            category = SettingCategory.SOUND,
            name = R.string.setting_alarm_alert_name,
            description = R.string.setting_alarm_alert_description,
            rawHelp = R.string.setting_audio_uri_raw_help,
            type = ExtraType.Uri,
        ),
        SettingValue(
            id = Settings.System.DTMF_TONE_WHEN_DIALING,
            category = SettingCategory.SOUND,
            name = R.string.setting_dtmf_tone_when_dialing_name,
            description = R.string.setting_dtmf_tone_when_dialing_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.SOUND_EFFECTS_ENABLED,
            category = SettingCategory.SOUND,
            name = R.string.setting_sound_effects_enabled_name,
            description = R.string.setting_sound_effects_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.System.HAPTIC_FEEDBACK_ENABLED,
            category = SettingCategory.SOUND,
            name = R.string.setting_haptic_feedback_enabled_name,
            description = R.string.setting_haptic_feedback_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.MODE_RINGER_STREAMS_AFFECTED,
            category = SettingCategory.SOUND,
            name = R.string.setting_mode_ringer_streams_affected_name,
            description = R.string.setting_mode_ringer_streams_affected_description,
            rawHelp = R.string.setting_stream_bitmask_raw_help,
            type =
                ExtraType.Flags(
                    listOf(
                        FlagOption("1", R.string.setting_audio_stream_voice_call),
                        FlagOption("2", R.string.setting_audio_stream_system),
                        FlagOption("4", R.string.setting_audio_stream_ring),
                        FlagOption("8", R.string.setting_audio_stream_music),
                        FlagOption("16", R.string.setting_audio_stream_alarm),
                        FlagOption("32", R.string.setting_audio_stream_notification),
                        FlagOption("64", R.string.setting_audio_stream_bt_sco),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.System.MUTE_STREAMS_AFFECTED,
            category = SettingCategory.SOUND,
            name = R.string.setting_mute_streams_affected_name,
            description = R.string.setting_mute_streams_affected_description,
            rawHelp = R.string.setting_stream_bitmask_raw_help,
            type =
                ExtraType.Flags(
                    listOf(
                        FlagOption("1", R.string.setting_audio_stream_voice_call),
                        FlagOption("2", R.string.setting_audio_stream_system),
                        FlagOption("4", R.string.setting_audio_stream_ring),
                        FlagOption("8", R.string.setting_audio_stream_music),
                        FlagOption("16", R.string.setting_audio_stream_alarm),
                        FlagOption("32", R.string.setting_audio_stream_notification),
                        FlagOption("64", R.string.setting_audio_stream_bt_sco),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Global.MODE_RINGER,
            category = SettingCategory.SOUND,
            name = R.string.setting_mode_ringer_name,
            description = R.string.setting_mode_ringer_description,
            rawHelp = R.string.setting_mode_ringer_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0", R.string.setting_ringer_mode_normal),
                        EnumOption("1", R.string.setting_ringer_mode_vibrate),
                        EnumOption("2", R.string.setting_ringer_mode_silent),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Secure.TTS_DEFAULT_PITCH,
            category = SettingCategory.SOUND,
            name = R.string.setting_tts_default_pitch_name,
            description = R.string.setting_tts_default_pitch_description,
            rawHelp = R.string.setting_tts_default_pitch_raw_help,
            type = ExtraType.String, // TODO: Int ranged 10..500
        ),
        SettingValue(
            id = Settings.Secure.TTS_DEFAULT_RATE,
            category = SettingCategory.SOUND,
            name = R.string.setting_tts_default_rate_name,
            description = R.string.setting_tts_default_rate_description,
            rawHelp = R.string.setting_tts_default_rate_raw_help,
            type = ExtraType.String, // TODO: Int ranged 10..300
        ),
        SettingValue(
            id = Settings.Secure.TTS_DEFAULT_SYNTH,
            category = SettingCategory.SOUND,
            name = R.string.setting_tts_default_synth_name,
            description = R.string.setting_tts_default_synth_description,
            rawHelp = R.string.setting_tts_default_synth_raw_help,
            type = ExtraType.String, // TODO: dynamic list from TextToSpeech.Engine
        ),
        SettingValue(
            id = Settings.Secure.TTS_ENABLED_PLUGINS,
            category = SettingCategory.SOUND,
            name = R.string.setting_tts_enabled_plugins_name,
            description = R.string.setting_tts_enabled_plugins_description,
            rawHelp = R.string.setting_tts_enabled_plugins_raw_help,
            type = ExtraType.String, // TODO: dynamic list from TextToSpeech.Engine
        ),
        // Text
        SettingValue(
            id = Settings.System.TEXT_AUTO_CAPS,
            category = SettingCategory.TEXT,
            name = R.string.setting_text_auto_caps_name,
            description = R.string.setting_text_auto_caps_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.TEXT_AUTO_PUNCTUATE,
            category = SettingCategory.TEXT,
            name = R.string.setting_text_auto_punctuate_name,
            description = R.string.setting_text_auto_punctuate_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.TEXT_AUTO_REPLACE,
            category = SettingCategory.TEXT,
            name = R.string.setting_text_auto_replace_name,
            description = R.string.setting_text_auto_replace_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.TEXT_SHOW_PASSWORD,
            category = SettingCategory.TEXT,
            name = R.string.setting_text_show_password_name,
            description = R.string.setting_text_show_password_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        // Time
        SettingValue(
            id = Settings.System.TIME_12_24,
            category = SettingCategory.TIME,
            name = R.string.setting_time_12_24_name,
            description = R.string.setting_time_12_24_description,
            rawHelp = R.string.setting_time_12_24_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("12", R.string.setting_hour_format_12h),
                        EnumOption("24", R.string.setting_hour_format_24h),
                    ),
                ),
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.System.DATE_FORMAT,
            category = SettingCategory.TIME,
            name = R.string.setting_date_format_name,
            description = R.string.setting_date_format_description,
            rawHelp = R.string.setting_date_format_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("MM/dd/yyyy", R.string.setting_date_format_mdy),
                        EnumOption("dd/MM/yyyy", R.string.setting_date_format_dmy),
                        EnumOption("yyyy/MM/dd", R.string.setting_date_format_ymd),
                        EnumOption("yyyy-MM-dd", R.string.setting_date_format_iso),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Global.AUTO_TIME,
            category = SettingCategory.TIME,
            name = R.string.setting_auto_time_name,
            description = R.string.setting_auto_time_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.AUTO_TIME_ZONE,
            category = SettingCategory.TIME,
            name = R.string.setting_auto_time_zone_name,
            description = R.string.setting_auto_time_zone_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        // Connectivity
        SettingValue(
            id = Settings.Global.AIRPLANE_MODE_ON,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_airplane_mode_on_name,
            description = R.string.setting_airplane_mode_on_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.BLUETOOTH_ON,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_bluetooth_on_name,
            description = R.string.setting_bluetooth_on_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.WIFI_ON,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_wifi_on_name,
            description = R.string.setting_wifi_on_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.DATA_ROAMING,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_data_roaming_name,
            description = R.string.setting_data_roaming_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.HTTP_PROXY,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_http_proxy_name,
            description = R.string.setting_http_proxy_description,
            rawHelp = R.string.setting_http_proxy_raw_help,
            type = ExtraType.String,
        ),
        SettingValue(
            id = Settings.Global.AIRPLANE_MODE_RADIOS,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_airplane_mode_radios_name,
            description = R.string.setting_airplane_mode_radios_description,
            rawHelp = R.string.setting_airplane_mode_radios_raw_help,
            type = ExtraType.String,
            // TODO: StringArray flags("bluetooth", "cell", "nfc", "wifi")
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.Global.WIFI_SLEEP_POLICY,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_wifi_sleep_policy_name,
            description = R.string.setting_wifi_sleep_policy_description,
            rawHelp = R.string.setting_wifi_sleep_policy_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0", R.string.setting_wifi_sleep_policy_default),
                        EnumOption("1", R.string.setting_wifi_sleep_policy_never_while_plugged),
                        EnumOption("2", R.string.setting_wifi_sleep_policy_never),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Global.WIFI_MAX_DHCP_RETRY_COUNT,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_wifi_max_dhcp_retry_count_name,
            description = R.string.setting_wifi_max_dhcp_retry_count_description,
            rawHelp = R.string.setting_wifi_max_dhcp_retry_count_raw_help,
            type = ExtraType.String, // TODO: Int ranged
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.Global.WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_wifi_networks_available_repeat_delay_name,
            description = R.string.setting_wifi_networks_available_repeat_delay_description,
            rawHelp = R.string.setting_wifi_networks_available_repeat_delay_raw_help,
            type = ExtraType.String, // TODO: Int ranged
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.Global.WIFI_NUM_OPEN_NETWORKS_KEPT,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_wifi_num_open_networks_kept_name,
            description = R.string.setting_wifi_num_open_networks_kept_description,
            rawHelp = R.string.setting_wifi_num_open_networks_kept_raw_help,
            type = ExtraType.String, // TODO: Int ranged
        ),
        SettingValue(
            id = Settings.Global.WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_wifi_mobile_data_transition_wakelock_timeout_ms_name,
            description = R.string.setting_wifi_mobile_data_transition_wakelock_timeout_ms_description,
            rawHelp = R.string.setting_wifi_mobile_data_transition_wakelock_timeout_ms_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("15000", R.string.setting_duration_15_seconds),
                        EnumOption("30000", R.string.setting_duration_30_seconds),
                        EnumOption("60000", R.string.setting_duration_1_minute),
                        EnumOption("120000", R.string.setting_duration_2_minutes),
                        EnumOption("300000", R.string.setting_duration_5_minutes),
                        EnumOption("600000", R.string.setting_duration_10_minutes),
                        EnumOption("1800000", R.string.setting_duration_30_minutes),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.System.BLUETOOTH_DISCOVERABILITY,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_bluetooth_discoverability_name,
            description = R.string.setting_bluetooth_discoverability_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.System.BLUETOOTH_DISCOVERABILITY_TIMEOUT,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_bluetooth_discoverability_timeout_name,
            description = R.string.setting_bluetooth_discoverability_timeout_description,
            rawHelp = R.string.setting_bluetooth_discoverability_timeout_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("120", R.string.setting_duration_2_minutes),
                        EnumOption("300", R.string.setting_duration_5_minutes),
                        EnumOption("600", R.string.setting_duration_10_minutes),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.System.END_BUTTON_BEHAVIOR,
            category = SettingCategory.CONNECTIVITY,
            name = R.string.setting_end_button_behavior_name,
            description = R.string.setting_end_button_behavior_description,
            rawHelp = R.string.setting_end_button_behavior_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0", R.string.setting_end_button_behavior_end_call),
                        EnumOption("1", R.string.setting_end_button_behavior_caller_log),
                    ),
                ),
        ),
        // Accessibility
        SettingValue(
            id = Settings.Secure.ACCESSIBILITY_ENABLED,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_accessibility_enabled_name,
            description = R.string.setting_accessibility_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_accessibility_display_inversion_enabled_name,
            description = R.string.setting_accessibility_display_inversion_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Secure.TOUCH_EXPLORATION_ENABLED,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_touch_exploration_enabled_name,
            description = R.string.setting_touch_exploration_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_enabled_accessibility_services_name,
            description = R.string.setting_enabled_accessibility_services_description,
            rawHelp = R.string.setting_component_names_raw_help,
            type = ExtraType.String, // TODO: dynamic list from AccessibilityManager
        ),
        SettingValue(
            id = Settings.Secure.DEFAULT_INPUT_METHOD,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_default_input_method_name,
            description = R.string.setting_default_input_method_description,
            rawHelp = R.string.setting_component_name_raw_help,
            type = ExtraType.String, // TODO: dynamic list from InputMethodManager
        ),
        SettingValue(
            id = Settings.Secure.ENABLED_INPUT_METHODS,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_enabled_input_methods_name,
            description = R.string.setting_enabled_input_methods_description,
            rawHelp = R.string.setting_component_names_raw_help,
            type = ExtraType.String, // TODO: dynamic list from InputMethodManager
        ),
        SettingValue(
            id = Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_selected_input_method_subtype_name,
            description = R.string.setting_selected_input_method_subtype_description,
            rawHelp = R.string.setting_component_name_raw_help,
            type = ExtraType.String, // TODO: dynamic list from InputMethodManager
        ),
        SettingValue(
            id = Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY,
            category = SettingCategory.ACCESSIBILITY,
            name = R.string.setting_input_method_selector_visibility_name,
            description = R.string.setting_input_method_selector_visibility_description,
            rawHelp = R.string.setting_input_method_selector_visibility_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0", R.string.setting_input_method_selector_auto),
                        EnumOption("1", R.string.setting_input_method_selector_always_show),
                    ),
                ),
        ),
        // Developer
        SettingValue(
            id = Settings.Global.ADB_ENABLED,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_adb_enabled_name,
            description = R.string.setting_adb_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_development_settings_enabled_name,
            description = R.string.setting_development_settings_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.WAIT_FOR_DEBUGGER,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_wait_for_debugger_name,
            description = R.string.setting_wait_for_debugger_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.ALWAYS_FINISH_ACTIVITIES,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_always_finish_activities_name,
            description = R.string.setting_always_finish_activities_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.WINDOW_ANIMATION_SCALE,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_window_animation_scale_name,
            description = R.string.setting_window_animation_scale_description,
            rawHelp = R.string.setting_animation_scale_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0.0", R.string.setting_animation_scale_off),
                        EnumOption("0.5", R.string.setting_animation_scale_0_5x),
                        EnumOption("1.0", R.string.setting_animation_scale_1x),
                        EnumOption("1.5", R.string.setting_animation_scale_1_5x),
                        EnumOption("2.0", R.string.setting_animation_scale_2x),
                        EnumOption("5.0", R.string.setting_animation_scale_5x),
                        EnumOption("10.0", R.string.setting_animation_scale_10x),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Global.TRANSITION_ANIMATION_SCALE,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_transition_animation_scale_name,
            description = R.string.setting_transition_animation_scale_description,
            rawHelp = R.string.setting_animation_scale_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0.0", R.string.setting_animation_scale_off),
                        EnumOption("0.5", R.string.setting_animation_scale_0_5x),
                        EnumOption("1.0", R.string.setting_animation_scale_1x),
                        EnumOption("1.5", R.string.setting_animation_scale_1_5x),
                        EnumOption("2.0", R.string.setting_animation_scale_2x),
                        EnumOption("5.0", R.string.setting_animation_scale_5x),
                        EnumOption("10.0", R.string.setting_animation_scale_10x),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Global.ANIMATOR_DURATION_SCALE,
            category = SettingCategory.DEVELOPER,
            name = R.string.setting_animator_duration_scale_name,
            description = R.string.setting_animator_duration_scale_description,
            rawHelp = R.string.setting_animation_scale_raw_help,
            type =
                ExtraType.Enum(
                    listOf(
                        EnumOption("0.0", R.string.setting_animation_scale_off),
                        EnumOption("0.5", R.string.setting_animation_scale_0_5x),
                        EnumOption("1.0", R.string.setting_animation_scale_1x),
                        EnumOption("1.5", R.string.setting_animation_scale_1_5x),
                        EnumOption("2.0", R.string.setting_animation_scale_2x),
                        EnumOption("5.0", R.string.setting_animation_scale_5x),
                        EnumOption("10.0", R.string.setting_animation_scale_10x),
                    ),
                ),
        ),
        // Power
        SettingValue(
            id = Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            category = SettingCategory.POWER,
            name = R.string.setting_stay_on_while_plugged_in_name,
            description = R.string.setting_stay_on_while_plugged_in_description,
            rawHelp = R.string.setting_stay_on_while_plugged_in_raw_help,
            type =
                ExtraType.Flags(
                    listOf(
                        FlagOption("1", R.string.setting_plugged_ac),
                        FlagOption("2", R.string.setting_plugged_usb),
                        FlagOption("4", R.string.setting_plugged_wireless),
                    ),
                ),
        ),
        SettingValue(
            id = Settings.Global.USB_MASS_STORAGE_ENABLED,
            category = SettingCategory.POWER,
            name = R.string.setting_usb_mass_storage_enabled_name,
            description = R.string.setting_usb_mass_storage_enabled_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
        // Device
        SettingValue(
            id = Settings.Global.DEVICE_NAME,
            category = SettingCategory.DEVICE,
            name = R.string.setting_device_name_name,
            description = R.string.setting_device_name_description,
            rawHelp = R.string.setting_device_name_raw_help,
            type = ExtraType.String,
        ),
        SettingValue(
            id = Settings.Global.DEVICE_PROVISIONED,
            category = SettingCategory.DEVICE,
            name = R.string.setting_device_provisioned_name,
            description = R.string.setting_device_provisioned_description,
            rawHelp = R.string.setting_device_provisioned_raw_help,
            type = ExtraType.Boolean,
        ),
        SettingValue(
            id = Settings.Global.USE_GOOGLE_MAIL,
            category = SettingCategory.DEVICE,
            name = R.string.setting_use_google_mail_name,
            description = R.string.setting_use_google_mail_description,
            rawHelp = R.string.setting_toggle_raw_help,
            type = ExtraType.Boolean,
        ),
    )

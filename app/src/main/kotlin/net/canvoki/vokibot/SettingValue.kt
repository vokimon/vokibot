package net.canvoki.vokibot

import android.provider.Settings
import androidx.annotation.StringRes

data class SettingValue(
    val id: String,
    val category: SettingCategory,
    val name: String,
    val description: String,
    val rawHelp: String,
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
            name = "Adaptive Brightness",
            description = "When enabled the screen brightness will adapt to environmental light",
            rawHelp = "1 for enabled, 0 for disabled",
        ),
        SettingValue(
            id = Settings.System.SCREEN_BRIGHTNESS,
            category = SettingCategory.DISPLAY,
            name = "Screen Brightness",
            description = "Screen backlight brightness",
            rawHelp = "Integer 0-255",
        ),
        SettingValue(
            id = Settings.System.SCREEN_OFF_TIMEOUT,
            category = SettingCategory.DISPLAY,
            name = "Screen Off Timeout",
            description = "Milliseconds before screen turns off",
            rawHelp = "Integer in milliseconds (e.g. 60000 for 1 minute)",
        ),
        SettingValue(
            id = Settings.System.USER_ROTATION,
            category = SettingCategory.DISPLAY,
            name = "User Rotation",
            description = "Default screen rotation",
            rawHelp = "0=0\u00b0, 1=90\u00b0, 2=180\u00b0, 3=270\u00b0",
        ),
        SettingValue(
            id = Settings.System.ACCELEROMETER_ROTATION,
            category = SettingCategory.DISPLAY,
            name = "Accelerometer Rotation",
            description = "Whether rotation is controlled by accelerometer",
            rawHelp = "0=manual, 1=accelerometer controls rotation",
        ),
        SettingValue(
            id = Settings.System.FONT_SCALE,
            category = SettingCategory.DISPLAY,
            name = "Font Scale",
            description = "Font scaling factor",
            rawHelp = "Float, 1.0 is default",
        ),
        // Sound
        SettingValue(
            id = Settings.System.RINGTONE,
            category = SettingCategory.SOUND,
            name = "Ringtone",
            description = "Content URI of default ringtone",
            rawHelp = "Content URI string",
        ),
        SettingValue(
            id = Settings.System.NOTIFICATION_SOUND,
            category = SettingCategory.SOUND,
            name = "Notification Sound",
            description = "Content URI of default notification sound",
            rawHelp = "Content URI string",
        ),
        SettingValue(
            id = Settings.System.ALARM_ALERT,
            category = SettingCategory.SOUND,
            name = "Alarm Alert",
            description = "Content URI of default alarm alert",
            rawHelp = "Content URI string",
        ),
        SettingValue(
            id = Settings.System.DTMF_TONE_WHEN_DIALING,
            category = SettingCategory.SOUND,
            name = "DTMF Tones",
            description = "Audible DTMF tones when dialing",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.System.SOUND_EFFECTS_ENABLED,
            category = SettingCategory.SOUND,
            name = "Sound Effects",
            description = "Key clicks and lid sounds",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.System.HAPTIC_FEEDBACK_ENABLED,
            category = SettingCategory.SOUND,
            name = "Haptic Feedback",
            description = "Haptic feedback on long press and other interactions",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.System.MODE_RINGER_STREAMS_AFFECTED,
            category = SettingCategory.SOUND,
            name = "Ringer Streams Affected",
            description = "Which audio streams are affected by ringer mode",
            rawHelp = "Bitmask: voice_call=1, system=2, ring=4, music=8, alarm=16, notification=32, bt_sco=64",
        ),
        SettingValue(
            id = Settings.System.MUTE_STREAMS_AFFECTED,
            category = SettingCategory.SOUND,
            name = "Mute Streams Affected",
            description = "Which audio streams are affected by mute",
            rawHelp = "Bitmask: voice_call=1, system=2, ring=4, music=8, alarm=16, notification=32, bt_sco=64",
        ),
        SettingValue(
            id = Settings.System.VIBRATE_ON,
            category = SettingCategory.SOUND,
            name = "Vibrate On",
            description = "When to vibrate for incoming calls and notifications",
            rawHelp = "Bitmask: call=1, call_cdma=2, notification=4, chat=8, calendar=16, hangup=32",
        ),
        SettingValue(
            id = Settings.Global.MODE_RINGER,
            category = SettingCategory.SOUND,
            name = "Ringer Mode",
            description = "Current ringer mode",
            rawHelp = "0=normal, 1=vibrate, 2=silent",
        ),
        SettingValue(
            id = Settings.Secure.TTS_DEFAULT_PITCH,
            category = SettingCategory.SOUND,
            name = "TTS Default Pitch",
            description = "Default pitch for text-to-speech",
            rawHelp = "Integer 10-500",
        ),
        SettingValue(
            id = Settings.Secure.TTS_DEFAULT_RATE,
            category = SettingCategory.SOUND,
            name = "TTS Default Rate",
            description = "Default speech rate for text-to-speech",
            rawHelp = "Integer 10-300",
        ),
        SettingValue(
            id = Settings.Secure.TTS_DEFAULT_SYNTH,
            category = SettingCategory.SOUND,
            name = "TTS Default Synth",
            description = "Package name of default TTS engine",
            rawHelp = "Package name string",
        ),
        SettingValue(
            id = Settings.Secure.TTS_ENABLED_PLUGINS,
            category = SettingCategory.SOUND,
            name = "TTS Enabled Plugins",
            description = "Space-separated list of enabled TTS plugins",
            rawHelp = "Space-separated package names",
        ),
        // Text
        SettingValue(
            id = Settings.System.TEXT_AUTO_CAPS,
            category = SettingCategory.TEXT,
            name = "Auto Capitalize",
            description = "Automatically capitalize first letter of sentences",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.System.TEXT_AUTO_PUNCTUATE,
            category = SettingCategory.TEXT,
            name = "Auto Punctuate",
            description = "Automatically add period after double space",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.System.TEXT_AUTO_REPLACE,
            category = SettingCategory.TEXT,
            name = "Auto Replace",
            description = "Automatically replace text using AutoText",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.System.TEXT_SHOW_PASSWORD,
            category = SettingCategory.TEXT,
            name = "Show Password",
            description = "Briefly show password characters when typing",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        // Time
        SettingValue(
            id = Settings.System.TIME_12_24,
            category = SettingCategory.TIME,
            name = "Hour Format",
            description = "Whether clock uses 12 or 24 hour format",
            rawHelp = "\"12\" or \"24\"",
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.System.DATE_FORMAT,
            category = SettingCategory.TIME,
            name = "Date Format",
            description = "System date format string. Deprecated API 31.",
            rawHelp = "Format string (e.g. mm/dd/yyyy, dd/mm/yyyy, yyyy/mm/dd)",
        ),
        SettingValue(
            id = Settings.Global.AUTO_TIME,
            category = SettingCategory.TIME,
            name = "Auto Time",
            description = "Automatically set clock from network",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.AUTO_TIME_ZONE,
            category = SettingCategory.TIME,
            name = "Auto Time Zone",
            description = "Automatically set time zone from network",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        // Connectivity
        SettingValue(
            id = Settings.Global.AIRPLANE_MODE_ON,
            category = SettingCategory.CONNECTIVITY,
            name = "Airplane Mode",
            description = "Whether airplane mode is active",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.BLUETOOTH_ON,
            category = SettingCategory.CONNECTIVITY,
            name = "Bluetooth",
            description = "Whether Bluetooth is enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.WIFI_ON,
            category = SettingCategory.CONNECTIVITY,
            name = "Wi-Fi",
            description = "Whether Wi-Fi is enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.DATA_ROAMING,
            category = SettingCategory.CONNECTIVITY,
            name = "Data Roaming",
            description = "Whether mobile data roaming is enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.NETWORK_PREFERENCE,
            category = SettingCategory.CONNECTIVITY,
            name = "Network Preference",
            description = "Preferred network types",
            rawHelp = "Integer bitmask for preferred networks",
        ),
        SettingValue(
            id = Settings.Global.HTTP_PROXY,
            category = SettingCategory.CONNECTIVITY,
            name = "HTTP Proxy",
            description = "Global HTTP proxy",
            rawHelp = "host:port string",
        ),
        SettingValue(
            id = Settings.Global.AIRPLANE_MODE_RADIOS,
            category = SettingCategory.CONNECTIVITY,
            name = "Airplane Mode Radios",
            description = "Radios disabled when airplane mode is on",
            rawHelp = "Comma-separated list (e.g. bluetooth,cell,nfc,wifi)",
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.Global.WIFI_SLEEP_POLICY,
            category = SettingCategory.CONNECTIVITY,
            name = "Wi-Fi Sleep Policy",
            description = "When Wi-Fi goes to sleep. Deprecated API 30.",
            rawHelp = "0=default, 1=never while plugged, 2=never",
        ),
        SettingValue(
            id = Settings.Global.WIFI_MAX_DHCP_RETRY_COUNT,
            category = SettingCategory.CONNECTIVITY,
            name = "Wi-Fi Max DHCP Retries",
            description = "Maximum number of DHCP retry attempts",
            rawHelp = "Integer",
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.Global.WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY,
            category = SettingCategory.CONNECTIVITY,
            name = "Wi-Fi Repeat Scan Delay",
            description = "Seconds before repeating open network notification. Deprecated API 30.",
            rawHelp = "Integer in seconds",
        ),
        SettingValue(
            id = @Suppress("DEPRECATION") Settings.Global.WIFI_NUM_OPEN_NETWORKS_KEPT,
            category = SettingCategory.CONNECTIVITY,
            name = "Wi-Fi Open Networks Kept",
            description = "Maximum open networks to remember. Deprecated API 30.",
            rawHelp = "Integer",
        ),
        SettingValue(
            id = Settings.Global.WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS,
            category = SettingCategory.CONNECTIVITY,
            name = "Wi-Fi/Data Wake Lock Timeout",
            description = "Wakelock timeout when switching between Wi-Fi and mobile data",
            rawHelp = "Integer in milliseconds",
        ),
        SettingValue(
            id = Settings.System.BLUETOOTH_DISCOVERABILITY,
            category = SettingCategory.CONNECTIVITY,
            name = "Bluetooth Discoverability",
            description = "Whether the device is discoverable via Bluetooth",
            rawHelp = "0=disabled, 1=enabled",
        ),
        SettingValue(
            id = Settings.System.BLUETOOTH_DISCOVERABILITY_TIMEOUT,
            category = SettingCategory.CONNECTIVITY,
            name = "Bluetooth Discoverability Timeout",
            description = "How long the device remains Bluetooth discoverable",
            rawHelp = "Integer in seconds (e.g. 120, 300, 600)",
        ),
        SettingValue(
            id = Settings.System.END_BUTTON_BEHAVIOR,
            category = SettingCategory.CONNECTIVITY,
            name = "End Button Behavior",
            description = "Action of the end call button",
            rawHelp = "0=end call, 1=go to caller log",
        ),
        // Accessibility
        SettingValue(
            id = Settings.Secure.ACCESSIBILITY_ENABLED,
            category = SettingCategory.ACCESSIBILITY,
            name = "Accessibility Services",
            description = "Whether accessibility services are enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED,
            category = SettingCategory.ACCESSIBILITY,
            name = "Display Inversion",
            description = "Whether screen colors are inverted",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Secure.TOUCH_EXPLORATION_ENABLED,
            category = SettingCategory.ACCESSIBILITY,
            name = "Touch Exploration",
            description = "Whether touch exploration (TalkBack) is enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            category = SettingCategory.ACCESSIBILITY,
            name = "Accessibility Service List",
            description = "List of enabled accessibility services",
            rawHelp = "Colon-separated component names",
        ),
        SettingValue(
            id = Settings.Secure.DEFAULT_INPUT_METHOD,
            category = SettingCategory.ACCESSIBILITY,
            name = "Default Input Method",
            description = "Component ID of the default keyboard",
            rawHelp = "Component name string",
        ),
        SettingValue(
            id = Settings.Secure.ENABLED_INPUT_METHODS,
            category = SettingCategory.ACCESSIBILITY,
            name = "Input Method List",
            description = "List of enabled keyboards and input methods",
            rawHelp = "Colon-separated component names",
        ),
        SettingValue(
            id = Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE,
            category = SettingCategory.ACCESSIBILITY,
            name = "Selected Input Method Subtype",
            description = "Default subtype of the current input method",
            rawHelp = "Component name string",
        ),
        SettingValue(
            id = Settings.Secure.INPUT_METHOD_SELECTOR_VISIBILITY,
            category = SettingCategory.ACCESSIBILITY,
            name = "Input Method Selector",
            description = "Whether the input method selector is always shown",
            rawHelp = "0=auto, 1=always show",
        ),
        // Developer
        SettingValue(
            id = Settings.Global.ADB_ENABLED,
            category = SettingCategory.DEVELOPER,
            name = "ADB",
            description = "Whether ADB over USB is enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            category = SettingCategory.DEVELOPER,
            name = "Development Settings",
            description = "Whether developer options menu is available",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.WAIT_FOR_DEBUGGER,
            category = SettingCategory.DEVELOPER,
            name = "Wait For Debugger",
            description = "Wait for debugger to attach before launching apps",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.ALWAYS_FINISH_ACTIVITIES,
            category = SettingCategory.DEVELOPER,
            name = "Always Finish Activities",
            description = "Destroy every activity as soon as the user leaves it",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        SettingValue(
            id = Settings.Global.WINDOW_ANIMATION_SCALE,
            category = SettingCategory.DEVELOPER,
            name = "Window Animation Scale",
            description = "Scale factor for window open/close animations",
            rawHelp = "Float (0.0=off, 0.5, 1.0, 1.5, 2.0, 5.0, 10.0)",
        ),
        SettingValue(
            id = Settings.Global.TRANSITION_ANIMATION_SCALE,
            category = SettingCategory.DEVELOPER,
            name = "Transition Animation Scale",
            description = "Scale factor for activity transition animations",
            rawHelp = "Float (0.0=off, 0.5, 1.0, 1.5, 2.0, 5.0, 10.0)",
        ),
        SettingValue(
            id = Settings.Global.ANIMATOR_DURATION_SCALE,
            category = SettingCategory.DEVELOPER,
            name = "Animator Duration Scale",
            description = "Scale factor for all animator-based animations",
            rawHelp = "Float (0.0=off, 0.5, 1.0, 1.5, 2.0, 5.0, 10.0)",
        ),
        // Power
        SettingValue(
            id = Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            category = SettingCategory.POWER,
            name = "Stay On While Plugged In",
            description = "Keep screen on while charging",
            rawHelp = "Bitmask: usb=1, ac=2, wireless=4",
        ),
        SettingValue(
            id = Settings.Global.USB_MASS_STORAGE_ENABLED,
            category = SettingCategory.POWER,
            name = "USB Mass Storage",
            description = "Whether USB mass storage is enabled",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
        // Device
        SettingValue(
            id = Settings.Global.DEVICE_NAME,
            category = SettingCategory.DEVICE,
            name = "Device Name",
            description = "Bluetooth and system device name",
            rawHelp = "Any string",
        ),
        SettingValue(
            id = Settings.Global.DEVICE_PROVISIONED,
            category = SettingCategory.DEVICE,
            name = "Device Provisioned",
            description = "Whether the device has been provisioned",
            rawHelp = "0=not provisioned, 1=provisioned",
        ),
        SettingValue(
            id = Settings.Global.USE_GOOGLE_MAIL,
            category = SettingCategory.DEVICE,
            name = "Use Google Mail",
            description = "Show \"Google Mail\" instead of \"Gmail\"",
            rawHelp = "0 for disabled, 1 for enabled",
        ),
    )

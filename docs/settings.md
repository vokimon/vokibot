# Android System Settings

Android settings are exposed through three main namespaces:

`Settings.System`:

- User-level configuration
- UI and device behavior settings
- Lowest restriction level
- `WRITE_SETTINGS` permission

`Settings.Secure`:

- Sensitive system settings
- Accessibility, location-related configuration, protected flags
- Write requires `WRITE_SECURE_SETTINGS` (via ADB or system app)
- or device owner privileges in some cases

`Settings.Global`:

- Device-wide system configuration
- Developer options, network behavior, system-wide flags
- `WRITE_SECURE_SETTINGS`
- or system-level privileges / ADB-granted access


Internally, all settings are stored as a simple **key -> string value map**, without a strict typed schema at the system level.

## Modify a Setting

Settings are modified through the `ContentResolver` using typed convenience methods:

```kotlin
Settings.System.putInt(contentResolver, key, value)
Settings.System.putString(contentResolver, key, value)
Settings.System.putFloat(contentResolver, key, value)
```

Example:

```kotlin
Settings.System.putInt(
    context.contentResolver,
    Settings.System.SCREEN_OFF_TIMEOUT,
    60_000
)
```

Notice:

- API is typed but the underlying storage is string based
- You could write whichever type, consumers try to parse the expected type and fallback to default


## Where to Find Available Settings

There is no complete official registry of all settings.

### Official (partial) documentation

- Global <https://developer.android.com/reference/android/provider/Settings.Global>
- System <

* Android SDK: `android.provider.Settings`
* AOSP documentation (incomplete and version-dependent)

### Open-source reference (most complete source)

* Android Open Source Project (AOSP):

  * `frameworks/base/core/java/android/provider/Settings.java`
  * System service implementations in `frameworks/base/services/`

These define:

* Known keys
* Default behavior
* Permission checks (partially)

---

## 4. Runtime Inspection of Available Settings

There is no official API to list all available settings, but partial inspection is possible.

### Option A: Reflection on SDK constants

```kotlin
Settings.System::class.java.fields
Settings.Secure::class.java.fields
Settings.Global::class.java.fields
```

This returns:

* Constant definitions exposed by the SDK
* Not guaranteed to match runtime availability

---

### Option B: Query actual stored values

```kotlin
val cursor = contentResolver.query(
    Settings.System.CONTENT_URI,
    null,
    null,
    null,
    null
)
```

This can return:

* Existing keys on the device
* OEM-specific settings (if accessible)

Limitations:

* Not all keys are exposed
* Access may be restricted on modern Android versions

---

### Option C: Intent-based discovery (indirect)

```kotlin
packageManager.queryIntentActivities(intent, 0)
```

Used for discovering:

* Settings screens
* System panels
* Available configuration activities

---

### Option D: Accessibility-based inspection

Accessibility Services can inspect:

* Settings UI structure
* Toggle availability
* Runtime visible options

This is UI-level inspection, not API-level discovery.

---

## 5. Permission Model

Permissions depend on the settings namespace:

### Settings.System

* Requires: `WRITE_SETTINGS`
* User must explicitly grant via system settings screen
* Cannot be granted via normal runtime permission dialog

---

### Settings.Secure

* Requires: `WRITE_SECURE_SETTINGS`
* Can only be granted via:

  * ADB (`pm grant`)
  * System app signature
  * Device Owner policies (limited cases)

---

### Settings.Global

* Requires: `WRITE_SECURE_SETTINGS`
* Often further restricted depending on Android version

---

## 6. Key Architectural Implications

* Settings are not strongly typed at system level
* Validity is enforced at read-time, not write-time
* Availability is device and version dependent
* There is no authoritative runtime registry of all settings

---

## 7. Practical Model for Automation Apps

A robust automation system should treat settings as:

* A **key-value store (string-based)**
* With a **separate typed metadata layer**

Example abstraction:

```kotlin
data class SettingDefinition(
    val key: String,
    val namespace: SettingsNamespace,
    val type: SettingType,
    val requiredPermission: PermissionLevel
)
```

Where:

* `type` drives UI + validation
* `namespace` drives access method
* `requiredPermission` determines execution capability

---

## Summary

* Android settings are fundamentally untyped string-based key-value pairs.
* System APIs provide typed wrappers only for developer convenience.
* There is no complete official registry of available settings.
* Runtime discovery is partial and unreliable.
* Permissions depend on namespace and system privilege level.
* A custom schema layer is required for any automation framework.

---

## Appendix: Settings Catalog

Comprehensive list of non-deprecated settings documented in the Android SDK.
Grouped by logical category. Namespace in parentheses after the category.

Based on Android SDK API 36 (compileSdk 36).

- <https://developer.android.com/reference/android/provider/Settings.System>
- <https://developer.android.com/reference/android/provider/Settings.Secure>
- <https://developer.android.com/reference/android/provider/Settings.Global>

AOSP source:

- `frameworks/base/core/java/android/provider/Settings.java`

### Display (System)

- `SCREEN_BRIGHTNESS`: (Int) Screen backlight brightness, 0-255
    - UI: Ranged value
- `SCREEN_BRIGHTNESS_MODE`: (Int) 0=manual, 1=automatic brightness
    - UI: Toggle
- `SCREEN_OFF_TIMEOUT`: (Int) Milliseconds before screen turns off
    - UI: Enum("15s"=15000, "30s"=30000, "1m"=60000, "2m"=120000, "5m"=300000, "10m"=600000, "30m"=1800000)
- `USER_ROTATION`: (Int) Default rotation: 0=0deg, 1=90deg, 2=180deg, 3=270deg
    - UI: Enum("0°"=0, "90°"=1, "180°"=2, "270°"=3)
- `ACCELEROMETER_ROTATION`: (Int) 0=manual, 1=accelerometer controls rotation
    - UI: Enum("Manual", "Auto") maybe Toggle
- `FONT_SCALE`: (Float) Font scaling factor, 1.0 is default
    - UI: Enum("0.5", "0.7", "0.85", "1.0"=1.0, "1.15", "1.3", "1.5", "2.0")

### Sound (System)

- `VOLUME_SYSTEM`: (Int) System/notifications volume, 0-15
    - UI: Ranged value
- `VOLUME_RING`: (Int) Ringer volume, 0-15
    - UI: Ranged value
- `VOLUME_MUSIC`: (Int) Music/media/gaming volume, 0-15
    - UI: Ranged value
- `VOLUME_ALARM`: (Int) Alarm volume, 0-15
    - UI: Ranged value
- `VOLUME_NOTIFICATION`: (Int) Notification volume, 0-15
    - UI: Ranged value
- `VOLUME_VOICE`: (Int) Voice call volume, 0-15
    - UI: Ranged value
- `VOLUME_BLUETOOTH_SCO`: (Int) Bluetooth SCO volume, 0-15
    - UI: Ranged value
- `RINGTONE`: (String) Content URI of default ringtone
    - UI: URI
- `NOTIFICATION_SOUND`: (String) Content URI of default notification sound
    - UI: URI
- `ALARM_ALERT`: (String) Content URI of default alarm alert
    - UI: URI
- `DTMF_TONE_WHEN_DIALING`: (Int) 0=off, 1=on, audible DTMF tones
    - UI: Toggle
- `SOUND_EFFECTS_ENABLED`: (Int) 0=off, 1=on, key clicks and lid sounds
    - UI: Toggle
- `HAPTIC_FEEDBACK_ENABLED`: (Int) 0=off, 1=on, haptic feedback on long press etc.
    - UI: Toggle
- `MODE_RINGER_STREAMS_AFFECTED`: (Int) Bitmask: voice_call=1, system=2, ring=4, music=8, alarm=16, notification=32, bt_sco=64
    - UI: Flags("Voice call", "System", "Ring", "Music", "Alarm", "Notification", "BT SCO")
- `MUTE_STREAMS_AFFECTED`: (Int) Bitmask: voice_call=1, system=2, ring=4, music=8, alarm=16, notification=32, bt_sco=64
    - UI: Flags("Voice call", "System", "Ring", "Music", "Alarm", "Notification", "BT SCO")
- `VIBRATE_ON`: (Int) Bitmask: call=1, call_cdma=2, notification=4, chat=8, calendar=16, hangup=32
    - UI: Flags("Incoming call", "CDMA call", "Notification", "Chat message", "Calendar", "Hang up")

### Text Input (System)

- `TEXT_AUTO_CAPS`: (Int) 0=off, 1=on, auto capitalize in text editors
    - UI: Toggle
- `TEXT_AUTO_PUNCTUATE`: (Int) 0=off, 1=on, auto punctuate in text editors
    - UI: Toggle
- `TEXT_AUTO_REPLACE`: (Int) 0=off, 1=on, auto replace (AutoText) in text editors
    - UI: Toggle
- `TEXT_SHOW_PASSWORD`: (Int) 0=off, 1=on, show password characters briefly
    - UI: Toggle

### Time (System)

- `TIME_12_24`: (String) "12" or "24" hour format
    - UI: Enum("12h", "24h")

### Bluetooth (System)

- `BLUETOOTH_DISCOVERABILITY`: (Int) 0=disabled, 1=enabled, discoverable mode
    - UI: Enum("Disabled", "Enabled")
- `BLUETOOTH_DISCOVERABILITY_TIMEOUT`: (Int) Discoverability timeout in seconds
    - UI: Enum("120s", "300s", "600s")

### Call (System)

- `END_BUTTON_BEHAVIOR`: (Int) 0=end call, 1=go to caller log
    - UI: Enum("End call", "Caller log")

### System Misc (System)

- `APPEND_FOR_LAST_AUDIBLE`: (Int) Bitmask: voice_call=1, system=2, ring=4, music=8, alarm=16, notification=32, bt_sco=64
    - UI: Flags("Voice call", "System", "Ring", "Music", "Alarm", "Notification", "BT SCO")

### Accessibility (Secure)

- `ACCESSIBILITY_ENABLED`: (Int) 0=off, 1=on
    - UI: Toggle
- `ACCESSIBILITY_DISPLAY_INVERSION_ENABLED`: (Int) 0=off, 1=on, color inversion
    - UI: Toggle
- `ACCESSIBILITY_SPEAK_PASSWORD`: (Int) 0=off, 1=on, speak passwords aloud
    - UI: Toggle
- `TOUCH_EXPLORATION_ENABLED`: (Int) 0=off, 1=on, Touch exploration (TalkBack)
    - UI: Toggle

### Input (Secure)

- `DEFAULT_INPUT_METHOD`: (String) Component ID of default IME
    - UI: Dynamic list (from InputMethodManager enabled input methods)
- `ENABLED_INPUT_METHODS`: (String, colon-separated list) Enabled input methods
    - UI: Dynamic list (colon-separated, from InputMethodManager enabled input methods)
- `SELECTED_INPUT_METHOD_SUBTYPE`: (String) Default IME subtype
    - UI: Dynamic list (from InputMethodManager input method subtypes)
- `INPUT_METHOD_SELECTOR_VISIBILITY`: (Int) 0=auto, 1=always show, IME selector
    - UI: Enum("Auto", "Always show")

### Location (Secure)

- `LOCATION_PROVIDERS_ALLOWED`: (String, comma-separated list) Allowed providers
    - UI: Dynamic list (comma-separated, from LocationManager.getProviders())
- `ALLOW_MOCK_LOCATION`: (Int) 0=off, 1=on, allow mock locations
    - UI: Toggle

### Lock Screen (Secure)

- `LOCK_PATTERN_ENABLED`: (Int) 0=off, 1=on, autolock enabled
    - UI: Toggle
- `LOCK_PATTERN_VISIBLE`: (Int) 0=off, 1=on, pattern visible while drawing
    - UI: Toggle

### Text-to-Speech (Secure)

- `TTS_DEFAULT_PITCH`: (Int) Pitch value, 10-500
    - UI: Ranged value
- `TTS_DEFAULT_RATE`: (Int) Speech rate, 10-300
    - UI: Ranged value
- `TTS_DEFAULT_SYNTH`: (String) Package name of default TTS engine
    - UI: Dynamic list (from TextToSpeech.Engine available engines)
- `TTS_ENABLED_PLUGINS`: (String, space-separated list) Enabled TTS plugins
    - UI: Dynamic list (space-separated, from TextToSpeech.Engine available plugins)

### Accessibility Services (Secure)

- `ENABLED_ACCESSIBILITY_SERVICES`: (String, colon-separated list) Enabled services
    - UI: Dynamic list (colon-separated, from AccessibilityManager enabled services)

### Connectivity (Global)

- `AIRPLANE_MODE_ON`: (Int) 0=off, 1=on
    - UI: Toggle
- `BLUETOOTH_ON`: (Int) 0=off, 1=on
    - UI: Toggle
- `WIFI_ON`: (Int) 0=off, 1=on
    - UI: Toggle
- `DATA_ROAMING`: (Int) 0=off, 1=on
    - UI: Toggle
- `NETWORK_PREFERENCE`: (Int) Preferred network(s)
    - UI: Ranged value
- `HTTP_PROXY`: (String) Global HTTP proxy as host:port
    - UI: Free text

### Developer (Global)

- `ADB_ENABLED`: (Int) 0=off, 1=on, ADB over USB
    - UI: Toggle
- `DEVELOPMENT_SETTINGS_ENABLED`: (Int) 0=off, 1=on, developer options
    - UI: Toggle
- `WAIT_FOR_DEBUGGER`: (Int) 0=off, 1=on, wait for debugger on launch
    - UI: Toggle
- `ALWAYS_FINISH_ACTIVITIES`: (Int) 0=off, 1=on, finish activities aggressively
    - UI: Toggle

### Animation (Global)

- `WINDOW_ANIMATION_SCALE`: (Float) Scale factor for window animations
    - UI: Enum("Off"=0.0, "0.5x"=0.5, "1x"=1.0, "1.5x"=1.5, "2x"=2.0, "5x"=5.0, "10x"=10.0)
- `TRANSITION_ANIMATION_SCALE`: (Float) Scale factor for activity transitions
    - UI: Enum("Off"=0.0, "0.5x"=0.5, "1x"=1.0, "1.5x"=1.5, "2x"=2.0, "5x"=5.0, "10x"=10.0)
- `ANIMATOR_DURATION_SCALE`: (Float) Scale factor for Animator-based animations
    - UI: Enum("Off"=0.0, "0.5x"=0.5, "1x"=1.0, "1.5x"=1.5, "2x"=2.0, "5x"=5.0, "10x"=10.0)

### Time (Global)

- `AUTO_TIME`: (Int) 0=off, 1=on, auto-set clock from network
    - UI: Toggle
- `AUTO_TIME_ZONE`: (Int) 0=off, 1=on, auto-set time zone from network
    - UI: Toggle

### Power (Global)

- `STAY_ON_WHILE_PLUGGED_IN`: (Int) Bitmask: usb=1, ac=2, wireless=4
    - UI: Flags("USB", "AC", "Wireless")

### Device (Global)

- `DEVICE_NAME`: (String) Bluetooth/device name
    - UI: Free text
- `DEVICE_PROVISIONED`: (Int) 0=not provisioned, 1=provisioned
    - UI: Toggle
- `MODE_RINGER`: (Int) 0=normal, 1=vibrate, 2=silent
    - UI: Enum("Normal", "Vibrate", "Silent")
- `USB_MASS_STORAGE_ENABLED`: (Int) 0=off, 1=on
    - UI: Toggle
- `INSTALL_NON_MARKET_APPS`: (Int) 0=off, 1=on, allow non-Market installs
    - UI: Toggle
- `USE_GOOGLE_MAIL`: (Int) 0=off, 1=on, show "Google Mail" instead of "Gmail"
    - UI: Toggle

### Wi-Fi (Global)

- `WIFI_SLEEP_POLICY`: (Int) 0=default, 1=never while plugged, 2=never
    - UI: Enum("Default", "Never while plugged", "Never")
- `WIFI_MAX_DHCP_RETRY_COUNT`: (Int) Max DHCP retries
    - UI: Ranged value
- `WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON`: (Int) 0=off, 1=on, notify open networks
    - UI: Toggle
- `WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY`: (Int) Seconds before repeating notification
    - UI: Ranged value
- `WIFI_NUM_OPEN_NETWORKS_KEPT`: (Int) Max open networks to remember
    - UI: Ranged value
- `WIFI_WATCHDOG_ON`: (Int) 0=off, 1=on, Wi-Fi watchdog
    - UI: Toggle
- `WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS`: (Int) Wakelock timeout in ms
    - UI: Ranged value

### Radio (Global)

- `AIRPLANE_MODE_RADIOS`: (String, comma-separated list) Radios disabled in airplane mode
    - UI: Dynamic list (comma-separated, from Settings.Global RADIO_BLUETOOTH, RADIO_CELL, RADIO_NFC, RADIO_WIFI)

### Other (Global)

- `SHOW_PROCESSES`: (Int) 0=off, 1=on, show process CPU usage meter
    - UI: Toggle



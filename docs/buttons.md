# Physical buttons

Analysis document exploring feasibility of using physical buttons as triggers.


## Permission levels

Permission classification for an automation engine:

- **None**: No permission required. Events available to the application when it has focus.
- **Automatic**: Declared in `AndroidManifest.xml`, granted automatically at install time.
- **Runtime**: User accepts a permission dialog.
- **Special**: User must manually enable access in Android settings.
- **Privileged**: Restricted to system/privileged applications.
  Sometimes grantable through `adb` on rooted/debug/system environments.
- **Signed**: Granted only to applications signed with the same certificate as the application/framework that defines the permission.
  Usually only available to OEM/system applications.


## Methods

Each method is a different code pattern to receive button events.
Methods are independent of which button is pressed.


### Activity key events

- **Availability:** Full
- **Permission:** None

Receives `KEYCODE_*` events when the application Activity is in the foreground.
Cannot intercept events when another app is focused.

Discovery: enumerate available keycodes across connected input devices.

```kotlin
val inputManager = context.getSystemService(InputManager::class.java)
val deviceIds = inputManager.inputDeviceIds

val keyCodes = intArrayOf(
    KeyEvent.KEYCODE_VOLUME_UP,
    KeyEvent.KEYCODE_VOLUME_DOWN,
    KeyEvent.KEYCODE_CAMERA,
    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
)
val hasKeys = KeyCharacterMap.deviceHasKeys(*keyCodes)
// hasKeys[i] == true if any device supports keyCodes[i]
```

Dispatch: override Activity key handlers.

```kotlin
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    trigger("KEYCODE_$keyCode")
    return super.onKeyDown(keyCode, event)
}
```

Device filtering: `event.device` provides device information.

```kotlin
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    val device = event?.device
    // device?.isExternal — true for USB/Bluetooth, false for built-in
    // device?.bluetoothAddress — Bluetooth MAC address
    // device?.vendorId / productId — USB device identification

    if (selectedDeviceId != null && event?.deviceId != selectedDeviceId) {
        return super.onKeyDown(keyCode, event)
    }
    trigger("KEYCODE_$keyCode")
    return super.onKeyDown(keyCode, event)
}
```


### Accessibility service

- **Availability:** Full
- **Permission:** Special (`BIND_ACCESSIBILITY_SERVICE`)

Uses `AccessibilityService.onKeyEvent()` to intercept key events globally
(only one service can filter at a time).
If TalkBack or another key-filtering service is active, this service won't receive events.

Discovery: same as Activity key events.

Dispatch: implement `onKeyEvent()` in the service.

```kotlin
override fun onKeyEvent(event: KeyEvent?): Boolean {
    trigger("KEYCODE_${event?.keyCode}")
    return true
}
```

Device filtering: same as Activity key events.

```kotlin
override fun onKeyEvent(event: KeyEvent?): Boolean {
    if (selectedDeviceId != null && event?.deviceId != selectedDeviceId) {
        return false
    }
    trigger("KEYCODE_${event?.keyCode}")
    return true
}
```

User action required:

* Settings → Accessibility → Installed services → Enable


Available media keycodes via `onKeyEvent()`:

* `KEYCODE_MEDIA_PLAY`
* `KEYCODE_MEDIA_PAUSE`
* `KEYCODE_MEDIA_PLAY_PAUSE`
* `KEYCODE_MEDIA_STOP`
* `KEYCODE_MEDIA_NEXT`
* `KEYCODE_MEDIA_PREVIOUS`
* `KEYCODE_MEDIA_REWIND`
* `KEYCODE_MEDIA_FAST_FORWARD`
* `KEYCODE_MEDIA_RECORD`
* `KEYCODE_HEADSETHOOK`


### MediaSession

- **Availability:** Full
- **Permission:** None

Uses Android media button routing for headphones, Bluetooth media buttons,
play/pause style controls. Does NOT handle volume buttons —
volume is routed through AudioManager, not MediaSession.

Note: This method interferes with actual media players (Spotify, YouTube, etc.).
When a media player is active, it will receive the events instead.
The trigger won't work while other media apps are playing.

Note: Device filtering is not available.
Callback methods do not expose source device information.
Events are routed by system media priority, not by input device.

Discovery: standard media actions are predefined constants.

```kotlin
val mediaActions = listOf(
    PlaybackState.ACTION_PLAY,
    PlaybackState.ACTION_PAUSE,
    PlaybackState.ACTION_PLAY_PAUSE,
    PlaybackState.ACTION_SKIP_TO_NEXT,
    PlaybackState.ACTION_SKIP_TO_PREVIOUS,
    PlaybackState.ACTION_FAST_FORWARD,
    PlaybackState.ACTION_REWIND,
    PlaybackState.ACTION_STOP
)
```

Dispatch: implement `MediaSession.Callback` methods.

```kotlin
session.setCallback(object : MediaSession.Callback() {

    override fun onPlay() {
        trigger("MEDIA_PLAY")
    }

    override fun onPause() {
        trigger("MEDIA_PAUSE")
    }

    override fun onSkipToNext() {
        trigger("MEDIA_NEXT")
    }

    override fun onSkipToPrevious() {
        trigger("MEDIA_PREVIOUS")
    }
})
```


### OEM SDK

- **Availability:** Vendor-dependent
- **Permission:** Vendor-dependent (Runtime, Special, Privileged, or Signed)

Manufacturer-specific SDK for dedicated hardware buttons.

Note: Device filtering depends on vendor SDK capabilities.
Check vendor documentation for available filtering options.

Discovery: vendor-specific enumeration.

```kotlin
// Zebra example
val intent = Intent("com.symbol.datawedge.api.ACTION")
intent.putExtra("com.symbol.datawedge.api.ENUMERATE_SCANNERS", "")
sendBroadcast(intent)
// Results arrive via BroadcastReceiver
```

Examples:

* Zebra
* Honeywell
* Samsung Knox integrations

Example pattern:

```kotlin
oemButtonManager.registerListener {
    trigger("OEM_BUTTON", it.id)
}
```


### Bluetooth input

- **Availability:** Full
- **Permission:** `BLUETOOTH_CONNECT` (Runtime, API 31+; Normal below)

For Bluetooth devices beyond media controls (custom buttons, special function keys),
the device uses HID profile. HID devices appear as input devices and send standard
`KEYCODE_*` events, captured through `AccessibilityService.onKeyEvent()` or `InputDevice` API.

Note: MediaSession already handles media controls (play/pause/next/prev).
This method is for other buttons on Bluetooth remotes and controllers.

Discovery: enumerate Bluetooth input devices.

```kotlin
val inputManager = getSystemService(InputManager::class.java)
val devices = inputManager.inputDeviceIds.mapNotNull { inputManager.getInputDevice(it) }
val btDevices = devices.filter { it.bluetoothAddress != null }

for (device in btDevices) {
    val possibleKeys = intArrayOf(
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
    )
    val hasKeys = device.hasKeys(*possibleKeys)
    val supported = possibleKeys.filterIndexed { index, _ -> hasKeys[index] }
    // Present: "${device.name} (${device.bluetoothAddress}): $supported"
}
```

Dispatch: filter events by Bluetooth address.

```kotlin
val selectedBtAddress: String? = "AA:BB:CC:DD:EE:FF"

override fun onKeyEvent(event: KeyEvent?): Boolean {
    val device = event?.device
    if (device?.bluetoothAddress == null) return false
    if (selectedBtAddress != null && device.bluetoothAddress != selectedBtAddress) {
        return false
    }
    trigger("KEYCODE_${event.keyCode}")
    return true
}
```


### Sensors

- **Availability:** Full
- **Permission:** None

Note: Continuous sensor monitoring drains battery significantly.
Use `SensorManager.registerListener()` with appropriate sampling rates
(`TYPE_DELAY_NORMAL`, `TYPE_DELAY_UI`, or `TYPE_DELAY_GAME`).

Note: Filtering is by sensor type and name, not by input device.
Each sensor is identified by `sensor.name`, `sensor.type`, `sensor.vendor`.

Discovery: list all sensors on the device.

```kotlin
val sensorManager =
    getSystemService(Context.SENSOR_SERVICE)
        as SensorManager

val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
for (sensor in sensors) {
    Log.d(TAG, "Sensor: ${sensor.name}, type: ${sensor.type}, vendor: ${sensor.vendor}")
}
```

Available sensors:

* Accelerometer
* Gyroscope
* Proximity
* Light

Dispatch: register listener for sensor events.

```kotlin
val sensorManager =
    getSystemService(Context.SENSOR_SERVICE)
        as SensorManager

val sensor =
    sensorManager.getDefaultSensor(
        Sensor.TYPE_ACCELEROMETER
    )

sensorManager.registerListener(
    object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            trigger("SENSOR_${event.sensor.type}", event.values[0])
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    },
    sensor,
    SensorManager.SENSOR_DELAY_NORMAL
)
```


## Buttons

Each button type has different available methods.


### Discovering input devices and keycodes

Discovery: enumerate input devices and check available keycodes.

Enumerate devices for user selection:

```kotlin
val inputManager = context.getSystemService(InputManager::class.java)
val devices = inputManager.inputDeviceIds.mapNotNull { inputManager.getInputDevice(it) }

for (device in devices) {
    val type = when {
        device.isVirtual -> "Virtual"
        device.isExternal -> "External"
        else -> "Built-in"
    }
    val connection = when {
        device.bluetoothAddress != null -> "Bluetooth"
        device.isExternal -> "USB"
        else -> "Integrated"
    }
    // Present: "${device.name} ($type, $connection)"
}
```

Keycodes to check:

```kotlin
val keyCodes = intArrayOf(
    KeyEvent.KEYCODE_VOLUME_UP,
    KeyEvent.KEYCODE_VOLUME_DOWN,
    KeyEvent.KEYCODE_CAMERA,
    KeyEvent.KEYCODE_MEDIA_PLAY,
    KeyEvent.KEYCODE_MEDIA_PAUSE,
    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
    KeyEvent.KEYCODE_MEDIA_STOP,
    KeyEvent.KEYCODE_MEDIA_NEXT,
    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
    KeyEvent.KEYCODE_MEDIA_REWIND,
    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
    KeyEvent.KEYCODE_MEDIA_RECORD,
    KeyEvent.KEYCODE_HEADSETHOOK,
)
```

Per-device check:

```kotlin
val device = inputManager.getInputDevice(deviceId)
val hasKeys = device.hasKeys(*keyCodes)
val supported = keyCodes.filterIndexed { index, _ -> hasKeys[index] }
```

System-wide check (any device):

```kotlin
val hasKeys = KeyCharacterMap.deviceHasKeys(*keyCodes)
val supported = keyCodes.filterIndexed { index, _ -> hasKeys[index] }
```

Device filtering: store selected device ID (or null for any device).

```kotlin
val selectedDeviceId: Int? = null // null = respond to all devices

override fun onKeyEvent(event: KeyEvent?): Boolean {
    if (selectedDeviceId != null && event?.deviceId != selectedDeviceId) {
        return false
    }
    trigger("KEYCODE_${event?.keyCode}")
    return true
}
```

Media keycodes (standard Android):

* `KEYCODE_MEDIA_PLAY`, `KEYCODE_MEDIA_PAUSE`, `KEYCODE_MEDIA_PLAY_PAUSE`
* `KEYCODE_MEDIA_STOP`, `KEYCODE_MEDIA_NEXT`, `KEYCODE_MEDIA_PREVIOUS`
* `KEYCODE_MEDIA_REWIND`, `KEYCODE_MEDIA_FAST_FORWARD`, `KEYCODE_MEDIA_RECORD`
* `KEYCODE_HEADSETHOOK`


### Volume buttons

- Keycodes: `KEYCODE_VOLUME_UP`, `KEYCODE_VOLUME_DOWN`
- Methods: Activity key events, Accessibility service


### Camera button

- Keycodes: `KEYCODE_CAMERA`
- Methods: Activity key events, Accessibility service
- Note: Rare on modern devices


### Media buttons

- Keycodes: `KEYCODE_MEDIA_PLAY`, `KEYCODE_MEDIA_PAUSE`, `KEYCODE_MEDIA_PLAY_PAUSE`, `KEYCODE_MEDIA_STOP`, `KEYCODE_MEDIA_NEXT`, `KEYCODE_MEDIA_PREVIOUS`, `KEYCODE_MEDIA_REWIND`, `KEYCODE_MEDIA_FAST_FORWARD`, `KEYCODE_MEDIA_RECORD`, `KEYCODE_HEADSETHOOK`
- Methods: Activity key events, Accessibility service, MediaSession
- Note: MediaSession interferes with actual media players
- Note: MediaSession does not support device filtering


### Programmable hardware buttons

- Methods: Accessibility service, OEM SDK
- Note: OEM SDK device filtering depends on vendor implementation


### Bluetooth buttons and remotes

- Methods: Bluetooth input, MediaSession
- Note: Bluetooth input supports device filtering by Bluetooth address
- Note: MediaSession does not support device filtering


### Power button

- Interactions: Single press (locks screen), Long press (power menu)
- Methods: Accessibility service
- Note: Double press is handled by system (camera/assist), not interceptable



## Events

Gesture detection for button events.
Android provides raw button events; gestures are implemented by the automation engine.

Standard Android timing thresholds:

* Long press: 500ms
* Double click: 300ms


### Single click

```text
DOWN
UP
```



### Long press

```text
DOWN
(wait > threshold)
UP
```



### Double click

```text
DOWN
UP
(wait < threshold)
DOWN
UP
```



### Combination

Example: Volume up + volume down pressed simultaneously.

```text
VOLUME_UP DOWN
+
VOLUME_DOWN DOWN
```

Implementation:

```kotlin
data class ButtonEvent(
    val keyCode: Int,
    val action: Int,
    val timestamp: Long
)
```

The trigger engine keeps a short event history and matches patterns.


## Accessibility gestures

Global touch screen gestures detected by the accessibility service.
Requires `FLAG_REQUEST_TOUCH_EXPLORATION_MODE` in service configuration.

Note: Device filtering is not available.
Gestures are system-wide, not attributed to specific input devices.

Discovery: gestures are predefined by Android, not dynamic.

Available gestures:

Single finger:

* Swipe up, down, left, right
* Swipe up and down, down and up
* Swipe left and right, right and left
* Double tap, double tap and hold

Two finger:

* Swipe up, down, left, right
* Double tap

Three finger:

* Swipe up, down, left, right
* Double tap

Four finger:

* Swipe up, down, left, right
* Double tap

Dispatch: implement `onGesture()` in the service.

```kotlin
override fun onGesture(gestureId: Int): Boolean {
    when (gestureId) {
        GESTURE_SWIPE_UP -> trigger("SWIPE_UP")
        GESTURE_SWIPE_DOWN -> trigger("SWIPE_DOWN")
        GESTURE_2_FINGER_DOUBLE_TAP -> trigger("TWO_FINGER_TAP")
        // ... other gestures
    }
    return true
}
```


## Fingerprint gestures

Swipe gestures on the fingerprint sensor (API 26+).
Requires `USE_BIOMETRIC` permission and `FLAG_REQUEST_FINGERPRINT_GESTURES`.

Note: Device filtering is not available.
Always from fingerprint sensor; no device discrimination needed.

Discovery: check `FingerprintGestureController.isAvailable()`.

Available gestures:

* Swipe up
* Swipe down
* Swipe left
* Swipe right

Dispatch: register callback in `onServiceConnected()`.

```kotlin
override fun onServiceConnected() {
    val controller = fingerprintGestureController
    controller?.registerFingerprintGestureCallback(
        object : FingerprintGestureController.FingerprintGestureCallback() {
            override fun onGestureCompleted(gestureId: Int) {
                when (gestureId) {
                    FINGERPRINT_GESTURE_SWIPE_UP -> trigger("FINGERPRINT_UP")
                    FINGERPRINT_GESTURE_SWIPE_DOWN -> trigger("FINGERPRINT_DOWN")
                    FINGERPRINT_GESTURE_SWIPE_LEFT -> trigger("FINGERPRINT_LEFT")
                    FINGERPRINT_GESTURE_SWIPE_RIGHT -> trigger("FINGERPRINT_RIGHT")
                }
            }
        },
        null
    )
}
```

# Extended capabilites for Android Automation Apps

NOTICE: None of those methods are implemented yet.
This documentation is just for analysis.

Future versions of VokiBot may need running actions that require extra capabilites.
This document compiles known mechanisms
of extending them in Android without requiring root access.

## `WRITE_SECURE_SETTINGS`

`WRITE_SECURE_SETTINGS` is a signature-level permission normally reserved for system applications.

A developer or power user can grant it manually using ADB.

### How the user enables it

Install the application normally.

Then run:

```bash
adb shell pm grant vokibot.canvoki.net android.permission.WRITE_SECURE_SETTINGS
```

The permission remains granted until:

* The app is uninstalled
* The permission is revoked
* Some OEM firmware resets permissions

### Application requirements

Manifest:

```xml
<uses-permission
    android:name="android.permission.WRITE_SECURE_SETTINGS"/>
```

No runtime permission request is possible.

The application should detect whether the permission is currently granted.

Example:

```kotlin
// Run-time check
val granted =
    ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.WRITE_SECURE_SETTINGS
    ) == PackageManager.PERMISSION_GRANTED
```

### What it enables

Examples include:

* Screen timeout changes
* Auto-rotation settings
* Some accessibility-related settings
* Animation scale settings
* Developer options settings
* Various Secure and Global settings

Capabilities vary by Android version and OEM.

Not all settings remain writable on modern Android releases.

Examples generally NOT allowed:

* Airplane mode on recent Android versions
* Fully controlling location services
* Arbitrary system-level administration

### Kotlin examples

Disable auto-rotation:

```kotlin
Settings.System.putInt(
    context.contentResolver,
    Settings.System.ACCELEROMETER_ROTATION,
    0
)
```

Set screen timeout:

```kotlin
Settings.System.putInt(
    context.contentResolver,
    Settings.System.SCREEN_OFF_TIMEOUT,
    60_000
)
```

Change animation scale:

```kotlin
Settings.Global.putFloat(
    context.contentResolver,
    Settings.Global.ANIMATOR_DURATION_SCALE,
    0f
)
```

## Accessibility Service

An Accessibility Service can observe UI events and perform UI actions on behalf of the user.

This is how many automation tools interact with apps that expose no public API.

### How the user enables it

The application opens Accessibility Settings:

```kotlin
startActivity(
    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
)
```

The user manually enables the service.

Android always requires explicit user consent.

### Application requirements

Manifest:

```xml
<service
    android:name=".MyAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="false">

    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>

    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config"/>
</service>
```

Accessibility configuration XML:

```xml
<accessibility-service
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:canPerformGestures="true"
    android:canRetrieveWindowContent="true"/>
```

### What it enables

Examples:

* Detect foreground apps
* Read visible UI text
* Click buttons
* Enter text into fields
* Scroll lists
* Perform gestures
* Dismiss dialogs
* Automate apps without APIs

This is the closest mechanism to "human interaction automation".

### Kotlin examples

Perform global Back:

```kotlin
performGlobalAction(
    AccessibilityService.GLOBAL_ACTION_BACK
)
```

Perform global Home:

```kotlin
performGlobalAction(
    AccessibilityService.GLOBAL_ACTION_HOME
)
```

Click a discovered node:

```kotlin
node.performAction(
    AccessibilityNodeInfo.ACTION_CLICK
)
```

Enter text:

```kotlin
val args = Bundle()
args.putCharSequence(
    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
    "Hello"
)

node.performAction(
    AccessibilityNodeInfo.ACTION_SET_TEXT,
    args
)
```

---

## Device Owner

### What it is

Device Owner is Android's enterprise management mode.

It provides the highest level of control available to third-party apps without root.

### How the user enables it

The device usually must be factory-reset first.

After installation:

```bash
adb shell dpm set-device-owner \
com.example.app/.MyDeviceAdminReceiver
```

Only one Device Owner may exist.

The command generally works only on a fresh or unmanaged device.

### Application requirements

Manifest:

```xml
<receiver
    android:name=".MyDeviceAdminReceiver"
    android:permission="android.permission.BIND_DEVICE_ADMIN">

    <meta-data
        android:name="android.app.device_admin"
        android:resource="@xml/device_admin"/>

    <intent-filter>
        <action android:name="android.app.action.DEVICE_ADMIN_ENABLED"/>
    </intent-filter>

</receiver>
```

Receiver:

```kotlin
class MyDeviceAdminReceiver : DeviceAdminReceiver()
```

### What it enables

Examples:

* Kiosk mode
* Application allowlists
* Lock task mode
* Password policies
* Certificate management
* Managed configurations
* Install and manage packages
* Network configuration
* Device-wide policy enforcement

Capabilities depend on Android version.

### Kotlin examples

Check Device Owner status:

```kotlin
val dpm =
    context.getSystemService(
        DevicePolicyManager::class.java
    )

val isOwner =
    dpm.isDeviceOwnerApp(context.packageName)
```

Lock the device:

```kotlin
dpm.lockNow()
```

Set a package as permitted for Lock Task mode:

```kotlin
dpm.setLockTaskPackages(
    adminComponent,
    arrayOf("com.example.target")
)
```

Start Lock Task:

```kotlin
activity.startLockTask()
```


## Comparison

| Capability                       | WRITE_SECURE_SETTINGS | Accessibility          | Device Owner       |
| -------------------------------- | --------------------- | ---------------------- | ------------------ |
| Change system settings           | Yes                   | No                     | Some               |
| Automate UI interactions         | No                    | Yes                    | No                 |
| Read other apps' UI              | No                    | Yes                    | No                 |
| Enterprise device management     | No                    | No                     | Yes                |
| Requires user setup              | ADB                   | Accessibility settings | ADB + fresh device |
| Works on normal consumer devices | Usually               | Yes                    | Limited            |


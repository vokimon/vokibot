# Android System Event Triggers

## General Considerations

### Receiving methods

On API 26+, there are several ways to receive system events:

**Manifest** -- receiver declared in AndroidManifest.xml, works when app not running.
Limited to exempt broadcasts (see individual entries).

**Dynamic** -- runtime registration via `Context.registerReceiver()`.
Requires app process to be alive. Works for any broadcast.
Must unregister to avoid leaks.

**ForegroundService** -- requires foreground service with persistent notification.
Keeps process alive for continuous monitoring (callbacks, sensors).
Call `startForeground(notificationId, notification)` in `onCreate()`.
Manifest: `<service android:name=".MyService" android:foregroundServiceType="dataSync" />`

**SpecialService** -- Accessibility or Notification Listener service.
Requires manual user activation in system settings.

**Callback** -- system callback like `ConnectivityManager.NetworkCallback`.
Requires app process to be alive.

### Permission levels

- **normal**: auto-granted at install, no user prompt
- **runtime**: requires user approval at runtime (dangerous permissions)
- **special**: requires manual user activation in system settings
- **privileged**: requires ADB grant or system app signature


## Events Reference

## Device Power and Battery



### `BOOT_COMPLETED`

- **Triggered:** once after the device finishes booting, before the user unlocks the screen.
- **Availability:** Full
- **Permissions:** `RECEIVE_BOOT_COMPLETED` (Normal)
- **Use cases:** Restore persistent automations after reboot
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

  <!-- inside application -->
    <receiver android:name=".BootReceiver" android:exported="false">
      <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
      </intent-filter>
    </receiver>
```

Code:

    class BootReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                // Do your stuff
            }
        }
    }


### `LOCKED_BOOT_COMPLETED`

- **Triggered:** after boot but before the user unlocks the device for the first time. Requires Direct Boot mode enabled for the app.
- **Availability:** Full
- **Permissions:** `RECEIVE_BOOT_COMPLETED` (Normal)
- **Use cases:** Pre-unlock automations
- **Exploitability:** Partially exploitable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

  <!-- inside application -->
    <receiver android:name=".BootReceiver" android:exported="false"
        android:directBootAware="true">
      <intent-filter>
        <action android:name="android.intent.action.LOCKED_BOOT_COMPLETED" />
      </intent-filter>
    </receiver>
```

Code:

    class BootReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> { /* post-unlock restore */ }
                "android.intent.action.LOCKED_BOOT_COMPLETED" -> { /* pre-unlock restore */ }
            }
        }
    }


### `POWER_CONNECTED`

- **Triggered:** external power (USB, AC, wireless) is connected to the device.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Charging start/stop automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".PowerReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.ACTION_POWER_CONNECTED" />
    </intent-filter>
  </receiver>
```

Code:

    class PowerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    // device plugged in
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    // device unplugged
                }
            }
        }
    }


### `POWER_DISCONNECTED`

- **Triggered:** external power is removed from the device.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Charging stop automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".PowerReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.ACTION_POWER_DISCONNECTED" />
    </intent-filter>
  </receiver>
```

Code:

    class PowerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    // device plugged in
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    // device unplugged
                }
            }
        }
    }



### `BATTERY_CHANGED`

- **Triggered:** battery level, status, or temperature changes. Sticky broadcast.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Battery level triggers
- **Exploitability:** Partially exploitable

Code:

    val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = level * 100 / scale.toFloat()
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL
        }
    }
    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    context.registerReceiver(batteryReceiver, filter)



### `BATTERY_LOW`

- **Triggered:** battery level drops below a system-defined threshold (typically around 15%).
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Low battery warning automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".BatteryReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.BATTERY_LOW" />
    </intent-filter>
  </receiver>
```

Code:

    class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_LOW -> { /* battery low */ }
                Intent.ACTION_BATTERY_OKAY -> { /* battery recovered */ }
            }
        }
    }



### `BATTERY_OKAY`

- **Triggered:** battery level recovers above the threshold after a BATTERY_LOW event.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Battery recovery automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".BatteryReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.BATTERY_OKAY" />
    </intent-filter>
  </receiver>
```

Code:

    class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_LOW -> { /* battery low */ }
                Intent.ACTION_BATTERY_OKAY -> { /* battery recovered */ }
            }
        }
    }



### `ACTION_SHUTDOWN`

- **Triggered:** device is shutting down (user initiated power off or low battery shutdown).
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Pre-shutdown cleanup
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".ShutdownReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.ACTION_SHUTDOWN" />
    </intent-filter>
  </receiver>
```

Code:

    class ShutdownReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SHUTDOWN) {
                // save state before shutdown
            }
        }
    }



## System Configuration



### `AIRPLANE_MODE_CHANGED`

- **Triggered:** airplane mode is enabled or disabled. Includes boolean extra "state".
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Airplane mode automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".AirplaneReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.AIRPLANE_MODE" />
    </intent-filter>
  </receiver>
```

Code:

    class AirplaneReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val enabled = intent.getBooleanExtra("state", false)
        }
    }



### `TIME_CHANGED`

- **Triggered:** the user or system changes the device time.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Time change reactions
- **Exploitability:** Partially exploitable

Code:

    val filter = IntentFilter(Intent.ACTION_TIME_CHANGED)
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // time was changed
        }
    }, filter)



### `TIMEZONE_CHANGED`

- **Triggered:** the device timezone changes (user setting or network update).
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Timezone-aware automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".TimezoneReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.TIMEZONE_CHANGED" />
    </intent-filter>
  </receiver>
```

Code:

    class TimezoneReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val timezone = intent.getStringExtra("time-zone")
            // update scheduled automations for new timezone
        }
    }



### `LOCALE_CHANGED`

- **Triggered:** the device locale (language) changes.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Multilingual automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".LocaleReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.LOCALE_CHANGED" />
    </intent-filter>
  </receiver>
```

Code:

    class LocaleReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // locale changed, update UI strings if needed
        }
    }



### `CONFIGURATION_CHANGED`

- **Triggered:** any device configuration changes (orientation, font scale, dark mode, etc.).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Configuration-aware automations
- **Exploitability:** Partially exploitable

Code:

    val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // configuration changed, check specific changes
        }
    }, filter)



## Screen and User Presence



### `SCREEN_ON`

- **Triggered:** the screen turns on (user presses power button, incoming call, notification wake, etc.).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Screen state automations
- **Exploitability:** Partially exploitable

Code:

    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_ON)
        addAction(Intent.ACTION_SCREEN_OFF)
        addAction(Intent.ACTION_USER_PRESENT)
    }
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> { /* screen turned on */ }
                Intent.ACTION_SCREEN_OFF -> { /* screen turned off */ }
                Intent.ACTION_USER_PRESENT -> { /* user unlocked device */ }
            }
        }
    }, filter)



### `SCREEN_OFF`

- **Triggered:** the screen turns off (user presses power button, screen timeout, etc.).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Sleep/lock automations
- **Exploitability:** Partially exploitable

Code:

    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_ON)
        addAction(Intent.ACTION_SCREEN_OFF)
        addAction(Intent.ACTION_USER_PRESENT)
    }
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> { /* screen turned on */ }
                Intent.ACTION_SCREEN_OFF -> { /* screen turned off */ }
                Intent.ACTION_USER_PRESENT -> { /* user unlocked device */ }
            }
        }
    }, filter)



### `USER_PRESENT`

- **Triggered:** the user unlocks the device after it was locked.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Post-unlock automations
- **Exploitability:** Partially exploitable

Code:

    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_ON)
        addAction(Intent.ACTION_SCREEN_OFF)
        addAction(Intent.ACTION_USER_PRESENT)
    }
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> { /* screen turned on */ }
                Intent.ACTION_SCREEN_OFF -> { /* screen turned off */ }
                Intent.ACTION_USER_PRESENT -> { /* user unlocked device */ }
            }
        }
    }, filter)



### `TIME_TICK`

- **Triggered:** the system time advances by one minute. Sent every 60 seconds.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** None (too frequent)
- **Exploitability:** Not exploitable

Code:

    val filter = IntentFilter(Intent.ACTION_TIME_TICK)
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // minute tick -- avoid heavy work here
        }
    }, filter)



## Package Lifecycle



### `PACKAGE_ADDED`

- **Triggered:** a new application package has been installed on the device.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** `QUERY_ALL_PACKAGES` (Normal, API 30+)
- **Use cases:** App install/update automations
- **Exploitability:** Partially exploitable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

Code:

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package installed
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package uninstalled
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package updated
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addAction(Intent.ACTION_PACKAGE_REPLACED)
        addDataScheme("package")
    }
    context.registerReceiver(receiver, filter)



### `PACKAGE_REMOVED`

- **Triggered:** an existing application package has been removed from the device.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** App uninstall cleanup
- **Exploitability:** Partially exploitable

Code:

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package installed
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package uninstalled
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package updated
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addAction(Intent.ACTION_PACKAGE_REPLACED)
        addDataScheme("package")
    }
    context.registerReceiver(receiver, filter)



### `PACKAGE_REPLACED`

- **Triggered:** a new version of an application package has been installed.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** App update automations
- **Exploitability:** Partially exploitable

Code:

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package installed
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package uninstalled
                }
                Intent.ACTION_PACKAGE_REPLACED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    // package updated
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addAction(Intent.ACTION_PACKAGE_REPLACED)
        addDataScheme("package")
    }
    context.registerReceiver(receiver, filter)



### `PACKAGE_DATA_CLEARED`

- **Triggered:** the user explicitly clears an app's data from Settings.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Data source monitoring
- **Exploitability:** Partially exploitable

Code:

    val filter = IntentFilter(Intent.ACTION_PACKAGE_DATA_CLEARED)
    filter.addDataScheme("package")
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.data?.schemeSpecificPart
            // app data cleared
        }
    }, filter)



### `MY_PACKAGE_REPLACED`

- **Triggered:** the app itself has been updated to a new version.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** App update migration
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".UpdateReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
    </intent-filter>
  </receiver>
```

Code:

    class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                // app was updated, migrate data if needed
            }
        }
    }



## Media and Storage



### `MEDIA_MOUNTED`

- **Triggered:** external storage (SD card, USB storage) has been mounted.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** SD card automations
- **Exploitability:** Partially exploitable

Code:

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_MEDIA_MOUNTED -> {
                    val path = intent.data?.path
                    // storage mounted
                }
                Intent.ACTION_MEDIA_UNMOUNTED -> {
                    val path = intent.data?.path
                    // storage unmounted
                }
                Intent.ACTION_MEDIA_REMOVED -> {
                    val path = intent.data?.path
                    // storage removed
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_MEDIA_MOUNTED)
        addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        addAction(Intent.ACTION_MEDIA_REMOVED)
        addDataScheme("file")
    }
    context.registerReceiver(receiver, filter)



### `MEDIA_UNMOUNTED`

- **Triggered:** external storage has been unmounted and is no longer available.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Storage removal cleanup
- **Exploitability:** Partially exploitable

Code:

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_MEDIA_MOUNTED -> {
                    val path = intent.data?.path
                    // storage mounted
                }
                Intent.ACTION_MEDIA_UNMOUNTED -> {
                    val path = intent.data?.path
                    // storage unmounted
                }
                Intent.ACTION_MEDIA_REMOVED -> {
                    val path = intent.data?.path
                    // storage removed
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_MEDIA_MOUNTED)
        addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        addAction(Intent.ACTION_MEDIA_REMOVED)
        addDataScheme("file")
    }
    context.registerReceiver(receiver, filter)



### `MEDIA_REMOVED`

- **Triggered:** external storage has been physically removed (SD card ejected).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** SD card ejection detection
- **Exploitability:** Partially exploitable

Code:

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_MEDIA_MOUNTED -> {
                    val path = intent.data?.path
                    // storage mounted
                }
                Intent.ACTION_MEDIA_UNMOUNTED -> {
                    val path = intent.data?.path
                    // storage unmounted
                }
                Intent.ACTION_MEDIA_REMOVED -> {
                    val path = intent.data?.path
                    // storage removed
                }
            }
        }
    }
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_MEDIA_MOUNTED)
        addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        addAction(Intent.ACTION_MEDIA_REMOVED)
        addDataScheme("file")
    }
    context.registerReceiver(receiver, filter)



### `DEVICE_STORAGE_LOW`

- **Triggered:** the device storage is running low on space.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Storage cleanup automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".StorageReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.DEVICE_STORAGE_LOW" />
    </intent-filter>
  </receiver>
```

Code:

    class StorageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_DEVICE_STORAGE_LOW -> { /* storage low */ }
                Intent.ACTION_DEVICE_STORAGE_OK -> { /* storage recovered */ }
            }
        }
    }



### `DEVICE_STORAGE_OK`

- **Triggered:** device storage recovers after a DEVICE_STORAGE_LOW event.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** Storage recovery automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".StorageReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.DEVICE_STORAGE_OK" />
    </intent-filter>
  </receiver>
```

Code:

    class StorageReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_DEVICE_STORAGE_LOW -> { /* storage low */ }
                Intent.ACTION_DEVICE_STORAGE_OK -> { /* storage recovered */ }
            }
        }
    }



## Connectivity



### `CONNECTIVITY_CHANGE` (legacy)

- **Triggered:** network connectivity changes (WiFi/mobile data connects or disconnects).
- **Availability:** Deprecated API 28+
- **Method:** Callback
- **Permissions:** `ACCESS_NETWORK_STATE` (Normal)
- **Use cases:** Network-dependent automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Code:

    val cm = context.getSystemService(ConnectivityManager::class.java)
    cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // network available
        }
        override fun onLost(network: Network) {
            // network lost
        }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            val hasWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasCell = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }
    })



### `WIFI_STATE_CHANGED`

- **Triggered:** WiFi adapter state changes (enabled, disabled, enabling, disabling).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** `ACCESS_WIFI_STATE` (Normal)
- **Use cases:** WiFi state automations
- **Exploitability:** Partially exploitable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

Code:

    val filter = IntentFilter("android.net.wifi.WIFI_STATE_CHANGED")
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
            when (state) {
                WifiManager.WIFI_STATE_ENABLED -> { /* WiFi on */ }
                WifiManager.WIFI_STATE_DISABLED -> { /* WiFi off */ }
            }
        }
    }, filter)



## Bluetooth



### `ACL_CONNECTED`

- **Triggered:** a Bluetooth device establishes an ACL link with the local device.
- **Availability:** Full
- **Permissions:** `BLUETOOTH_CONNECT` (Runtime, API 31+; Normal below)
- **Use cases:** Bluetooth device arrival
- **Exploitability:** Implemented

Manifest changes:

```xml
  <uses-permission android:name="android.permission.BLUETOOTH"
      android:maxSdkVersion="30" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

  <!-- inside application -->
    <receiver android:name=".BluetoothTriggerReceiver" android:exported="true">
      <intent-filter>
        <action android:name="android.bluetooth.device.action.ACL_CONNECTED" />
      </intent-filter>
    </receiver>
```

Code:

    class BluetoothTriggerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return

            val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            } ?: return

            val name = device.name ?: "Unknown"
            val mac = device.address
            // device connected
        }
    }



### `ACL_DISCONNECTED`

- **Triggered:** a Bluetooth device's ACL link drops (device goes out of range, turned off, or disconnected).
- **Availability:** Full
- **Permissions:** `BLUETOOTH_CONNECT` (Runtime, API 31+; Normal below)
- **Use cases:** Bluetooth device departure
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.BLUETOOTH"
      android:maxSdkVersion="30" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

  <!-- inside application -->
    <receiver android:name=".BluetoothTriggerReceiver" android:exported="true">
      <intent-filter>
        <action android:name="android.bluetooth.device.action.ACL_DISCONNECTED" />
      </intent-filter>
    </receiver>
```

Code:

    class BluetoothTriggerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_ACL_DISCONNECTED) return
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            // device disconnected
        }
    }



### `ACTION_STATE_CHANGED`

- **Triggered:** the local Bluetooth adapter state changes (on/off).
- **Availability:** Full
- **Permissions:** `BLUETOOTH_CONNECT` (Runtime, API 31+; Normal below)
- **Use cases:** Bluetooth on/off automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.BLUETOOTH"
      android:maxSdkVersion="30" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

  <!-- inside application -->
    <receiver android:name=".BluetoothStateReceiver" android:exported="false">
      <intent-filter>
        <action android:name="android.bluetooth.adapter.action.STATE_CHANGED" />
      </intent-filter>
    </receiver>
```

Code:

    class BluetoothStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                when (state) {
                    BluetoothAdapter.STATE_ON -> { /* Bluetooth enabled */ }
                    BluetoothAdapter.STATE_OFF -> { /* Bluetooth disabled */ }
                }
            }
        }
    }



### `ACTION_CONNECTION_STATE_CHANGED` (BluetoothA2dp, BluetoothHeadset)

- **Triggered:** Bluetooth profile connection state changes (device connects or disconnects from a specific profile like A2DP or HFP).
- **Availability:** Full
- **Permissions:** `BLUETOOTH_CONNECT` (Runtime, API 31+; Normal below)
- **Use cases:** Audio-specific automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.BLUETOOTH"
      android:maxSdkVersion="30" />
  <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

  <!-- inside application -->
    <receiver android:name=".BluetoothProfileReceiver" android:exported="false">
      <intent-filter>
        <action android:name="android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED" />
        <action android:name="android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED" />
      </intent-filter>
    </receiver>
```

Code:

    class BluetoothProfileReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
            val previousState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> { /* profile connected */ }
                BluetoothProfile.STATE_DISCONNECTED -> { /* profile disconnected */ }
            }
        }
    }



## Telephony



### `PHONE_STATE_CHANGED`

- **Triggered:** device phone state changes (idle, ringing, offhook).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** `READ_PHONE_STATE` (Runtime)
- **Use cases:** Call-triggered automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

Code:

    val filter = IntentFilter("android.intent.action.PHONE_STATE")
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> { /* incoming call */ }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> { /* call answered */ }
                TelephonyManager.EXTRA_STATE_IDLE -> { /* call ended */ }
            }
        }
    }, filter)



### `SIM_STATE_CHANGED`

- **Triggered:** SIM card state changes (absent, locked, ready, etc.).
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** `READ_PHONE_STATE` (Runtime)
- **Use cases:** SIM change triggers
- **Exploitability:** Implementable

Code:

    val filter = IntentFilter("android.intent.action.SIM_STATE_CHANGED")
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getStringExtra("ss")
            when (state) {
                "READY" -> { /* SIM ready */ }
                "ABSENT" -> { /* no SIM */ }
                "PIN_REQUIRED" -> { /* SIM locked */ }
            }
        }
    }, filter)



### `SMS_RECEIVED`

- **Triggered:** an SMS message is received on the device.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** `RECEIVE_SMS` (Runtime), `READ_SMS` (Runtime, for content)
- **Use cases:** SMS-triggered automations
- **Exploitability:** Implementable

Code:

    val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
    context.registerReceiver(object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (msg in messages) {
                val sender = msg.displayOriginatingAddress
                val body = msg.displayMessageBody
                // process SMS
            }
        }
    }, filter)



## USB



### `USB_DEVICE_ATTACHED`

- **Triggered:** a USB device is physically connected to the device.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** USB device automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".UsbReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
      <action android:name="android.hardware.usb.action.USB_DEVICE_DETACHED" />
    </intent-filter>
  </receiver>
```

Code:

    class UsbReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "android.hardware.usb.action.USB_DEVICE_ATTACHED" -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    // USB device attached
                }
                "android.hardware.usb.action.USB_DEVICE_DETACHED" -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    // USB device detached
                }
            }
        }
    }



### `USB_DEVICE_DETACHED`

- **Triggered:** a USB device is physically disconnected from the device.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** USB device disconnect automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".UsbReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
      <action android:name="android.hardware.usb.action.USB_DEVICE_DETACHED" />
    </intent-filter>
  </receiver>
```

Code:

    class UsbReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "android.hardware.usb.action.USB_DEVICE_ATTACHED" -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    // USB device attached
                }
                "android.hardware.usb.action.USB_DEVICE_DETACHED" -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    // USB device detached
                }
            }
        }
    }



### `USB_ACCESSORY_ATTACHED`

- **Triggered:** a USB accessory (ADB device, audio adapter, etc.) is connected.
- **Availability:** Full
- **Permissions:** None
- **Use cases:** USB accessory automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".UsbReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" />
      <action android:name="android.hardware.usb.action.USB_ACCESSORY_DETACHED" />
    </intent-filter>
  </receiver>
```

Code:

    class UsbReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "android.hardware.usb.action.USB_ACCESSORY_ATTACHED" -> { /* accessory attached */ }
                "android.hardware.usb.action.USB_ACCESSORY_DETACHED" -> { /* accessory detached */ }
            }
        }
    }



## NFC



### `TAG_DISCOVERED`

- **Triggered:** an NFC tag is discovered that is not recognized by any other component.
- **Availability:** Full
- **Permissions:** `NFC` (Normal)
- **Use cases:** NFC tag detection
- **Exploitability:** Implemented

Manifest changes:

```xml
  <uses-permission android:name="android.permission.NFC" />

  <!-- inside application -->
    <activity android:name=".nfc.NfcDispatchActivity"
        android:launchMode="singleTop"
        android:exported="true">
      <intent-filter>
        <action android:name="android.nfc.action.TAG_DISCOVERED" />
        <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
    </activity>
```

Code:

    class NfcDispatchActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as? Tag
            val uid = tag?.id?.joinToString(":") { "%02X".format(it) }
            // process NFC tag
            finish()
        }
    }



### `NDEF_DISCOVERED`

- **Triggered:** an NFC tag containing NDEF data is discovered. More specific than TAG_DISCOVERED.
- **Availability:** Full
- **Permissions:** `NFC` (Normal)
- **Use cases:** NDEF content-triggered automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <uses-permission android:name="android.permission.NFC" />

  <!-- inside application -->
    <activity android:name=".nfc.NdefDispatchActivity"
        android:launchMode="singleTop"
        android:exported="true">
      <intent-filter>
        <action android:name="android.nfc.action.NDEF_DISCOVERED" />
        <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
    </activity>
```

Code:

    class NdefDispatchActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            rawMessages?.forEach { msg ->
                val ndefMsg = msg as NdefMessage
                for (record in ndefMsg.records) {
                    val payload = String(record.payload)
                    // process NDEF record
                }
            }
            finish()
        }
    }



## Audio



### `HEADSET_PLUG`

- **Triggered:** a wired headset (headphones, earbuds) is plugged in or unplugged.
- **Availability:** Full
- **Method:** Dynamic
- **Permissions:** None
- **Use cases:** Audio routing automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <receiver android:name=".HeadsetReceiver" android:exported="false">
    <intent-filter>
      <action android:name="android.intent.action.HEADSET_PLUG" />
    </intent-filter>
  </receiver>
```

Code:

    class HeadsetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                val name = intent.getStringExtra("name")
                val microphone = intent.getIntExtra("microphone", -1)
                when (state) {
                    1 -> { /* headset plugged in */ }
                    0 -> { /* headset unplugged */ }
                }
            }
        }
    }



## Other Events



### `Quick Settings Tile`

- **Triggered:** user taps a Quick Settings tile. Manual trigger, not a system broadcast.
- **Availability:** Full
- **Method:** SpecialService
- **Permissions:** None (user must add tile)
- **Use cases:** One-tap manual trigger
- **Exploitability:** Implementable

Manifest changes:

```xml
  <service android:name=".AutomationTileService"
      android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"
      android:exported="true">
    <intent-filter>
      <action android:name="android.service.quicksettings.action.QS_TILE" />
    </intent-filter>
  </service>
```

Code:

    class AutomationTileService : TileService() {
        override fun onClick() {
            // execute automation
            val intent = Intent(this, AutomationRunner::class.java)
            startForegroundService(intent)
        }

        override fun onStartListening() {
            super.onStartListening()
            qsTile?.state = Tile.STATE_ACTIVE
            qsTile?.label = "Run Automation"
            qsTile?.updateTile()
        }
    }



### `Notification Listener`

- **Triggered:** any notification is posted, updated, or removed by any app on the device.
- **Availability:** Full
- **Method:** SpecialService
- **Permissions:** None (user must enable in Settings)
- **Use cases:** Notification-driven automations
- **Exploitability:** Implementable

Manifest changes:

```xml
  <service android:name=".NotificationListener"
      android:label="NotificationListener"
      android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
      android:exported="true">
    <intent-filter>
      <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
  </service>
```

Code:

    class NotificationListener : NotificationListenerService() {
        override fun onNotificationPosted(sbn: StatusBarNotification) {
            val packageName = sbn.packageName
            val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE)
            val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT)
            // process notification
        }

        override fun onNotificationRemoved(sbn: StatusBarNotification) {
            // notification dismissed
        }
    }



### `Accessibility Service` (Foreground App Detection)

- **Triggered:** accessibility events occur (window changes, focus changes, etc.). Can detect foreground app changes.
- **Availability:** Full
- **Method:** SpecialService
- **Permissions:** None (user must enable in Settings)
- **Use cases:** Foreground app detection
- **Exploitability:** Implementable

Manifest changes:

```xml
  <service android:name=".AutomationAccessibilityService"
      android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
      android:exported="false">
    <intent-filter>
      <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
  </service>
```

```xml
  <!-- res/xml/accessibility_service_config.xml -->
  <accessibility-service
      android:accessibilityEventTypes="typeWindowStateChanged"
      android:accessibilityFeedbackType="feedbackGeneric"
      android:canRetrieveWindowContent="true" />
```

Code:

    class AutomationAccessibilityService : AccessibilityService() {
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString()
                val className = event.className?.toString()
                // foreground app changed
            }
        }

        override fun onInterrupt() {}
    }



## Exploitability Summary

### Implementable (reliable, manifest or foreground service)

- PHONE_STATE_CHANGED -- call state triggers (requires READ_PHONE_STATE)
- SIM_STATE_CHANGED -- SIM change triggers (requires READ_PHONE_STATE)
- SMS_RECEIVED -- SMS triggers (requires RECEIVE_SMS)
- BOOT_COMPLETED -- restore automations after reboot
- LOCKED_BOOT_COMPLETED -- pre-unlock automations (partial)
- POWER_CONNECTED -- charging start trigger
- POWER_DISCONNECTED -- charging stop trigger
- BATTERY_LOW -- low battery trigger
- BATTERY_OKAY -- battery recovery trigger
- ACTION_SHUTDOWN -- pre-shutdown cleanup
- ACL_CONNECTED -- Bluetooth device arrival (implemented)
- ACL_DISCONNECTED -- Bluetooth device departure
- ACTION_STATE_CHANGED -- Bluetooth on/off
- ACTION_CONNECTION_STATE_CHANGED -- profile connection changes
- TAG_DISCOVERED -- NFC tag detection (implemented)
- NDEF_DISCOVERED -- NDEF content detection
- USB_DEVICE_ATTACHED -- USB device connection
- USB_DEVICE_DETACHED -- USB device disconnection
- USB_ACCESSORY_ATTACHED -- USB accessory connection
- USB_ACCESSORY_DETACHED -- USB accessory disconnection
- DEVICE_STORAGE_LOW -- low storage trigger
- DEVICE_STORAGE_OK -- storage recovery trigger
- MY_PACKAGE_REPLACED -- app update migration
- Quick Settings tile -- manual trigger
- Notification Listener -- notification-driven automations (special permission)
- Accessibility Service -- foreground app detection (special permission)

### Partially exploitable (requires foreground service, less reliable)

- BATTERY_CHANGED -- battery level monitoring (sticky, dynamic only)
- CONNECTIVITY_CHANGE -- network state (deprecated, use callback)
- WIFI_STATE_CHANGED -- WiFi state (dynamic only)
- SCREEN_ON/OFF -- screen state (dynamic only)
- USER_PRESENT -- unlock event (dynamic only)
- AIRPLANE_MODE_CHANGED -- airplane mode toggle
- TIMEZONE_CHANGED -- timezone changes
- LOCALE_CHANGED -- locale changes
- MEDIA_MOUNTED/UNMOUNTED/REMOVED -- storage changes
- HEADSET_PLUG -- audio device changes (device dependent)
- PACKAGE_ADDED/REMOVED/REPLACED -- package lifecycle
- PACKAGE_DATA_CLEARED -- app data cleared

### Not exploitable (impractical or limited value)

- TIME_CHANGED -- dynamic only, users rarely change time manually
- TIME_TICK -- too frequent (every minute), battery drain
- CONFIGURATION_CHANGED -- dynamic only, limited automation value

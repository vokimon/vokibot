# Android Application Components

An Android **application**, hosts **components** (Activities, Services...)
that other applications (clients) may interact with.

This document explains the four component types from both perspectives:
how to define them in a host, and how to invoke them from a client.

A client can invoke components:

- **Explicitly** by specifying both app (`packageName`) and component (`className`)
- **Implicitly** by not specifying neither app or component and let the system to resolve the best match or the user to choose.
- **Scopedly** by specifying just the app and letting the system choose the best matching component or all.

## Activity: Open an app screen

### Purpose

An Activity provides a visible screen the user can see and interact
with. It is the most common component type — every Android app has
at least one.

A client application can ask a host to open a concrete screen
(if exported).

### Client interaction

Clients interact with an activity by starting it (making the screen active).
This is done by calling `startActivity(intent)`.
The intent objects carries all the information the Activity receives (see below).

An Activity can be launched by:

- **Implicit**: System resolves via intent filters across all apps.
  May show a chooser if multiple apps match.
- **Explicit**: Direct to component (`packageName` + `className`).
  Intent filters are ignored.
- **Scoped**: Target an app only. System picks the best Activity
  within that app.

An intent may contain:

- **Action**: A string identifying the general action to perform
  (e.g. `android.intent.action.VIEW`).
  May be null for pure explicit intents.
  Actions are often fully qualified names (to avoid collisions).
- **Data URI**: A URI referencing the data to operate on
  (e.g. `geo:0,0?q=Madrid`). Use depends on the action.
- **MIME type**: The media type of the data.
- **Extras**: Key-value pairs of typed data (String, Int, Boolean,
  Uri, etc.). Extras are the main way to pass parameters.
- **Flags**: Modifiers like `NEW_TASK`, `CLEAR_TOP`,
  `SINGLE_TOP` that affect how the Activity is launched.



### Host implementation

In the manifest

```xml
<activity
    android:name=".MyActivity"
    android:exported="true" />
```

Set `exported="true"` for components that should be reachable from client applications.
Add `<intent-filter>` elements if the component should respond to implicit intents.

A component can declare several `<intent-filter>` elements;
an implicit intent matches if it satisfies **any** of them.
Within one filter, the intent must satisfy **all** requirement categories
that appear (action, category, data),
but multiple tags within a category (e.g. two `<action>` elements) are alternatives (ORed).

Then the implementation:

```kotlin
class MyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // inspect intent via intent.action, intent.extras, etc.
        setContent { /* UI that displays the intent details */ }
    }
}
```

---

## BroadcastReceiver: Notify an app

### Purpose

A BroadcastReceiver listens for event messages sent across the
system or within an app. Unlike Activities, it has no user
interface — it runs briefly in `onReceive()` and returns.

A client application can send a signal to a host and optionally
pass data with it.

### Client interaction

The client entry point is `sendBroadcast(intent)`.
The intent is delivered to all matching receivers.

A broadcast intent can specify:

- **Action**: A string identifying the event.
- **Data URI**: A URI associated with the event.
- **Extras**: Key-value pairs of typed data, same as Activity.
- **Permission**: A permission string that receivers must hold.

Implicit broadcasts are **restricted** on Android 14+.
Use scoped or explicit targeting.

### Host implementation

```xml
<receiver
    android:name=".MyReceiver"
    android:exported="true" />
```

Like activities, `intent-filters` declare which broadcast actions
the receiver handles.

Then implement it like:

```kotlin
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // inspect intent.action, intent.extras, etc.
        // can start an Activity to show results:
        val display = Intent(context, ResultActivity::class.java).apply {
            putExtra("received_intent", intent)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(display)
    }
}
```

Since the receiver has no UI, the typical testing pattern is to
start an Activity that displays the received intent details,
labeled as a broadcast response.

---

## Service: Control a background task

### Purpose

A Service performs background work without user interface.
It continues active even if the user switches to another app.
It has a lifecycle like an Activity but no ui attached.

A client application can request a host to start a background
task and optionally pass data to it.

There are three modes:

- **Background service**:
    `startService(intent)`
    Launch and forget unnoticed by the user.
    Not commonly used since API 26.

- **Foreground service**:
    `startForegroundService(intent): ComponentName?`
    Launch and forget displaying a notification while running.
    This is the modern way introduced in API 26 to replace background service.
    They declare their purpose by type:
    `dataSync`, `location`, `mediaPlayback`, `connectedDevice`, `camera`, `microphone`, `health`...
    <https://developer.android.com/develop/background-work/services/fgs/service-types>

- **Bound service**:
    `bindService()`
    Supports ongoing interaction or IPC between the client and the host.


### Client interaction

On modern Android (API 26+),
for batery life concern,
services can only be started from a foreground app,
that is having a foreground service,
or having an active activity,
with a grace period of few seconds after it becomes inactive,

There are tree kinds of service, started in different ways:

**Background service:** (`startService(intent)`

- Runs without the user noticing it.
- Because its nature by security it cannot be run from 

- This is the modern way. Introduced in API 26.
- Requires the host to define the foregroundServiceType as one of
    `dataSync`, `location`, `mediaPlayback`, `connectedDevice`,
    `camera`, `microphone`...
    <https://developer.android.com/develop/background-work/services/fgs/service-types>
- Requires the host having permisions `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_*` where `*` is the type like `DATA_SYNC`.
- Since API 31 the caller must be visible  when calling it or at least having been visible recently.
- Requires setting a notification channel.

**`startService(intent)`** 

- The deprecated, seldomly used way.
- No notification required. No type definition.
- As API 26 it must be called stritly from a foreground client.

Services use explicit targeting only (both app and component).
A service intent can carry:

- **Action**: A string identifying the work to perform.
- **Extras**: Key-value pairs with parameters for the work.

Services can also be bound via `bindService()`,
for a continuous comunication between client and host.


### Host implementation

**Regular service (no notification):**

```xml
<service
    android:name=".MyService"
    android:exported="true" />
```

```kotlin
class MyService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // inspect intent.action, intent.extras, etc.
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
```

**Foreground service (persistent notification):**

```xml
<service
    android:name=".MyService"
    android:exported="true"
    android:foregroundServiceType="dataSync" />
```

Requires `android.permission.FOREGROUND_SERVICE` plus a
type-specific sub-permission (e.g.
`android.permission.FOREGROUND_SERVICE_DATA_SYNC`).

```kotlin
class MyService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service title")
            .setSmallIcon(android.R.drawable.ic_menu_info)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

---

## ContentProvider: Access data

### Purpose

A ContentProvider manages structured data (similar to a database
table) and exposes it to other applications via a content URI.
Unlike the other three component types, it is **not triggered by
an Intent** — it is accessed directly through the ContentResolver.

A client application can read or write data exposed by a host.

### Client interaction

The client entry points are:

- **QUERY**: `ContentResolver.query(uri, projection, selection,
  selectionArgs, sortOrder)` returns a Cursor with result rows.
- **READ**: `ContentResolver.openInputStream(uri)` returns an
  InputStream for reading binary data.
- **WRITE**: (planned) `ContentResolver.openOutputStream(uri)`
  for writing data.

Since there is no Intent, this component type does not support
action, extras, or flags. Instead it works with:

- **Authority**: The identifier of the provider (usually the
  host package name or a subdomain).
- **Path**: A URI path segment identifying the specific data set.
- **Operation**: QUERY, READ, or WRITE.
- **MIME type**: The type of data the provider serves.

### Host implementation

In the manifest:

```xml
<provider
    android:name=".MyProvider"
    android:authorities="com.example.app"
    android:exported="true" />
```

Set `android:grantUriPermissions="true"` if temporary URI
permissions should be granted to the client.

Then the implementation:

```kotlin
class MyProvider : ContentProvider() {
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        // Return a MatrixCursor with canned data
        return MatrixCursor(arrayOf("_id", "value")).apply {
            addRow(arrayOf(1, "response"))
        }
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.example"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?,
                        selectionArgs: Array<out String>?): Int = 0
    override fun onCreate(): Boolean = true
}
```

The provider returns Cursor objects for queries or streams for
read operations. For testing, the simplest approach is returning
a `MatrixCursor` with one or two hardcoded rows so the client
can verify data was received.

---

## Appendix: Standard Broadcast Actions

This appendix lists standard broadcast actions relevant to two use cases:
sending as a **command** (a client emits, any host with a matching receiver
receives) and listening as a **trigger** (only the system emits, but any
host can receive).

Sources:
- [`Intent` standard broadcast actions](https://developer.android.com/reference/android/content/Intent#standard-broadcast-actions)
- [`broadcast_actions.txt`](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/res/res/values/actions.xml) — all framework broadcast actions
- AOSP [`AndroidManifest.xml`](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/res/AndroidManifest.xml) — protected-broadcast declarations (system-only send)
- [Implicit broadcast exceptions](https://developer.android.com/guide/components/broadcast-exceptions) — which actions work in the manifest on API 26+
- Android SDK local file: `platforms/android-<api>/data/broadcast_actions.txt`

### Broadcast commands (client sends, host receives)

A client application can send these, and any host with an exported receiver
can receive them. Custom actions (using the host's own package prefix) are
the most common pattern — standard non-protected actions are rare.

| Action | String | Permission to send | Permission for host to receive | Notes |
|---|---|---|---|---|
| Custom action | `com.example.app.ACTION_FOO` | None | None (opt. `android:permission`) | Preferred pattern |
| `ACTION_MEDIA_BUTTON` | `android.intent.action.MEDIA_BUTTON` | None | None | Media key press |
| `ACTION_PROVIDER_CHANGED` | `android.intent.action.PROVIDER_CHANGED` | None | None | Content URI updated |
| `ACTION_CLOSE_SYSTEM_DIALOGS` | `android.intent.action.CLOSE_SYSTEM_DIALOGS` | `BROADCAST_CLOSE_SYSTEM_DIALOGS` | None | Internal use; deprecated |

Most system broadcast actions are **protected** — only the system can send
them. They are listed separately below as potential triggers.

### Broadcast triggers (system sends, host receives)

The system broadcasts these actions automatically. Any host can receive them
with the appropriate registration (manifest or dynamic). They are useful as
trigger events for automation: "when this happens, run a command."

#### Device power and battery

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_BOOT_COMPLETED` | `android.intent.action.BOOT_COMPLETED` | `RECEIVE_BOOT_COMPLETED` | ✅ Exception |
| `ACTION_LOCKED_BOOT_COMPLETED` | `android.intent.action.LOCKED_BOOT_COMPLETED` | `RECEIVE_BOOT_COMPLETED` | ✅ Exception |
| `ACTION_BATTERY_LOW` | `android.intent.action.BATTERY_LOW` | None | ✅ Exception |
| `ACTION_BATTERY_OKAY` | `android.intent.action.BATTERY_OKAY` | None | ✅ Exception |
| `ACTION_POWER_CONNECTED` | `android.intent.action.ACTION_POWER_CONNECTED` | None | ✅ Exception |
| `ACTION_POWER_DISCONNECTED` | `android.intent.action.ACTION_POWER_DISCONNECTED` | None | ✅ Exception |
| `ACTION_SHUTDOWN` | `android.intent.action.ACTION_SHUTDOWN` | None | ✅ Exception |
| `ACTION_BATTERY_CHANGED` | `android.intent.action.BATTERY_CHANGED` | None | ❌ Sticky, dynamic only |

#### System configuration

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_AIRPLANE_MODE_CHANGED` | `android.intent.action.AIRPLANE_MODE` | None | ✅ Exception |
| `ACTION_TIMEZONE_CHANGED` | `android.intent.action.TIMEZONE_CHANGED` | None | ✅ Exception |
| `ACTION_LOCALE_CHANGED` | `android.intent.action.LOCALE_CHANGED` | None | ✅ Exception |
| `ACTION_TIME_CHANGED` | `android.intent.action.TIME_SET` | None | ❌ Dynamic only |
| `ACTION_DATE_CHANGED` | `android.intent.action.DATE_CHANGED` | None | ❌ Dynamic only |
| `ACTION_CONFIGURATION_CHANGED` | `android.intent.action.CONFIGURATION_CHANGED` | None | ❌ Dynamic only |
| `ACTION_DOCK_EVENT` | `android.intent.action.DOCK_EVENT` | None | ❌ Dynamic only |
| `ACTION_DREAMING_STARTED` | `android.intent.action.DREAMING_STARTED` | None | ❌ Dynamic only |
| `ACTION_DREAMING_STOPPED` | `android.intent.action.DREAMING_STOPPED` | None | ❌ Dynamic only |

#### Screen and user presence

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_SCREEN_ON` | `android.intent.action.SCREEN_ON` | None | ❌ Dynamic only |
| `ACTION_SCREEN_OFF` | `android.intent.action.SCREEN_OFF` | None | ❌ Dynamic only |
| `ACTION_USER_PRESENT` | `android.intent.action.USER_PRESENT` | None | ❌ Dynamic only |
| `ACTION_TIME_TICK` | `android.intent.action.TIME_TICK` | None | ❌ Dynamic only |

#### Package lifecycle

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_MY_PACKAGE_REPLACED` | `android.intent.action.MY_PACKAGE_REPLACED` | None | ✅ Exception |
| `ACTION_MY_PACKAGE_SUSPENDED` | `android.intent.action.MY_PACKAGE_SUSPENDED` | None | ✅ Exception |
| `ACTION_MY_PACKAGE_UNSUSPENDED` | `android.intent.action.MY_PACKAGE_UNSUSPENDED` | None | ✅ Exception |
| `ACTION_PACKAGE_ADDED` | `android.intent.action.PACKAGE_ADDED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_REMOVED` | `android.intent.action.PACKAGE_REMOVED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_REPLACED` | `android.intent.action.PACKAGE_REPLACED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_CHANGED` | `android.intent.action.PACKAGE_CHANGED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_DATA_CLEARED` | `android.intent.action.PACKAGE_DATA_CLEARED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_RESTARTED` | `android.intent.action.PACKAGE_RESTARTED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_FULLY_REMOVED` | `android.intent.action.PACKAGE_FULLY_REMOVED` | None | ❌ Dynamic only |
| `ACTION_PACKAGE_FIRST_LAUNCH` | `android.intent.action.PACKAGE_FIRST_LAUNCH` | None | ❌ Dynamic only |
| `ACTION_PACKAGES_SUSPENDED` | `android.intent.action.PACKAGES_SUSPENDED` | None | ❌ Dynamic only |
| `ACTION_PACKAGES_UNSUSPENDED` | `android.intent.action.PACKAGES_UNSUSPENDED` | None | ❌ Dynamic only |
| `ACTION_UID_REMOVED` | `android.intent.action.UID_REMOVED` | None | ❌ Dynamic only |
| `ACTION_EXTERNAL_APPLICATIONS_AVAILABLE` | `android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE` | None | ❌ Dynamic only |
| `ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE` | `android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE` | None | ❌ Dynamic only |

#### Media and storage

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_MEDIA_MOUNTED` | `android.intent.action.MEDIA_MOUNTED` | None | ❌ Dynamic only |
| `ACTION_MEDIA_UNMOUNTED` | `android.intent.action.MEDIA_UNMOUNTED` | None | ❌ Dynamic only |
| `ACTION_MEDIA_REMOVED` | `android.intent.action.MEDIA_REMOVED` | None | ❌ Dynamic only |
| `ACTION_MEDIA_EJECT` | `android.intent.action.MEDIA_EJECT` | None | ❌ Dynamic only |
| `ACTION_MEDIA_BAD_REMOVAL` | `android.intent.action.MEDIA_BAD_REMOVAL` | None | ❌ Dynamic only |
| `ACTION_MEDIA_CHECKING` | `android.intent.action.MEDIA_CHECKING` | None | ❌ Dynamic only |
| `ACTION_MEDIA_NOFS` | `android.intent.action.MEDIA_NOFS` | None | ❌ Dynamic only |
| `ACTION_MEDIA_UNMOUNTABLE` | `android.intent.action.MEDIA_UNMOUNTABLE` | None | ❌ Dynamic only |
| `ACTION_MEDIA_SCANNER_STARTED` | `android.intent.action.MEDIA_SCANNER_STARTED` | None | ❌ Dynamic only |
| `ACTION_MEDIA_SCANNER_FINISHED` | `android.intent.action.MEDIA_SCANNER_FINISHED` | None | ❌ Dynamic only |
| `ACTION_DEVICE_STORAGE_LOW` | `android.intent.action.DEVICE_STORAGE_LOW` | None | ✅ Exception |
| `ACTION_DEVICE_STORAGE_OK` | `android.intent.action.DEVICE_STORAGE_OK` | None | ✅ Exception |

#### Connectivity

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_AIRPLANE_MODE_CHANGED` | `android.intent.action.AIRPLANE_MODE` | None | ✅ Exception |
| `ACTION_WIFI_STATE_CHANGED` | `android.net.wifi.WIFI_STATE_CHANGED` | `ACCESS_WIFI_STATE` | ❌ Dynamic only |
| `ACTION_WIFI_SCAN_RESULTS` | `android.net.wifi.SCAN_RESULTS` | `ACCESS_WIFI_STATE` | ❌ Dynamic only |
| `ACTION_CONNECTIVITY_CHANGE` | `android.net.conn.CONNECTIVITY_CHANGE` | `ACCESS_NETWORK_STATE` | ❌ Dynamic only |
| `ACTION_INPUT_METHOD_CHANGED` | `android.intent.action.INPUT_METHOD_CHANGED` | None | ❌ Dynamic only |
| `ACTION_WALLPAPER_CHANGED` | `android.intent.action.WALLPAPER_CHANGED` | None | ❌ Dynamic only |

#### Telephony

| Constant | String | Permission for host | Manifest (API 26+) |
|---|---|---|---|
| `ACTION_PHONE_STATE_CHANGED` | `android.intent.action.PHONE_STATE` | `READ_PHONE_STATE` | ❌ Dynamic only |
| `ACTION_SIM_STATE_CHANGED` | `android.intent.action.SIM_STATE_CHANGED` | `READ_PHONE_STATE` | ❌ Dynamic only |
| `ACTION_SMS_RECEIVED` | `android.provider.Telephony.SMS_RECEIVED` | `RECEIVE_SMS` | ❌ Dynamic only |
| `ACTION_NEW_OUTGOING_CALL` | `android.intent.action.NEW_OUTGOING_CALL` | `PROCESS_OUTGOING_CALLS` | ✅ Exception |
| `ACTION_SERVICE_STATE` | `android.intent.action.SERVICE_STATE` | `READ_PHONE_STATE` | ❌ Dynamic only |
| `ACTION_SIG_STR` | `android.intent.action.SIG_STR` | `READ_PHONE_STATE` | ❌ Dynamic only |

> **Note:** `✅ Exception` means the action is exempt from Android 8+ implicit
> broadcast restrictions and can be declared in the manifest. `❌ Dynamic only`
> means it requires `Context.registerReceiver()` at runtime. Some `❌` actions
> can still reach manifest receivers if targeted via explicit intent
> (`intent.setPackage()` / `intent.setComponent()`).

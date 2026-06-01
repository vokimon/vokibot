# Bluetooth

## Introduction

Bluetooth is a short-range wireless protocol
for communication between devices.

Two main types:

- **Classic Bluetooth (BR/EDR)** high throughput, continuous connection.
  Used by headphones, car hands-free, speakers. Higher power consumption.
- **Bluetooth Low Energy (BLE)** — low power, small data packets.
  Used by beacons, sensors, wearables. Faster discovery.

## Core concepts

- **Adapter** (`BluetoothAdapter`):
  Abstraction for the local bluetooth hardware.
  Lets you scan, query paired devices, etc.
- **Device** (`BluetoothDevice`):
  Abstraction for remote Bluetooth devices,
  identified by its MAC address and a human-readable name.
- **MAC address**: Unique hardware identifier, format `AA:BB:CC:DD:EE:FF`.
  Stable across reboots and renames.
- **Name**: Human-readable device label set by default by the manufacturer.
  Can be changed afterwards by the user, so not an stable identifier.
- **Bonding (pairing)**: a persistent relationship between two devices.
  Bonded devices can reconnect automatically without user confirmation.
  Pairing is the term used for the user.
  Bonding is the term used in the api.
- **ACL (Asynchronous Connection-Less) link**: the low-level data link
  between two Bluetooth devices. When a bonded device comes within range
  and reconnects, an ACL link is established. "ACL connected" means the
  device is now linked at the radio level.
- **Profile:** Standardized set of rules that defines what a device does and how it communicates with peers.
- **Roles:** Each profile define two complementary roles for the communication.
- **Implemented role:** A role of a profile a device can adopt.
- **Service:** An exposed implementation of a role that is discoverable by peers
  (Not all the implemented roles are always advertised as services)
- **Advertising:** is how a device makes a role/service (services) discoverable
  so that a peer can initiate a connection using the complementary role.
  - Classic Bluetooth: SDP (Service Discovery Protocol) Holds all information
  - BLE (Bluetooth Low Energy): GAP (lightweight advertising) + GATT (provides details after connection)
- **Device Class:** A coarse classification of devices (phone, laptop, keyboard...). Does not imply protocol just a hint for users.

## Bluetooth profiles

- Each profile defines two complementary roles
- Devices may implement several profiles and one or both roles of each profile
- Of those implmented, device may advertise the ones to be initiated by the complementary role

Common profiles:

* HID: Human Interface Device

  * Examples: keyboard, mouse, drawing tablet, game controller, remote control...
  * Optimized for low latency
  * Roles: Host / Device

* HSP: Headset Profile

  * Mono audio intended for telephony
  * Supports mic + speaker, call answer, call end
  * Limited sound quality
  * Roles: Headset / Audio Gateway

* HFP: Hands-Free Profile

  * Improves HSP, mostly used today for headset calls
  * Supports voice calling, caller ID, voice dialing
  * Additional call controls and status information (e.g. battery reporting)
  * Roles: Hands-Free / Audio Gateway

* A2DP: Advanced Audio Distribution Profile

  * High-quality audio streaming
  * Stereo audio for music and media playback
  * Does not carry microphone audio
  * Roles: Source / Sink

* AVRCP: Audio/Video Remote Control Profile

  * Remote control for media playback
  * Play, pause, next track, previous track
  * Volume control
  * Can provide media metadata (song title, artist...)
  * Roles: Controller / Target

* PAN: Personal Area Networking Profile

  * Network connection over Bluetooth
  * Can be used for internet tethering or device-to-device networking
  * Roles: PANU / NAP / GN

* OPP: Object Push Profile

  * Transfer files between devices
  * Commonly used for contacts, images and documents
  * Mostly found on older devices
  * Roles: Client / Server

* PBAP: Phone Book Access Profile

  * Allows access to a phone's contacts
  * Commonly used by car infotainment systems
  * Roles: Client / Server

* MAP: Message Access Profile

  * Allows access to messages and notifications
  * Commonly used by cars for SMS display and read-aloud features
  * Roles: Message Client / Message Server

A device may support several profiles and roles.

* Typical headset: A2DP Sink (media) + AVRCP Target (media control) + HFP Hands-Free (voice calls)
* Typical speaker: A2DP Sink (media) + AVRCP Target (media control)
* Typical smart car: A2DP Sink + HFP Hands-Free + AVRCP Target + PBAP Client + MAP Client


## Android implementation

Android provides access to Bluetooth through two API packages:

- android.bluetooth for Classic Bluetooth
- android.bluetooth.le for BLE.

Both require runtime permissions on recent Android versions.

### List paired (bonded) devices

- Method: `BluetoothAdapter.getBondedDevices()`.
- Returns devices previously paired with this device.
- Instant, no scanning, no user-visible prompt.
- Permission: `BLUETOOTH_CONNECT` Required runtime grant on 31+ Auto-granted for older versions.

### Classic device scanning

Discovers nearby visible devices, even if not paired.

- `BluetoothAdapter.startDiscovery()`.
- After that receives broadcast `ACTION_FOUND` for every device.
- Battery-heavy and async.
- Background apps cannot start it on API 26+.
- Permission: `BLUETOOTH_SCAN` Required runtime grant on 31+ Auto-granted for older versions.
- On API 23-30 it requires `ACCESS_FINE_LOCATION` instead.

### BLE device scanning

- `BluetoothLeScanner.startScan()` with ScanFilter and ScanSettings.
- More power-efficient than Classic.
- Background apps cannot start it on API 26+.
- Permission: `BLUETOOTH_SCAN` Required runtime grant on 31+ Auto-granted for older versions.
- On API 23-30 it also requires `ACCESS_FINE_LOCATION`.

### Detecting connections (passive)

The system sends two relevant broadcasts:

- `ACTION_ACL_CONNECTED`: fires when a device establishes an ACL link.
  This is the key event for "device arrived" detection.
- `ACTION_ACL_DISCONNECTED`: fires when the ACL link drops.

Both broadcasts are exempt from the implicit broadcast restrictions
on API 26+
(https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions).
They work through a manifest-declared BroadcastReceiver with
exported=true. No polling and no foreground service needed.

Permission: BLUETOOTH_CONNECT (runtime on 31+).
On older versions BLUETOOTH is auto-granted.


### Toggling Bluetooth

**`BluetoothAdapter.enable()` / `disable()`** (API 5+, deprecated 31, blocked 33+):

- Permission: `BLUETOOTH` + `BLUETOOTH_ADMIN` (legacy), `BLUETOOTH_CONNECT` (31+).
- No user flow: silent programmatic toggle.
- On API 33+ both methods always return `false` for non-system apps
  (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/bluetooth/BluetoothAdapter.java).
- Reliable only on API 30 and below.

```kotlin
val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
adapter?.enable()    // returns false on API 33+
adapter?.disable()   // returns false on API 33+
```

**`ACTION_REQUEST_ENABLE`** (API 5+):

- Permission: `BLUETOOTH` (normal, auto-granted).
- User flow: system dialog requesting consent — requires a foreground `Activity`.
- No `ACTION_REQUEST_DISABLE` exists (enable only).
- Not usable for background automation (per-action dialog + Activity context).

```kotlin
// From an Activity
val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
startActivityForResult(intent, REQUEST_ENABLE_BT)
```

**Settings panel** (API 33+):

- Permission: none.
- User flow: app opens floating panel -> user toggles Bluetooth manually.
- Not automation, just a shortcut to system UI.

```kotlin
val intent = Intent(Settings.Panel.ACTION_BLUETOOTH)
startActivity(intent)
```

**Shell command `svc bluetooth`**:

- Requires ADB connection, root, or `BLUETOOTH_PRIVILEGED` (system app).
- Not available to normal non-system apps.

```bash
svc bluetooth enable
svc bluetooth disable
```

### Connecting and disconnecting devices

**Profile proxy via reflection** (A2DP, HFP/HSP, HID Host):

Most profile proxy classes (`BluetoothA2dp`, `BluetoothHeadset`, `BluetoothHidHost`)
have `connect()` and `disconnect()` methods marked `@hide`.
On the source side (phone connecting to headset or speaker), these methods
require only `BLUETOOTH_CONNECT` — not `BLUETOOTH_PRIVILEGED`.
The sink side (`BluetoothA2dpSink`) requires `BLUETOOTH_PRIVILEGED`.

- API: 11+ (A2DP, Headset).
- Permission: `BLUETOOTH_CONNECT` for source-side profiles.
- User flow: none — async, result via `BluetoothProfile.ServiceListener`.
- Caveat: reflection is fragile across Android releases; may break on future versions.

```kotlin
// Connect via A2DP profile
adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        val a2dp = proxy as BluetoothA2dp
        val connectMethod = BluetoothA2dp::class.java.getDeclaredMethod(
            "connect", BluetoothDevice::class.java
        )
        connectMethod.isAccessible = true
        connectMethod.invoke(a2dp, device)
    }

    override fun onServiceDisconnected(profile: Int) {}
}, BluetoothProfile.A2DP)
```

```kotlin
// Disconnect via A2DP profile
adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        val a2dp = proxy as BluetoothA2dp
        val disconnectMethod = BluetoothA2dp::class.java.getDeclaredMethod(
            "disconnect", BluetoothDevice::class.java
        )
        disconnectMethod.isAccessible = true
        disconnectMethod.invoke(a2dp, device)
    }

    override fun onServiceDisconnected(profile: Int) {}
}, BluetoothProfile.A2DP)
```

**`BluetoothDevice.disconnect()`** (API 37+):

- Clean public API — disconnects all active profiles for a device.
- Permission: `BLUETOOTH_CONNECT` + (`BLUETOOTH_PRIVILEGED` or Companion Device Manager association).
- User flow: none for PRIVILEGED apps; one-time CDM association otherwise.
- API 37 is not yet released as of SDK 36.

**Companion Device Manager (CDM)**:

- API: 26+ for basic association, 37+ for connect/disconnect grant.
- User flow: one-time system UI — user selects the device and confirms.
- After association: persistent permission grant, no further dialogs.

```kotlin
val deviceManager = getSystemService(CompanionDeviceManager::class.java)
val deviceFilter = BluetoothDeviceFilter.Builder()
    .setAddress(macAddress)
    .build()
val request = AssociationRequest.Builder()
    .addDeviceFilter(deviceFilter)
    .build()

deviceManager.associate(request, object : CompanionDeviceManager.Callback() {
    override fun onDeviceFound(discoveryResult: IntentSender) {
        startIntentSenderForResult(
            discoveryResult, REQUEST_CDM, null, 0, 0, 0
        )
    }

    override fun onFailure(error: CharSequence?) {}
}, null)
```

**RFCOMM socket**:

- Low-level `BluetoothDevice.createRfcommSocketToServiceRecord(uuid)` + `socket.connect()`.
- Permission: `BLUETOOTH_CONNECT`.
- Requires knowing the service UUID on the target device.
- Only applicable for custom peer-to-peer apps, not for connecting to
  consumer devices under standard profiles.

### Detecting device capabilities

`BluetoothDevice.fetchUuidsWithSdp()` queries the device's SDP record for
supported profile UUIDs. The result arrives via `ACTION_UUID` broadcast.
Cached values are available through `getUuids()`.

Profile UUIDs are defined by the Bluetooth SIG:

```
A2DP Source      0000110A-0000-1000-8000-00805F9B34FB
A2DP Sink        0000110B-0000-1000-8000-00805F9B34FB
HFP (Hands-Free) 0000111E-0000-1000-8000-00805F9B34FB
HSP (Headset)    00001108-0000-1000-8000-00805F9B34FB
HID              00001124-0000-1000-8000-00805F9B34FB
```

```kotlin
// Check cached UUIDs
device.uuids?.forEach { uuid ->
    when (uuid.toString().uppercase()) {
        "0000110B-0000-1000-8000-00805F9B34FB" -> supportsA2dp = true
        "0000111E-0000-1000-8000-00805F9B34FB" -> supportsHfp = true
    }
}

// Trigger fresh SDP fetch (result via ACTION_UUID broadcast)
device.fetchUuidsWithSdp()
```

- Permission: `BLUETOOTH_CONNECT`.
- Some devices do not advertise all profiles via SDP.
- Useful for validation: warn user when the selected profile is not advertised.


## Design decisions

### Device connection event trigger

**Passive ACL detection over active scanning**:
Use `ACTION_ACL_CONNECTED` via a manifest-declared `BroadcastReceiver`
instead of polling with `BluetoothAdapter.startDiscovery()` or `BluetoothLeScanner`.
The ACL broadcast is fire-and-forget, needs no
foreground service, and is exempt from API 26+ implicit broadcast
restrictions.
Since VokiBot only needs to react when a bonded device
reconnects (arrival), this is sufficient.

**No AmbientMonitorService yet**:
all current trigger types (NFC, Shortcut, Bluetooth) are passive,
they either receive a system
broadcast or get invoked by the launcher. A foreground service will
only be introduced if a polling-based trigger type appears (e.g.,
WiFi SSID via `NETWORK_STATE_CHANGED_ACTION`).

**Receiver handles only `ACTION_ACL_CONNECTED`**: we detect device
arrival, not departure. If departure detection is needed later, it
can be added with `ACTION_ACL_DISCONNECTED` in the same receiver.

**`BLUETOOTH_CONNECT` is the only runtime permission needed** (API
31+). Below API 31, `BLUETOOTH` is a normal permission (auto-granted
at install). `ACCESS_FINE_LOCATION` is **not** required because we
don't do BLE scanning.

### Device connection command

**Bluetooth connect/disconnect via reflection**:
source-side profile proxy methods (`BluetoothA2dp.connect()`, `BluetoothHeadset.connect()`)
require only `BLUETOOTH_CONNECT` and are accessible via reflection.
This works on API 33-36 today but is fragile across Android updates.
If implemented, the `try` button in the editor provides immediate feedback
on whether the call succeeded on the current device.

**CDM + `BluetoothDevice.disconnect()` as future path**
This would solve the BT status in a more reliable way
but Vokibot is API 26-36 this is API 37.
A future migration could be considered when API support moves.

**Bluetooth enable/disable not viable for automation**:
`enable()`/`disable()` are hard-blocked on API 33+.
`ACTION_REQUEST_ENABLE` requires a foreground Activity and a system dialog
per action — incompatible with background execution. Shell commands need
ADB or root. No practical option exists for a non-system F-Droid app.

**Profile detection via SDP useful for editorial validation**:
`fetchUuidsWithSdp()` can determine which profiles a device advertises.
In a connect-action editor this helps the user select an appropriate profile.
The ACL trigger itself does not need this information.


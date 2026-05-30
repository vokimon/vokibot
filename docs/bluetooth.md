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


## Design decisions


**Passive ACL detection over active scanning**:
we use `ACTION_ACL_CONNECTED` via a manifest-declared `BroadcastReceiver`
instead of polling with `BluetoothAdapter.startDiscovery()` or `BluetoothLeScanner`.
The ACL broadcast is fire-and-forget, needs no
foreground service, and is exempt from API 26+ implicit broadcast
restrictions. Since VokiBot only needs to react when a bonded device
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


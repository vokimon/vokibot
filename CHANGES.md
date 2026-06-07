# Change log

## 0.4.0 (2026-06-07)

New command: Connect and disconnect Bluetooth devices

- ✨ BluetoothConnectCommand: Connect or disconnect bluetooth devices
- ✨ BluetoothConnectCommand: Pick specific roles to disconnect
- ♻️ Common code extracted for reusing

## 0.3.0 (2026-06-01)

Bluetooth Device Trigger, launch commands on Bluetooth connection

- ✨ BluetoothDeviceTrigger: Added new type of trigger
- ✨ BluetoothDeviceTrigger: Choosing from a list of paired devices
- ✨ BluetoothDeviceTrigger: Ask permissions only when adding the trigger
- ✨ BluetoothDeviceTrigger: Represent device class information

## 0.2.0 (2026-05-29)

Improved application commands edition

- ✨ ApplicationCommand: Inverted flow
    Now the first you see is the intent editor, not the app list,
    providing context of what you are doing when selecting the app.
- ✨ ApplicationCommand: Enabled editing already existing one
- ✨ ApplicationCommand: Change the selected component
- 🚸 ApplicationCommand: Same editor adapts to the picked component type
- ✨ ApplicationCommand: Edit intent data (uri and/or mime)
- ✨ ApplicationCommand: Edit intent extra parameters
- 🚸 ApplicationCommand: Pick file button for Uri fields
- 🚸 ApplicationCommand: Autocompletion for Uri protocols and Mime fields
- 🌐 SettingsPage: translated page and category names

## 0.1.0 (2026-05-19)

- ✨ Automations: Relate triggers to commands
- ✨ NfcTrigger: Runs a command when approaching an NFC tag
- ✨ NfcTrigger: Autodetect NFC tags for quick setup
- ✨ ShortcutTrigger: Run a command from a home screen shortcut
- ✨ ShortcutTrigger: On creation also creates the Shortcuts in the Launcher
- ✨ ApplicationCommand: Launches an Application
- ✨ ApplicationCommand: Configure by browsing installed applications and their components
- ✨ SettingsPageCommand: Opens a system configuration page
- 🌐 Translations: an, ar, ca, de, en, es, eu, fr, gl, pt, ru, andaluh
- 💄 Material3 theming (light and dark)

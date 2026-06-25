# Change log

## Sprinted


- Change Setting Command:
    - [x] Select a setting
    - [x] Setting List: Screen skeletton returning a setting
    - [x] Setting List: Build the Category list and translations
    - [x] Setting List: Settings database
    - [ ] Setting List: Navigate settings database
    - [ ] Setting List: Group by category
    - [ ] Setting DB: Add new types
    - [ ] Different icon for each value type?
    - [ ] Setting Editor: Add new types
    - [ ] Very short description (Brighness) to be used as title, short enough to be also composed as command title "Brightness=100"
    - [ ] Long description: user centered to be shown in the editor when modifying (Sets the brightness of the screen)
    - [ ] Raw value description: to be used in raw edition, describes string format (Integer between 0 and 300)a
    - [x] Change Settings Editor: rememberDiscardableState
    - [x] Change Settings Editor: if dirty show discard dialog before leaving
    - [x] Change Settings Editor: isReadyToSave requires setting not null
    - [x] Change Settings Editor: isReadyToTry requires setting not null
    - [ ] Deprecation: Hide deprecated settings according to device android version
    - [ ] Deprecation: When editing a deprecated setting (because device upgraded) show a warning
    - [x] Typed editor for Boolean (Toggle)
    - [x] Raw editor: Text editor with help on formatting this settings.
    - [x] Permissions warning: and asker (reuse stuff from bluetooth)
    - [ ] Permissions warning: for Secure and Global settings
    - [ ] Permissions warning: Generalize for other strict permissions
        - [ ] WRITE_SETTINGS	Settings.System.canWrite()	ACTION_MANAGE_WRITE_SETTINGS
        - [ ] SYSTEM_ALERT_WINDOW	Settings.canDrawOverlays()	ACTION_MANAGE_OVERLAY_PERMISSION
        - [ ] REQUEST_IGNORE_BATTERY_OPTIMIZATIONS	PowerManager.isIgnoringBatteryOptimizations()	ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        - [ ] PACKAGE_USAGE_STATS	Consultar AppOpsManager	ACTION_USAGE_ACCESS_SETTINGS
        - [ ] MANAGE_EXTERNAL_STORAGE	Environment.isExternalStorageManager()	ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
    - [ ] Permissions warning: Move to shared
    - [ ] Editor header: Fix: long titles move the done button out the screen.

## Backlog

- [ ] Hide Dasher when option clicked
- [x] Add System.Panel to Settings Pages to choose
- [x] Use System.Panel.ACTION_NFC to enable NFC instead of Settings page
- [ ] Weblate: create meta module
- [ ] Weblate: create web module
- [ ] Import: warn newer versions
- [ ] Import: warn older versions or give option to migrate
- [ ] Import: warn on duplicated ids in the bundle
- [ ] Import: different ids may collide in file after sanitization
- [ ] Import: DataChangeBus storm on big files (not relevant by now)
- [ ] Import: Atomicity (not relevant by now)
- [ ] Import: edge case: Ids dupplicated in the bundle.
- [ ] Trigger/CommandList: Set a fix group order (it changes on every refresh)
- [ ] Export: Fix: UnknownEntity title is using command's
- [ ] ComponentLst: Fix: Shown app icon instead of component icon
- [ ] ComponentList: add help icon for each kind of component
- [ ] All buttons Save -> Done to denote save+close
- [ ] Shortcuts: Customizable icon/color
- [ ] DataProviders: How to use them as commands?
- [ ] Bound services: How to use them as commands?
- [ ] IntentActionEditor: Suggest package name as action prefix
- [ ] ExtraEditor: Edit the name of the Extra value afterwards
- [ ] ExtraEditor: Suggest package prefixes for the Extra name when empty
- [ ] ExtraEditor: Delete Extra
- [ ] ExtraEditor: Specific editor for string list
- [ ] ExtraEditor: Specific editor for uri list
- [ ] UriField: When the protocol ask for it (tel, sms, mailto), provide button to browse contacts.
- [ ] UriField: Fix: Expands multiple lines. Limit single line? Summary?
- [ ] IntentDataEditor: Paste icon button
- [ ] Magic naming: icon button to assign a default name from content in the name editor. Show when empty instead of delete button.
- [ ] ApplicationCommand: Overwrite dialog: if not confirm, back to ask the name not full cancell
- [ ] AppList: Filter sheet gets cropped on landscape
- [ ] Action to stop a service
- [ ] Extract list group header item and reuse it
- [ ] Extract list group item and reuse it
- [ ] Translate extra type names
- [ ] Translate action auto description
- [ ] Explore Tasker compatibility (being able to reuse tasker extensions as host)

- More commands:
    - [ ] Airplaine mode on/off
    - [ ] Enable/Disable/Connect Wi-Fi
    - [ ] Set screen timeout

- More triggers:
    - [ ] System Broadcast: As trigger to vokibot, system sends broadcasts to any listening app
    - [ ] On notification received
    - [ ] Date/Day of Week/Time/Timer
    - [ ] Calendar events
    - [ ] Call received
    - [ ] Location
    - [ ] SMS received
    - [ ] Batery level
    - [ ] Power status
    - [ ] NFC Enabled
    - [ ] Connectivity type
    - [ ] Headset
    - [ ] Screen status
    - [ ] Wifi enabled
    - [ ] Wifi connection
    - [ ] Tiled services: https://developer.android.com/develop/ui/views/quicksettings-tiles
    - [ ] List of system broadcasts: <https://github.com/flyskywhy/android-sdk/blob/master/platforms/android-10/data/broadcast_actions.txt>

## Done

## Done 0.6.1

- [x] BT Trigger: Detect that bt is disabled or/and button to reenable it
- [x] BT Connect: Detect that bt is disabled or/and button to reenable it
- [x] AutomationEditor: when launched from nfc detector, it has no padding
- [x] ShortcutDispatcher: if no trigger related, create it
- [x] ShortcutDispatcher: if no action related, create and edit it

## Done 0.6.0

- [x] Import/Export entities.
- [x] Import/Export text translations
- [x] Import edge case 1: Malformed bundle JSON: ExportedBundle.fromJson() throws SerializationException. -> Catch in Drawer item click handler, notify on UserMessage
- [x] Import edge case 2: Unknown type in bundle: now loaded as UnknownEntity -> report as warning
- [x] Import edge case 3: Missing dependencies: Automation references command/trigger not in bundle neither the repo. -> report as warning
- [x] Import edge case 4: Duplicate IDs: Importing same bundle twice -> silent overwrite -> This is intended -> report as warning
- [x] Import edge case 6: File picker cancelled: Returns null -- should be handled, no import triggered. -> bytes?.let
- [x] Refresh after import

## Done 0.5.0

## Done 0.4.0

- [x] Command: Enable/Disable/Connect Bluetooth 
- [x] Fix: BluetoothConnect: default icon does not get the tint

## Done 0.3.0

- [x] Detect bluetooth connection
- [x] BluetoothDeviceTrigger
- [x] BluetoothDeviceTriggerEditor
- [x] BluetoothDeviceTriggerEditor list of bound devices
- [x] BluetoothDeviceTriggerEditor ask for grants
- [x] BluetoothDeviceTriggerEditor list of paired devices
- [x] BluetoothDeviceTriggerEditor disable "Save" until fields filed
- [x] Bluetooth detector launches activitites
- [x] Bluetooth show class information as icon/description
- [x] BluetoothDeviceTriggerEditor list of detected devices -> Discarded, not useful, android provides no means to make them useful
- [x] Add bluetooth class description in trigger list and automation editor
- [x] Traduccions del bluetoothdevicetriggereditor
- [x] Combine icons for bluetooth + class icon
- [x] Extract device list as composable
- [x] Extract permissions warning as composable
- [x] app/src/main/kotlin/net/canvoki/vokibot/Bluetooth.kt:9: Error: Call requires permission which may be rejected by user: code should explicitly check to see if permission is available (with checkPermission) or explicitly handle a potential SecurityException [MissingPermission]
- [x] warning: app/src/main/kotlin/net/canvoki/vokibot/Bluetooth.kt:146:36 'static fun getDefaultAdapter(): BluetoothAdapter!' is deprecated. Deprecated in Java.

## Done 0.2.0

- [x] Use UserMessage for "Try" button errors
- [x] Services: https://developer.android.com/develop/background-work/services
- [x] Broadcasts: Send a shot and forget message https://developer.android.com/develop/background-work/background-tasks/broadcasts
    - [x] Puppet: Add broadcast to the manifest
    - [x] Puppet: Required permissions to receive
    - [x] Puppet: onReceive https://developer.android.com/reference/android/content/BroadcastReceiver
    - [x] What permissions are required to interact
    - [x] try with val canDeliver = context.packageManager.queryBroadcastReceivers(intent, 0).isNotEmpty()
- [x] fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes ExposedDropdownMenuAnchorType and enabled parameters.
- [x] ApplicationCommandEditor: Extract component selector as reusable composable
- [x] Play icon to the 'Try' button
- [x] Add EditorHeader to Trigger/CommandList "Saved Triggers/Commands"
- [x] UriField: Only show the file picker if the field is empty (or content://)
- [x] ActivityLaunchCommandEditor: Click on app/component actually open AppList
- [x] ActivityLaunchCommandEditor: Done button actually saves entity
- [x] ActivityLaunchCommandEditor: Fix: 'data' extra in command specs is not extra but data in most cases
- [x] ActivityLaunchCommandEditor: Fix: data required by command specs should be protocol restricted in some cases
- [x] ActivityLaunchCommandEditor: Edit mode
- [x] UriField: When file picked show the file name instead of the uri and make it readonly for typing
- [x] UrlField: reuse for extras
- [x] ActivityLaunchCommandEditor: Try button
- [x] IntentDataEditor: Pick file icon button
- [x] IntentDataEditor: Pick file icon button sets mime
- [x] IntentDataEditor: Autocomplete mime type
- [x] IntentEditor: Action select in a dialog with scroll -> Material version is better
- [x] IntentEditor: Action select with icons
- [x] IntentEditor: Add custom Extras (choose name and type)
- [x] IntentEditor: On change action, remove empty extras, keep filled
- [x] Extract as composables the parts of the editor that might be reused

## Done 0.1.0

- [x] TriggerList delete does not work for NFC (it does for shortcuts)
- [x] AutomationEditor: Renaming existing should rename, not duplicate
- [x] TriggerList: Chooser: add type icon
- [x] TriggerList: Chooser: remove radiobutton
- [x] Shortcut: Fix: Renaming does not rename Shortcut yet
- [x] Registry: Move serialization to the type info
- [x] Open settings page: <https://developer.android.com/reference/android/provider/Settings#ACTION_APPLICATION_DEVELOPMENT_SETTINGS>
- [x] New Trigger: Shortcut
- [x] New Trigger: Nfc Tag
- [x] Push the retrieved PublicComponent to the IntentEditor
- [x] IntentEditor: Use data from the PublicComponent
- [x] Remove the old data retrieval in IntentEditor
- [x] Use ApplicationCommand to try (only Activity, only currently edited fields)
- [x] Build a data repository to store among other things, actions by assigning and opaque id and a display name.
- [x] Implement the save button
- [x] Add new top level screen Command picker
- [x] Add button to add action -> Show a menu of kind (only application by now) -> got to current app list
- [x] List saved commands
- [x] Command Menu: "Run"
- [x] Command Menu: "Delete"
- [x] Llista d'applicacions
- [x] Llista d'activitats
- [x] Llençar activitat
- [x] Detectar accions
- [x] TriggerList: Make type separator label, type dependant (now Nfc for all types)
- [x] StackNavigator: First screen at the begining with no slide animation
- [x] StackNavigator: On push, previous screen slide out left (not fully which is not a problem if the next work)
- [x] StackNavigator: On push, previous screen fades out -> No the screen is still visible!!!
- [x] StackNavigator: On push, new screen slides left in
- [x] StackNavigator: On push, new screen slides fades in
- [x] StackNavigator: On pop, discarded screen slides right out
- [x] StackNavigator: On pop, discarded screen fades out
- [x] StackNavigator: On pop, recovered screen slides right in
- [x] StackNavigator: On pop, recovered screen fades in
- [x] StackNavigator: return value from pop to the pusher


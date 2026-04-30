# Change log

- [ ] TriggerList delete does not work for NFC (it does for shortcuts)
- [ ] AutomationEditor: Renaming existing should rename, not duplicate
- [x] TriggerList: Make type separator label, type dependant (now Nfc for all types)
- [ ] TriggerList: Chooser: add type icon
- [ ] TriggerList: Chooser: remove radiobutton

- [x] First screen at the begining with no slide animation
- [x] On push, previous screen slide out left (not fully which is not a problem if the next work)
- [x] On push, previous screen fades out -> No the screen is still visible!!!
- [x] On push, new screen slides left in
- [x] On push, new screen slides fades in
- [x] On back, discarded screen slides right out
- [x] On back, discarded screen fades out
- [x] On back, recovered screen slides right in
- [x] On back, recovered screen fades in


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
- [ ] Complete the dialog for Activities
- [ ] Extract as composables the parts of the dialog that might be reused
- [ ] Conditionally choose the Activity or no dialog depending of the kind
- [ ] Progressively, one component kind at a time, add the dialog of that kind of App component, reusing parts of Activity and composing its ApplicationCommand.
- [ ] Command Menu: "Edit"
- [ ] Use UserMessage for Try errors
- [ ] ApplicationCommand: Overwrite dialog: if not confirm, back to ask the name not full cancell
- [ ] Save dialog: smarter default naming, not just the app name
- [ ] AppList: Filter sheet gets cropped on landscape
- [ ] IntentEditor: Action select with icons
- [ ] IntentEditor: Action select in a dialog with scroll
- [ ] IntentEditor: Add custom Extras (choose name and type)
- [ ] IntentEditor: Delete Extra
- [ ] IntentEditor: On change action, remove empty extras, keep filled
- [ ] Saved Commands: List splitted by types, every type `+` to add a new command
- [ ] Application Command type: List application


- [x] Llista d'applicacions
- [x] Llista d'activitats
- [x] Llençar activitat
- [x] Detectar accions
- [ ] List actionable Services 
    - PackageInfo.services
    - pm.queryIntentServices(intent, 0)
    - background capabilites
- [ ] List actionable Receivers
    - PackageInfo.receivers
    - pm.queryBroadcastReceivers(intent, 0)
    - not that interesting, maybe we could inverse engineer custom app events we could also subscribe
- [ ] List providers
    - PackageInfo.providers
    - pm.resolveContentProvider(authority, 0)


- More commands:
    - [ ] Open settings page: <https://developer.android.com/reference/android/provider/Settings#ACTION_APPLICATION_DEVELOPMENT_SETTINGS>
- More triggers:
    - [ ] Widget
    - [ ] On broadcast received
    - [ ] On notification received
    - [ ] Date/Day of Week/Time/Timer
    - [ ] Calendar events
    - [ ] Call received
    - [ ] Location
    - [ ] SMS received
    - [ ] Batery level
    - [ ] Power status
    - [ ] Connected Bluetooth
    - [ ] Bluetooth Enabled
    - [ ] NFC Enabled
    - [ ] Connectivity type
    - [ ] Headset
    - [ ] Screen status
    - [ ] Wifi enabled
    - [ ] Wifi connection




# Change log

- [x] Push the retrieved PublicComponent to the IntentEditor
- [x] IntentEditor: Use data from the PublicComponent
- [x] Remove the old data retrieval in IntentEditor

- [x] Use ApplicationCommand to try (only Activity, only currently edited fields)
- [x] Build a data repository to store among other things, actions by assigning and opaque id and a display name.
- [x] Implement the save button
- [x] Add new top level screen Command picker
- [x] Add button to add action -> Show a menu of kind (only application by now) -> got to current app list
- [ ] List saved commands
- [ ] Command Menu: "Run"
- [ ] Command Menu: "Delete"
- [ ] Complete the dialog for Activities
- [ ] Extract as composables the parts of the dialog that might be reused
- [ ] Conditionally choose the Activity or no dialog depending of the kind
- [ ] Progressively, one component kind at a time, add the dialog of that kind of App component, reusing parts of Activity and composing its ApplicationCommand.
- [ ] Command Menu: "Edit"
- [ ] Use UserMessage for Try errors
- [ ] Overwrite dialog: if not confirm, back to ask the name not full cancell
- [ ] Save dialog: smarter default naming, not just the app name
- [ ] AppList: Filter sheet gets cropped on landscape
- [ ] IntentEditor: Action select with icons
- [ ] IntentEditor: Action select in a dialog with scroll
- [ ] IntentEditor: Add custom Extras (choose name and type)
- [ ] IntentEditor: Delete Extra
- [ ] IntentEditor: On change action, remove empty extras, keep filled



- [ ] Main view
    - [ ] List of automations (On Even, Command)
    - [ ] Button to add a new one
    - [ ] Automation item menu to edit
    - [ ] Automation item menu to delete
- [ ] Package component view
    - [ ] Components: Translated plural section names (Activity -> Activitats)
- [ ] Automation edit
    - [ ] Event section
        - [ ] Event Item
    - [ ] Command section
        - [ ] Command Item
    
    
    - [ ] Automations and `+` button to add.
    - [ ] Saved Commands: List splitted by types, every type `+` to add a new command
    - [ ] Application Command type: List application
    - [ ] On 


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






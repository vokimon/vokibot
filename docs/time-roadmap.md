# TimeTrigger roadmap

## Managing this document

Maintenance:

- Stages and steps are only a guideline deduced from previous deliberations.
- They can be modified to better fit the situation as we move on.
- But whenever we change the plan, we should update this document.
- As steps land, we should update checkboxes
- Comment after a `->` resolutions, even when the step is discarded.

## Resolved design decisions

- Stored UUID id.
  Content-derived ids would orphan automations on edit.
- `startAt` is valid-from for all recurrence kinds.
  The device zone applies at fire time.
- DST: gap resolves with the java.time shift,
  overlap resolves to the earlier pass.
  `nextOccurrence` is computed after the occurrence just fired
  to avoid double fires.
- `scheduledFor` lives in the entity as epoch millis.
  It is owned by the scheduler.
- `onMissed` is Skip or CatchUp per trigger.
  CatchUp depends on `scheduledFor`.
- Inexact AlarmManager first, exact toggle later.
- Dispatch is headless, errors are logged first.

## Plan overview

Vertical thin slices over finished layers: the riskiest part
(Android scheduling) is touched in phase A, not last.
`nextOccurrence` starts one-shot and extends with recurrence.
-> stages reordered and merged: old Stage 1..6 are discarded.
-> phases follow the functional objectives, in priority order.
Each phase is complete enough for the user to use it:
it carries its own attributes, methods, editor field and dispatch.

Functional objectives, in priority order:

1. Program an hour and execute it, once.
2. Once executed, if periodic, schedule the next occurrence.
3. On device boot, reschedule the triggers.
4. On device boot, execute the triggers that were programmed.

## Phase A -- objective 1: fire once at the programmed time

- [x] `LocalDateTimeIsoSerializer`.
    - [x] serialization
    - [x] deserialization
      Three RED/GREEN pairs: parse, malformed input, ISO encode.
- [x] Serialization interface
    - [x] toJson empty
    - [x] fromJson invalid json
    - [x] fromJson empty
- [x] `id`
    - [x] default value uuid
    - [x] fromJson loads id
    - [x] toJson stores id
- [x] `startAt`
    - [x] fixture gains startAt
    - [x] startAt with `LocalDateTimeIsoSerializer`
- [x] `displayName`
    - [x] fixture gains displayName
- [x] infrastructure
    - [x] fixture gains type
    - [x] `description`
          GREEN literal; refactor format(); refactor const DESCRIPTION_FORMAT.
    - [x] `iconRes`  -> ic_schedule
    - [x] Robolectric runner on `TimeTriggerTest` (green refactor)
    - [x] `getTitle` returns displayName
    - [x] Inherit `Trigger()`: replace `: StorableEntity` with `: Trigger()`.
          Overrides already in place; must stay green.
          (Cross-reference: sub-item of companion `EntityMetadata` above.)
    - [x] companion `EntityMetadata`, 2 keys via `proposal.yaml`
          [x] member `typeKey`, `iconRes`, `entityClass`, `labelRes`, `helpRes`, `deserializer`
          [x] member `editorFactory` (placeholder `NotYetImplementedEditor`)
          [x] inherit `: EntityMetadata` + `override`  (class `: StorableEntity` as prerequisite)
          [x] class inherits `Trigger()` (replaces `StorableEntity`)
    - [x] `register()` in `EntityBootstrap`
- [x] Editor:
    - [x] TDD for `editorFactory`
          - [x] `editor returns TimeTriggerEditor, without id`
          - [x] `editor returns TimeTriggerEditor, with id`
    - [x] Shell copied from `ShortcutTriggerEditor` / `NfcTriggerEditor`:
          - [x] Show the `triggerId` parameter in the Screen (hello world;
                verifies navigation reaches the screen).
          - [x] `EditorHeader` + `rememberDiscardableState` + save via
                `repository.trigger.save(...)` with default values
                (`startAt = now`, `displayName` empty).
                English literals for header title/save, migrate to
                translations at the end of the release.
                Editing case `-> resolved`: `displayName` and `startAt`
                live in editor state (`rememberSaveable`); the save
                builds the trigger from scratch from that state.
    - [x] `displayName` field (pattern already in `ShortcutTriggerEditor`).
    - [x] Decide the date/time input.
          `-> resolved`: two clickable rows showing the value (Time
          first, Date second), edited with Material3 TimePicker /
          DatePicker dialogs; the date field will evolve when
          recurrence lands (phase B).
    - [x] Date and Time fields in the editor (render + pickers).
- [x] Dispatch
    - [x] `TimeScheduler` (AlarmManager abstraction).
    - [x] `TimeTriggerReceiver` + manifest + dispatch via
          `Automation.executeByTrigger`.
    - [x] Schedule on save (new and edit).
    - [x] `TimeTrigger.schedule` syncs the alarm: schedule if future,
          cancel if past (via `nextOccurrence`).
    - [x] `StorableEntity.onRemoved`: `TimeTrigger` cancels its alarm.
    - [x] Problem: alarms in  past trigger as soon as they are programmed
        -> nextOccurrence returning null on past startAt
    - [x] Problem: imported time triggers are never scheduled (import
          bypasses the editor).
        -> resolved: TimeTrigger.scheduleAll after importBundle in both
           branches
    - [x] Problem: alarms do not survive reboot
        -> resolved: `AlarmRestoreReceiver` on BOOT_COMPLETED
    - [x] Problem: alarms do not survive app updates
        -> resolved: same receiver on MY_PACKAGE_REPLACED
    - [x] Problem: alarm fires but launching activities is blocked
          from background (BAL)
        -> SYSTEM_ALERT_WINDOW banner at TimeTriggerEditor
    - [x] `TimeTrigger.scheduleAll(context)`: sync the alarm of every
          TimeTrigger.
    - [x] `AlarmRestoreReceiver` + manifest (BOOT_COMPLETED,
          MY_PACKAGE_REPLACED, RECEIVE_BOOT_COMPLETED, exported=false).
    - [x] Reschedule on timezone/clock change.
        -> resolved: TIME_SET and TIMEZONE_CHANGED actions added to
           AlarmRestoreReceiver

## Phase B -- Recurrent triggers

- [ ] `recurrence` and `summary` attributes.
- [ ] `nextOccurrence` recurrence (TDD: 1, N cases).
- [ ] Editor gains the `recurrence` field.
- [ ] Receiver reschedules the next occurrence after fire.

## Phase C -- Missed triggers

- [ ] `onMissed` attribute (Skip / CatchUp).
- [ ] Editor gains the `onMissed` field.
- [ ] CatchUp on boot: run the triggers whose `scheduledFor` passed.
      Fold-back: next after `scheduledFor`, not after now.

## Later

- [ ] ShortcutTrigger: implement `onRemoved` to unpin the launcher
      shortcut (today the pinned shortcut survives deletion and dispatch
      degrades with the NoTrigger message).
- [ ] Exact alarm toggle, once decided.
- [ ] Migrate literals to translations once the UI stabilizes.
- [ ] Uniformize other editors with the TimeTrigger scaffold decisions:
    - [ ] Save asynchronously (all editors save synchronously today).
    - [ ] Extract the body to a file-level `@Composable fun`.
    - [ ] Rename the constructor id parameter to `editingId`.
    - [ ] Header action text: "Done" (Nfc, Shortcut, BluetoothDevice,
          BluetoothConnect, ChangeSetting still say "Save").
    - [ ] `Column` with `verticalScroll(rememberScrollState())` +
          `padding(8.dp)` (Nfc/Automation lack padding, Shortcut/
          SettingsPage lack scroll, Application scrolls inner content).
    - [ ] ShortcutTriggerEditor: replace the manual dirty/ConfirmDialog
          with `rememberDiscardableState`.
- [ ] Consider onRemove/Save entity callbacks if other entities than TimeTrigger needs them.
    That will make each entity more autocontained and centralized
    and will unify adhoc actions on import/save/remove.
    






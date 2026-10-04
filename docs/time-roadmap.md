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
- Reconcile hooks: app onCreate, DataChangeBus subscription, boot receiver.

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
- [ ] Editor:
    - [ ] Shell from `ShortcutTriggerEditor` / `NfcTriggerEditor`:
          `StackedScreen` + `EditorHeader` + `rememberDiscardableState` +
          save via `repository.trigger.save(...)`, with default values
          (`startAt = now`, `displayName` empty). Then flip `editorFactory`.
    - [ ] TDD for `editorFactory`
          - [x] `editor returns TimeTriggerEditor, without id`
          - [ ] `editor returns TimeTriggerEditor, with id`
    - [ ] `displayName` field (pattern already in `ShortcutTriggerEditor`).
    - [ ] Decide the date/time input: invest in a full editor now
          (if its features will match the eventual recurring editor)
          vs. a basic Material Compose editor to swap later.
- [ ] Dispatch
    - [ ] `nextOccurrence` one-shot (TDD: 0, 1, N cases).
          Past -> null, future -> `startAt`, `now` boundary.
    - [ ] How to program the triggering
    - [ ] How to dispatch the triggering

## Phase B -- objective 2: reschedule after each fire

- [ ] `recurrence` and `summary` attributes.
- [ ] `nextOccurrence` recurrence (TDD: 1, N cases).
- [ ] Editor gains the `recurrence` field.
- [ ] Receiver reschedules the next occurrence after fire.

## Phase C -- objective 3: reschedule on boot

- [ ] Boot receiver + reconcile on boot and app open.
      Decision function under TDD.
      Schedule on save: TBD within phase A dispatch
      (`How to program the triggering`).

## Phase D -- objective 4: run the triggers missed while off

- [ ] `onMissed` attribute (Skip / CatchUp).
- [ ] Editor gains the `onMissed` field.
- [ ] CatchUp on boot: run the triggers whose `scheduledFor` passed.
      Fold-back: next after `scheduledFor`, not after now.

## Later

- [ ] Exact alarm toggle, once decided.
- [ ] Migrate literals to translations once the UI stabilizes.

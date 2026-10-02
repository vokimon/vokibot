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
    - [x] seralization
    - [x] deserialization
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
- [ ] `displayName`
    - [ ] fixture gains displayName
- [ ] infrastructure
    - [ ] fixture gains type
    - [ ] `Trigger` implementation: getTitle, description, icon
    - [ ] companion `EntityMetadata`, 2 keys via `proposal.yaml`
    - [ ] `register()` in `EntityBootstrap`
    - [ ] wiring tests: registered entityClass, editor, roundtrip
- [ ] Minimal `TimeTriggerEditor` (displayName + startAt).
      UI, so wiring tests only.
      -> only path to create a trigger for manual testing.
- [ ] `nextOccurrence` one-shot (TDD: 0, 1, N cases).
      Past -> null, future -> `startAt`, `now` boundary.
- [ ] `TimeAlarmScheduler`, `TimeTriggerReceiver`, manifest entry.
      Inexact alarm, no permission needed.
      Schedule on save via DataChangeBus (from phase C):
      entry point for the manual test.
      `scheduledFor` written by the scheduler (characterization).
      Manual test on device.

## Phase B -- objective 2: reschedule after each fire

- [ ] `recurrence` and `summary` attributes.
- [ ] `nextOccurrence` recurrence (TDD: 1, N cases).
- [ ] Editor gains the `recurrence` field.
- [ ] Receiver reschedules the next occurrence after fire.

## Phase C -- objective 3: reschedule on boot

- [ ] Boot receiver + reconcile on boot and app open.
      Decision function under TDD.
      Schedule on save already landed in phase A.

## Phase D -- objective 4: run the triggers missed while off

- [ ] `onMissed` attribute (Skip / CatchUp).
- [ ] Editor gains the `onMissed` field.
- [ ] CatchUp on boot: run the triggers whose `scheduledFor` passed.
      Fold-back: next after `scheduledFor`, not after now.

## Later

- [ ] Exact alarm toggle, once decided.
- [ ] Migrate literals to translations once the UI stabilizes.

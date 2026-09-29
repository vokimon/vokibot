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
- Strings stay literal until the UI stabilizes,
  translated through `proposal.yaml` as needed.
- The schema is free to change during development.
  Migration is only needed between releases.

## Stage 1 -- entity

- [x] `LocalDateTimeIsoSerializer`.
      Three RED/GREEN pairs: parse, malformed input, ISO encode.
- [ ] infrastructure, zero attributes.
      Minimal `TimeTrigger(id)` plus metadata, registration,
      `proposal.yaml` with 2 keys, and test helpers.
      Wiring tests pass on arrival, so no RED.
- [ ] `displayName`.
      One RED/GREEN pair: `getTitle with displayName`.
- [ ] `startAt`.
      Field plus serializer annotation plus fixture.
      Characterization only, covered by existing roundtrip tests.
- [ ] `recurrence` and `summary`.
      Four RED/GREEN pairs: daily, once, weekly,
      and `getTitle without displayName`.

## Stage 2 -- editor

- [ ] `TimeTriggerEditor` replaces `NotYetImplementedEditor`.
  UI, so wiring tests only.
- [ ] Add the `onMissed` field here.
  Characterization only.

## Stage 3 -- nextOccurrence (TDD: 0, 1, N cases)

- [ ] Once past returns null, daily, weekly, valid-from boundary.
- [ ] DST gap day and overlap day.
- [ ] Late delivery across the fold-back:
  next after `scheduledFor`, not after now.

## Stage 4 -- scheduling (end-to-end one-shot)

- [ ] `TimeAlarmScheduler`, `TimeTriggerReceiver`, manifest entry.
  Inexact alarms, no permission needed.
- [ ] Add the `scheduledFor` field here.
  Written by the scheduler.

## Stage 5 -- recovery

- [ ] Recurrence rescheduling in the receiver.
- [ ] Reconcile on boot, app open, and DataChangeBus.
  Decision function under TDD.
- [ ] `onMissed` Skip and CatchUp behavior.

## Stage 6 -- strings

- [ ] Migrate literals to translations once the UI stabilizes.

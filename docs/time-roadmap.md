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
- The entity grows attribute by attribute over one shared roundtrip
  fixture: each step widens the expected JSON (RED), then implements
  the field (GREEN). The first test of a new SUT carries a stub that
  compiles but fails by assertion.
  `@Serializable` arrived with `id`; `type` comes with infrastructure.
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

## Plan overview

Vertical thin slices over finished layers: the riskiest part
(Android scheduling) is touched in phase A, not last.
`nextOccurrence` starts one-shot and extends with recurrence.
-> stages reordered and merged: old Stage 1..6 are discarded.

## Phase A -- one-shot end to end

- [x] `LocalDateTimeIsoSerializer`.
      Three RED/GREEN pairs: parse, malformed input, ISO encode.
- [x] Roundtrip base.
      Empty `toJson` pair, then `fromJson` with bad JSON:
      validates and still stubs the result.
- [x] `id`, stored in the fixture.
      Two pairs: default is uuid; `toJson` stores `id`.
- [x] `fromJson` happy path.
      GREEN swaps the stub for `decodeFromString`,
      subsuming the `parseToJsonElement` step.
      -> `data class` + `toString` comparison.
- [ ] infrastructure.
      Implement `Trigger` (fixture gains `type`), plus metadata,
      registration, `proposal.yaml` with 2 keys, and test helpers.
      RED is the fixture widening for `type`;
      the wiring tests pass on arrival.
- [ ] `displayName`.
      Fixture pair + the `getTitle with displayName` pair.
- [ ] `startAt`.
      Fixture pair with the `LocalDateTimeIsoSerializer` annotation.
- [ ] Minimal `TimeTriggerEditor` (displayName + startAt).
      UI, so wiring tests only.
      -> only path to create a trigger for manual testing.
- [ ] `nextOccurrence` one-shot (TDD: 0, 1, N cases).
      Past -> null, future -> `startAt`, `now` boundary.
- [ ] `TimeAlarmScheduler`, `TimeTriggerReceiver`, manifest entry.
      Inexact alarm, no permission needed.
      `scheduledFor` written by the scheduler (characterization).
      Manual test on device.

## Phase B -- recurrence (on top of a working one-shot)

- [ ] `recurrence` and `summary`.
      once / daily / weekly fixture pairs,
      plus `getTitle without displayName`.
- [ ] `nextOccurrence` recurrence (TDD: 1, N cases).
      Daily, weekly, valid-from boundary,
      DST gap day and overlap day.
- [ ] Receiver reschedules the next occurrence after fire.
- [ ] Editor gains the `recurrence` field.

## Phase C -- robustness

- [ ] `onMissed` Skip and CatchUp behavior (TDD).
      Fold-back: next after `scheduledFor`, not after now.
- [ ] Reconcile on boot, app open, and DataChangeBus.
      Decision function under TDD.
- [ ] Exact alarm toggle, once decided.

## Phase D -- strings

- [ ] Migrate literals to translations once the UI stabilizes.

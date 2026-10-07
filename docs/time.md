# Time Triggers

## Existing methods

- AlarmManager
- WorkManager

## AlarmManager

### Characteristics

AlarmManager is intended for executions that should happen at a specific date and time.

Advantages:

- Supports exact execution (`setExact()` and `setExactAndAllowWhileIdle()`).
- Suitable for alarms, reminders and precise automations.
- Supports one-shot scheduling naturally.
- Alarms live in the system process: they survive the death of the app
  process (back from task list, swipe away, `am kill`).

Limitations:

- Exact alarms may require the `SCHEDULE_EXACT_ALARM` permission on API 31+.
- Repeating alarms are generally less flexible than manually scheduling the next occurrence after each execution.
- Android may still delay alarms in exceptional situations such as battery saver or manufacturer-specific optimizations.
- Do not survive reboot or app updates.
- Force-stop cancels the alarms and disables the receivers.
- Receivers run in the background: starting an activity requires a permission.

Recommended use:

- Exact execution requested by the user.
- Daily, weekly or cron-like schedules requiring good accuracy.

### Choosing a method

Two independent attributes decide which `set*` method fits: the precision
of the delivery, and how the method behaves in Doze (the idle state the
device enters when the screen has been off for a while).

| Precision \ Doze | Deferred to maintenance window | Fires in Doze (max. once per 9 min) | Exits Doze before firing |
|---|---|---|---|
| Inexact | `set()` | `setAndAllowWhileIdle()` | - |
| Window | `setWindow()` | - | - |
| Exact | `setExact()` | `setExactAndAllowWhileIdle()` | `setAlarmClock()` |

**Exact methods** have the payback of having an impact on energy use,
so Android requires `SCHEDULE_EXACT_ALARM` permission.
Revoking that permission will clear already scheduled alarms.

**Inexact methods** may fire within an hour later. More if battery restrictions apply.

`setAlarmClock()` also shows an alarm icon in the status bar.

The repeating methods (`setRepeating()`, `setInexactRepeating()`) are
inexact since API 19: schedule the next one-shot after each fire instead
(see "Recommended scheduling strategy").

Parameters shared by all methods:

- `type` combines two independent choices:
  - Time base:
    - `RTC` / `RTC_WAKEUP`: the wall clock. Use it for "at 07:00"
      scheduling; clock and timezone changes shift the correspondence
      between the stored instant and the displayed local time.
    - `ELAPSED_REALTIME` / `ELAPSED_REALTIME_WAKEUP`: milliseconds since
      boot, including deep sleep. Use it for "in 10 minutes" delays;
      unaffected by clock or timezone changes.
  - Wakeup:
    - `_WAKEUP`: the system wakes the CPU when the alarm fires (the screen
      stays off). Required to run while the device sleeps.
    - Without it, an alarm that comes due while the CPU is asleep waits
      until something else wakes the device.
- `triggerAtMillis`: alarms never fire before it.
- `operation`: the `PendingIntent` to run; its identity (request code +
  intent) is what `cancel()` matches.

### Creating a scheduled execution

1. Build an `Intent` targeting the application's `BroadcastReceiver`.
2. Store the trigger UUID in the Intent extras.
3. Create a `PendingIntent` using a deterministic request code derived from the UUID.
4. Register the alarm using the appropriate AlarmManager method.

Example:

```kotlin
val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
    putExtra(EXTRA_TRIGGER_ID, triggerId)
}

val pendingIntent = PendingIntent.getBroadcast(
    context,
    triggerId.hashCode(),
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

alarmManager.set(
    AlarmManager.RTC_WAKEUP,
    triggerTime,
    pendingIntent
)
```

The example uses the inexact `set()`; see "Choosing a method" for the
alternatives.

When the alarm fires, the receiver reads the UUID and calls:

```kotlin
dispatch(triggerId)
```

If the trigger is recurring, the receiver computes the next execution and schedules it again.

### Cancelling

To cancel an alarm, recreate exactly the same PendingIntent.

Example:

```kotlin
val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
    putExtra(EXTRA_TRIGGER_ID, triggerId)
}

val pendingIntent = PendingIntent.getBroadcast(
    context,
    triggerId.hashCode(),
    intent,
    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
)

pendingIntent?.let {
    alarmManager.cancel(it)
}
```

Updating a trigger consists of:

1. Cancel the existing alarm.
2. Create a new one using the updated configuration.

The UUID remains unchanged.

## WorkManager

### Characteristics

WorkManager is intended for guaranteed background execution rather than precise scheduling.

Advantages:

- Survives process death and device reboot.
- Automatically handles execution constraints.
- Does not require exact alarm permissions.
- Integrates well with Android power management.

Limitations:

- Execution time is not exact.
- The system may delay execution to optimize battery usage.
- Periodic work has a minimum interval of 15 minutes.

Recommended use:

- Best effort scheduling.
- Background synchronization.
- Automations where a small execution delay is acceptable.

### Creating a scheduled execution

Each trigger uses its UUID as the unique work name.

Example:

```kotlin
val work = OneTimeWorkRequestBuilder<TimeTriggerWorker>()
    .setInitialDelay(delay)
    .setInputData(
        workDataOf(
            EXTRA_TRIGGER_ID to triggerId.toString()
        )
    )
    .build()

WorkManager.getInstance(context).enqueueUniqueWork(
    triggerId.toString(),
    ExistingWorkPolicy.REPLACE,
    work
)
```

The Worker reads the UUID and calls:

```kotlin
dispatch(triggerId)
```

If the trigger is recurring, the Worker schedules the next execution after dispatching.

### Cancelling

Cancellation is performed using the same unique work name.

```kotlin
WorkManager.getInstance(context)
    .cancelUniqueWork(triggerId.toString())
```

Updating a trigger consists of:

1. Cancel the existing work.
2. Enqueue new work with the same UUID.

Alternatively, `ExistingWorkPolicy.REPLACE` can be used directly when scheduling, replacing any existing work associated with the UUID.

## Recommended scheduling strategy

Recurring schedules should not precompute all future executions.

Instead:

1. Schedule only the next execution.
2. When it fires, call `dispatch(triggerId)`.
3. Compute the following occurrence.
4. Schedule it.

Advantages:

- Trigger modifications only affect one scheduled task.
- Deleting a trigger only requires cancelling one scheduled task.
- Reboot recovery is straightforward.
- No obsolete schedules remain pending.

## UUID lifecycle

Trigger creation:

1. Generate a UUID.
2. Persist the trigger configuration.
3. Schedule the first execution.

Trigger update:

1. Cancel the existing schedule using the UUID.
2. Update the stored configuration.
3. Schedule the next execution using the same UUID.

Trigger deletion:

1. Cancel the existing schedule using the UUID.
2. Remove the trigger from persistent storage.

At execution time:

1. AlarmManager or WorkManager delivers the UUID.
2. The receiver or Worker extracts the UUID.
3. The application invokes:

```kotlin
dispatch(triggerId)
```

The scheduling backend is therefore completely transparent to the automation engine.

## Debugging

List the alarms registered for the app (trigger time, operation, request code):

```bash
APP_ID=net.canvoki.vokibot
adb shell dumpsys alarm | grep -A 8 $APP_ID
```

Kill the process without touching the alarms (verifies delivery with the app closed):

```bash
APP_ID=net.canvoki.vokibot
adb shell am kill $APP_ID
```

Force-stop is the special case that cancels alarms and disables receivers
until the app is opened again:

```bash
APP_ID=net.canvoki.vokibot
adb shell am force-stop $APP_ID
```


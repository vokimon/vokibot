package net.canvoki.vokibot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertIsUUID
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.shared.test.withFixedTime
import net.canvoki.vokibot.time.Recurrence
import net.canvoki.vokibot.time.TimeTrigger
import net.canvoki.vokibot.time.TimeTriggerEditor
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en")
class TimeTriggerTest {
    val startAt = LocalDateTime.of(2026, 6, 1, 8, 30)
    val displayName = "Morning coffee"

    fun timeTriggerBase(
        id: String? = "my_id",
        startAt: LocalDateTime = this.startAt,
        recurrence: Recurrence = Recurrence.None,
        displayName: String = this.displayName,
        weekdays: Set<DayOfWeek> = emptySet(),
    ) = TimeTrigger(
        id = id,
        displayName = displayName,
        startAt = startAt,
        recurrence = recurrence,
        weekdays = weekdays,
    )

    fun timeTriggerJson(overrides: JsonObjectBuilder.() -> Unit = {}): String =
        buildJsonObject {
            put("type", "trigger_time")
            put("id", "my_id")
            put("displayName", "Morning coffee")
            put("recurrence", "None")
            put("startAt", "2026-06-01T08:30")
            overrides()
        }.toString()

    fun assertNextOccurrence(
        recurrence: Recurrence,
        startAt: String,
        now: String,
        expected: String?,
        weekdays: Set<DayOfWeek> = emptySet(),
    ) {
        val trigger =
            timeTriggerBase(
                startAt = LocalDateTime.parse(startAt),
                recurrence = recurrence,
                weekdays = weekdays,
            )
        val actual = trigger.nextOccurrence(LocalDateTime.parse(now))?.toString()
        assertEquals(expected, actual)
    }

    @Test
    fun `toJson`() {
        assertJsonEqual(timeTriggerBase().toJson(), timeTriggerJson())
    }

    @Test
    fun `fromJson with malformed input throws`() {
        assertFailsWith<SerializationException> {
            TimeTrigger.fromJson("not a json")
        }
    }

    @Test
    fun `id defaults to uuid`() {
        assertIsUUID(timeTriggerBase(id = null).id)
    }

    @Test
    fun `scheduleDescription one shot shows startAt`() {
        assertEquals("2026-06-01 08:30", timeTriggerBase().scheduleDescription(context()))
    }

    @Test
    fun `scheduleDescription daily shows the phrase`() {
        assertEquals("08:30 daily", timeTriggerBase(recurrence = Recurrence.Daily).scheduleDescription(context()))
    }

    @Config(qualifiers = "ca")
    @Test
    fun `scheduleDescription daily in catalan`() {
        assertEquals("08:30 cada dia", timeTriggerBase(recurrence = Recurrence.Daily).scheduleDescription(context()))
    }

    @Test
    fun `scheduleDescription weekly one day shows the full weekday name`() {
        assertEquals(
            "Friday at 08:30",
            timeTriggerBase(
                recurrence = Recurrence.Weekly,
                weekdays = setOf(DayOfWeek.FRIDAY),
            ).scheduleDescription(context()),
        )
    }

    @Test
    fun `scheduleDescription weekly many days shows the short weekday names`() {
        assertEquals(
            "Fri, Sun at 08:30",
            timeTriggerBase(
                recurrence = Recurrence.Weekly,
                weekdays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
            ).scheduleDescription(context()),
        )
    }

    @Test
    fun `scheduleDescription weekly no days shows never`() {
        assertEquals(
            "Never",
            timeTriggerBase(recurrence = Recurrence.Weekly).scheduleDescription(context()),
        )
    }

    @Config(qualifiers = "ca")
    @Test
    fun `scheduleDescription weekly no days in catalan`() {
        assertEquals(
            "Mai",
            timeTriggerBase(recurrence = Recurrence.Weekly).scheduleDescription(context()),
        )
    }

    @Config(qualifiers = "ca")
    @Test
    fun `scheduleDescription weekly many days in catalan`() {
        assertEquals(
            "dv., dg. a les 08:30",
            timeTriggerBase(
                recurrence = Recurrence.Weekly,
                weekdays = setOf(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
            ).scheduleDescription(context()),
        )
    }

    @Config(qualifiers = "ca")
    @Test
    fun `scheduleDescription weekly one day in catalan`() {
        assertEquals(
            "divendres a les 08:30",
            timeTriggerBase(
                recurrence = Recurrence.Weekly,
                weekdays = setOf(DayOfWeek.FRIDAY),
            ).scheduleDescription(context()),
        )
    }

    @Test
    fun `nextOccurrenceHumanized daily after first occurrence returns relative`() {
        val now = LocalDateTime.of(2026, 5, 29, 9, 0)
        assertEquals(
            "In 23 hours",
            timeTriggerBase(recurrence = Recurrence.Daily)
                .nextOccurrenceHumanized(context(), now),
        )
    }

    @Test
    fun `nextOccurrenceHumanized one shot past returns expired`() {
        val now = LocalDateTime.of(2026, 6, 2, 0, 0)
        assertEquals(
            "expired",
            timeTriggerBase().nextOccurrenceHumanized(context(), now),
        )
    }

    @Test
    fun `description returns next occurrence when name is not given`() {
        val now = LocalDateTime.of(2026, 5, 29, 9, 0)
        withFixedTime(now) {
            val trigger =
                timeTriggerBase(
                    displayName = "",
                    recurrence = Recurrence.Daily,
                )
            assertEquals(
                "In 23 hours",
                trigger.description(context()),
            )
        }
    }

    @Test
    fun `description joins short and next occurrence when name is given`() {
        val now = LocalDateTime.of(2026, 5, 29, 9, 0)
        withFixedTime(now) {
            val trigger =
                timeTriggerBase(
                    displayName = "Given",
                    recurrence = Recurrence.Daily,
                )
            assertEquals(
                "08:30 daily | In 23 hours",
                trigger.description(context()),
            )
        }
    }

    @Test
    fun `iconRes is the schedule icon`() {
        assertEquals(R.drawable.ic_schedule, timeTriggerBase().iconRes)
    }

    fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `getTitle returns displayName`() {
        assertEquals(displayName, timeTriggerBase().getTitle(context()))
    }

    @Test
    fun `getTitle without name daily shows the schedule`() {
        assertEquals(
            "08:30 daily",
            timeTriggerBase(displayName = "", recurrence = Recurrence.Daily).getTitle(context()),
        )
    }

    @Test
    fun `entityClass is TimeTrigger`() {
        assertEquals(TimeTrigger::class, TimeTrigger.entityClass)
    }

    @Test
    fun `deserializer parses the fixture`() {
        val deserialized = TimeTrigger.deserializer(timeTriggerJson())
        assertEquals(timeTriggerBase().toString(), deserialized.toString())
    }

    @Test
    fun `polymorphic Trigger fromJson`() {
        assertIs<TimeTrigger>(Trigger.fromJson(timeTriggerJson()))
    }

    @Test
    fun `fromJson`() {
        val deserialized = TimeTrigger.fromJson(timeTriggerJson())
        assertEquals(timeTriggerBase().toString(), deserialized.toString())
    }

    @Test
    fun `fromJson without weekdays loads an empty set`() {
        val trigger = TimeTrigger.fromJson(timeTriggerJson())
        assertEquals(emptySet<DayOfWeek>(), trigger.weekdays)
    }

    @Test
    fun `fromJson with weekdays parses the enum names`() {
        val trigger =
            TimeTrigger.fromJson(
                timeTriggerJson {
                    put(
                        "weekdays",
                        buildJsonArray {
                            add("MONDAY")
                            add("THURSDAY")
                        },
                    )
                },
            )
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), trigger.weekdays)
    }

    @Test
    fun `toJson weekdays stores the enum names`() {
        val trigger =
            timeTriggerBase(weekdays = setOf(DayOfWeek.THURSDAY))
        assertJsonEqual(
            timeTriggerJson {
                put(
                    "weekdays",
                    buildJsonArray {
                        add("THURSDAY")
                    },
                )
            },
            trigger.toJson(),
        )
    }

    @Test
    fun `toJson weekdays stores multiple enum names in ISO order`() {
        val trigger =
            timeTriggerBase(weekdays = setOf(DayOfWeek.THURSDAY, DayOfWeek.MONDAY))
        assertJsonEqual(
            timeTriggerJson {
                put(
                    "weekdays",
                    buildJsonArray {
                        add("MONDAY")
                        add("THURSDAY")
                    },
                )
            },
            trigger.toJson(),
        )
    }

    @Test
    fun `editor returns TimeTriggerEditor, without id`() {
        val editor = StorableEntity.getEditorScreen("trigger_time", null)
        assertEquals(TimeTriggerEditor(), editor)
    }

    @Test
    fun `editor returns TimeTriggerEditor, with id`() {
        val editor = StorableEntity.getEditorScreen("trigger_time", "my_id")
        assertEquals(TimeTriggerEditor("my_id"), editor)
    }

    @Test
    fun `nextOccurrence future single shot returns startAt`() {
        assertNextOccurrence(
            recurrence = Recurrence.None,
            startAt = "2026-06-01T08:30",
            now = "2026-05-31T08:30",
            expected = "2026-06-01T08:30",
        )
    }

    @Test
    fun `nextOccurrence past single shot returns null`() {
        assertNextOccurrence(
            recurrence = Recurrence.None,
            startAt = "2026-06-01T08:30",
            now = "2026-06-02T08:30",
            expected = null,
        )
    }

    @Test
    fun `nextOccurrence daily after first occurrence returns second`() {
        assertNextOccurrence(
            recurrence = Recurrence.Daily,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T09:00",
            expected = "2026-06-02T08:30",
        )
    }

    @Test
    fun `nextOccurrence daily before first occurrence returns first`() {
        assertNextOccurrence(
            recurrence = Recurrence.Daily,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T07:00",
            expected = "2026-06-01T08:30",
        )
    }

    @Test
    fun `nextOccurrence daily later day before that day occurrency`() {
        assertNextOccurrence(
            recurrence = Recurrence.Daily,
            startAt = "2026-06-01T08:30",
            now = "2026-06-10T07:00",
            expected = "2026-06-10T08:30",
        )
    }

    @Test
    fun `nextOccurrence daily later day after that day occurrency`() {
        assertNextOccurrence(
            recurrence = Recurrence.Daily,
            startAt = "2026-06-01T08:30",
            now = "2026-06-10T09:00",
            expected = "2026-06-11T08:30",
        )
    }

    @Test
    fun `nextOccurrence daily days before startAt before today occurrency returns today`() {
        assertNextOccurrence(
            recurrence = Recurrence.Daily,
            startAt = "2026-06-01T08:30",
            now = "2026-05-01T07:00",
            expected = "2026-05-01T08:30",
        )
    }

    @Test
    fun `nextOccurrence weekly today selected before time returns today`() {
        assertNextOccurrence(
            recurrence = Recurrence.Weekly,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T07:00",
            weekdays = setOf(DayOfWeek.MONDAY),
            expected = "2026-06-01T08:30",
        )
    }

    @Test
    fun `nextOccurrence weekly today selected after time returns next week`() {
        assertNextOccurrence(
            recurrence = Recurrence.Weekly,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T09:00",
            weekdays = setOf(DayOfWeek.MONDAY),
            expected = "2026-06-08T08:30",
        )
    }

    @Test
    fun `nextOccurrence weekly without weekdays returns null`() {
        assertNextOccurrence(
            recurrence = Recurrence.Weekly,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T07:00",
            expected = null,
        )
    }

    @Test
    fun `nextOccurrence weekly later weekday selected returns it this week`() {
        assertNextOccurrence(
            recurrence = Recurrence.Weekly,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T09:00",
            weekdays = setOf(DayOfWeek.THURSDAY),
            expected = "2026-06-04T08:30",
        )
    }

    @Test
    fun `nextOccurrence weekly today selected after time returns nearest other weekday`() {
        assertNextOccurrence(
            recurrence = Recurrence.Weekly,
            startAt = "2026-06-01T08:30",
            now = "2026-06-01T09:00",
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            expected = "2026-06-04T08:30",
        )
    }
}

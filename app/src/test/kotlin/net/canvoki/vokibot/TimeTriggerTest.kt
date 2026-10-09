package net.canvoki.vokibot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.serialization.SerializationException
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertIsUUID
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.time.Recurrence
import net.canvoki.vokibot.time.TimeTrigger
import net.canvoki.vokibot.time.TimeTriggerEditor
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
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
    ) = TimeTrigger(
        id = id,
        displayName = displayName,
        startAt = startAt,
        recurrence = recurrence,
    )

    fun timeTriggerJson() =
        """
        {
          "type": "trigger_time",
          "id": "my_id",
          "displayName": "Morning coffee",
          "recurrence": "None",
          "startAt": "2026-06-01T08:30"
        }
        """

    fun assertNextOccurrence(
        recurrence: Recurrence,
        startAt: String,
        now: String,
        expected: String?,
    ) {
        val trigger =
            timeTriggerBase(
                startAt = LocalDateTime.parse(startAt),
                recurrence = recurrence,
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
    fun `description one shot shows startAt`() {
        assertEquals("2026-06-01 08:30", timeTriggerBase().description(context()))
    }

    @Test
    fun `description daily shows the phrase`() {
        assertEquals(
            "08:30 daily",
            timeTriggerBase(recurrence = Recurrence.Daily).description(context()),
        )
    }

    @Config(qualifiers = "ca")
    @Test
    fun `description daily in catalan`() {
        assertEquals(
            "08:30 cada dia",
            timeTriggerBase(recurrence = Recurrence.Daily).description(context()),
        )
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
}

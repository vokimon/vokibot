package net.canvoki.vokibot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.serialization.SerializationException
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertIsUUID
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.time.TimeTrigger
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

    fun timeTriggerBase(id: String? = "my_id") =
        TimeTrigger(
            id = id,
            displayName = displayName,
            startAt = startAt,
        )

    fun timeTriggerJson() =
        """
        {
          "type": "trigger_time",
          "id": "my_id",
          "displayName": "Morning coffee",
          "startAt": "2026-06-01T08:30"
        }
        """

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
    fun `description shows startAt`() {
        assertEquals("2026-06-01 08:30", timeTriggerBase().description)
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
}

package net.canvoki.vokibot

import kotlinx.serialization.SerializationException
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertIsUUID
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.time.TimeTrigger
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class TimeTriggerTest {
    fun timeTriggerBase(id: String? = "my_id") = if (id == null) TimeTrigger() else TimeTrigger(id)

    fun timeTriggerJson() =
        """
        {
          "id": "my_id"
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
    fun `fromJson`() {
        val deserialized = TimeTrigger.fromJson(timeTriggerJson())
        assertEquals(timeTriggerBase().toString(), deserialized.toString())
    }
}

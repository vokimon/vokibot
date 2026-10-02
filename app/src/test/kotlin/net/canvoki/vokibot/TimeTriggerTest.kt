package net.canvoki.vokibot

import kotlinx.serialization.SerializationException
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.time.TimeTrigger
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class TimeTriggerTest {
    fun timeTriggerBase() = TimeTrigger()

    fun timeTriggerJson() =
        """
        {
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
}

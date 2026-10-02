package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.time.TimeTrigger
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeTriggerTest {
    fun timeTriggerBase() =
        TimeTrigger(
        )


    fun timeTriggerJson() =
        """
        {
        }
        """

    @Test
    fun `toJson`() {
        assertJsonEqual(timeTriggerBase().toJson(), timeTriggerJson())
    }
}

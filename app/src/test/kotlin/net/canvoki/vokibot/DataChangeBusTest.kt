package net.canvoki.vokibot

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

class DataChangeBusTest {
    @After
    fun tearDown() {
        DataChangeBus.reset()
    }

    @Test
    fun `notify calls subscribed listener`() {
        var count = 0
        DataChangeBus.subscribe { count += 1 }

        DataChangeBus.emit()

        assertEquals(1, count)
    }

    @Test
    fun `unsubscribe stops notifications`() {
        var count = 0
        val listener = { count += 1 }
        DataChangeBus.subscribe(listener)

        DataChangeBus.unsubscribe(listener)
        DataChangeBus.emit()

        assertEquals(0, count)
    }

    @Test
    fun `reset clears all listeners`() {
        var count = 0
        DataChangeBus.subscribe { count += 1 }
        DataChangeBus.subscribe { count += 1 }

        DataChangeBus.reset()
        DataChangeBus.emit()

        assertEquals(0, count)
    }

    @Test
    fun `multiple listeners all notified`() {
        var a = 0
        var b = 0
        DataChangeBus.subscribe { a += 1 }
        DataChangeBus.subscribe { b += 1 }

        DataChangeBus.emit()

        assertEquals(1, a)
        assertEquals(1, b)
    }
}

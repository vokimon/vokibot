package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.vokibot.time.LocalDateTimeIsoSerializer
import org.junit.Test
import java.time.LocalDateTime

class LocalDateTimeIsoSerializerTest {
    @Test
    fun `deserialize ISO local date time parses it`() {
        val decoded = JsonConfig.decodeFromString(LocalDateTimeIsoSerializer, "\"2026-03-12T07:30\"")
        assertEquals(LocalDateTime.of(2026, 3, 12, 7, 30), decoded)
    }
}

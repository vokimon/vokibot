package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.vokibot.time.LocalDateTimeIsoSerializer
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.test.assertFailsWith

class LocalDateTimeIsoSerializerTest {
    @Test
    fun `deserialize ISO local date time parses it`() {
        val decoded = JsonConfig.decodeFromString(LocalDateTimeIsoSerializer, "\"2026-03-12T07:30\"")
        assertEquals(LocalDateTime.of(2026, 3, 12, 7, 30), decoded)
    }

    @Test
    fun `deserialize with malformed input throws DateTimeParseException`() {
        val exception =
            assertFailsWith<DateTimeParseException> {
                JsonConfig.decodeFromString(LocalDateTimeIsoSerializer, "\"not-a-date\"")
            }
        assertEquals(exception.parsedString, "not-a-date")
    }
}

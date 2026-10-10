package net.canvoki.shared.test

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.time.LocalDateTime

fun <T> withFixedTime(
    dateTime: LocalDateTime,
    block: () -> T,
): T {
    mockkStatic(LocalDateTime::class)

    try {
        every { LocalDateTime.now() } returns dateTime
        return block()
    } finally {
        unmockkStatic(LocalDateTime::class)
    }
}

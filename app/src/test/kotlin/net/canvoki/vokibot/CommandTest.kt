package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test
import kotlin.test.assertIs

class CommandTest {
    @Test
    fun `getRegisteredTypes returns all registered commands`() {
        val types = Command.getRegisteredTypes()
        val typeKeys = types.map { it.typeKey }.sorted().joinToString("\n")
        val expectedTypes =
            listOfNotNull(
                "access_provider",
                "bluetooth_connect",
                "change_setting",
                "launch_activity",
                "send_broadcast",
                "settings_page",
                "start_service",
            )

        assertEquals(
            expectedTypes.joinToString("\n"),
            typeKeys,
        )
    }

    @Test
    fun `fromJson returns UnknownCommand for unregistered type`() {
        val json = """{"type":"unknown_type","data":"test"}"""
        val result = Command.fromJson(json)
        assertIs<UnknownCommand>(result)
    }
}

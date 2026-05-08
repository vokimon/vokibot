package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test

class CommandTest {
    @Test
    fun `getRegisteredTypes returns all registered commands`() {
        val types = Command.getRegisteredTypes()
        val typeKeys = types.map { it.typeKey }.sorted().joinToString("\n")
        assertEquals(
            "access_provider\nlaunch_activity\nsend_broadcast\nstart_service",
            typeKeys,
        )
    }

    @Test
    fun `fromJson deserializes LaunchActivityCommand`() {
        val json =
            """{"type":"launch_activity","displayName":"Test",""" +
                """"packageName":"com.test","className":"MainActivity"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is LaunchActivityCommand)
    }

    @Test
    fun `fromJson deserializes SendBroadcastCommand`() {
        val json =
            """{"type":"send_broadcast","displayName":"Test",""" +
                """"packageName":"com.test","action":"android.intent.action.MAIN"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is SendBroadcastCommand)
    }

    @Test
    fun `fromJson returns UnknownCommand for unregistered type`() {
        val json = """{"type":"unknown_type","data":"test"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is UnknownCommand)
    }
}

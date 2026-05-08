package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test

class CommandTest {
    @Test
    fun `getRegisteredTypes returns all registered commands`() {
        val types = Command.getRegisteredTypes()
        val typeKeys = types.map { it.typeKey }.sorted().joinToString("\n")
        assertEquals(
            "access_provider\nlaunch_activity\nsend_broadcast\nsettings_page\nstart_service",
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

    @Test
    fun `polymorphic deserialization identifies launch_activity`() {
        val json =
            """{"type":"launch_activity","displayName":"T",""" +
                """"packageName":"p","className":"c"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is LaunchActivityCommand)
    }

    @Test
    fun `polymorphic deserialization identifies send_broadcast`() {
        val json =
            """{"type":"send_broadcast","displayName":"T",""" +
                """"packageName":"p","action":"a"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is SendBroadcastCommand)
    }

    @Test
    fun `polymorphic deserialization identifies start_service`() {
        val json =
            """{"type":"start_service","displayName":"T",""" +
                """"packageName":"p","className":"c"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is StartServiceCommand)
    }

    @Test
    fun `polymorphic deserialization identifies access_provider`() {
        val json =
            """{"type":"access_provider","displayName":"T",""" +
                """"packageName":"p","authority":"a","operation":"QUERY"}"""
        val result = Command.fromJson(json)
        assertEquals(true, result is AccessProviderCommand)
    }
}

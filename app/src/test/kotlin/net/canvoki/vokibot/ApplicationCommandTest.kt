package net.canvoki.vokibot

import kotlinx.serialization.json.Json
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationCommandTest {
    private val json =
        Json {
            explicitNulls = false
            encodeDefaults = true
            classDiscriminator = "type"
        }

    // ---------- LaunchActivityCommand ----------
    fun launchActivityCommandBase() =
        LaunchActivityCommand(
            displayName = "Open Maps",
            packageName = "com.google.android.apps.maps",
            className = "com.google.android.apps.maps.MapsActivity",
            action = "android.intent.action.VIEW",
            dataUri = "geo:0,0?q=Madrid",
            extras =
                mapOf(
                    "query" to ExtraValue.StringValue("gas stations"),
                    "zoom" to ExtraValue.IntValue(15),
                    "favorite" to ExtraValue.BooleanValue(true),
                ),
            flagList = listOf("NEW_TASK", "CLEAR_TOP"),
        )

    fun launchActivityCommandJson() =
        """
        {
          "type": "launch_activity",
          "displayName": "Open Maps",
          "packageName": "com.google.android.apps.maps",
          "className": "com.google.android.apps.maps.MapsActivity",
          "action": "android.intent.action.VIEW",
          "dataUri": "geo:0,0?q=Madrid",
          "extras": {
            "query": {"type": "string", "value": "gas stations"},
            "zoom": {"type": "int", "value": 15},
            "favorite": {"type": "boolean", "value": true}
          },
          "flagList": ["NEW_TASK", "CLEAR_TOP"]
        }
        """.trimIndent()

    @Test
    fun `LaunchActivityCommand toJson`() {
        assertJsonEqual(launchActivityCommandBase().toJson(), launchActivityCommandJson())
    }

    @Test
    fun `LaunchActivityCommand fromJson`() {
        val deserialized = ApplicationCommand.fromJson(launchActivityCommandJson())
        assertEquals(launchActivityCommandBase().toString(), deserialized.toString())
    }

    // ---------- SendBroadcastCommand ----------
    fun sendBroadcastCommandBase() =
        SendBroadcastCommand(
            displayName = "Send SMS",
            packageName = "com.android.messaging",
            action = "android.intent.action.SENDTO",
            dataUri = "smsto:+1234567890",
            extras = mapOf("sms_body" to ExtraValue.StringValue("Hello")),
            permission = "android.permission.SEND_SMS",
        )

    fun sendBroadcastCommandJson() =
        """
        {
          "type": "send_broadcast",
          "displayName": "Send SMS",
          "packageName": "com.android.messaging",
          "action": "android.intent.action.SENDTO",
          "dataUri": "smsto:+1234567890",
          "extras": {
            "sms_body": {"type": "string", "value": "Hello"}
          },
          "permission": "android.permission.SEND_SMS"
        }
        """.trimIndent()

    @Test
    fun `SendBroadcastCommand toJson`() {
        assertJsonEqual(sendBroadcastCommandBase().toJson(), sendBroadcastCommandJson())
    }

    @Test
    fun `SendBroadcastCommand fromJson`() {
        val deserialized = ApplicationCommand.fromJson(sendBroadcastCommandJson())
        assertEquals(sendBroadcastCommandBase().toString(), deserialized.toString())
    }

    // ---------- StartServiceCommand ----------
    fun startServiceCommandBase() =
        StartServiceCommand(
            displayName = "Sync Data",
            packageName = "com.example.app",
            className = "com.example.app.SyncService",
            action = "com.example.ACTION_SYNC",
            extras = mapOf("force" to ExtraValue.BooleanValue(true)),
        )

    fun startServiceCommandJson() =
        """
        {
          "type": "start_service",
          "displayName": "Sync Data",
          "packageName": "com.example.app",
          "className": "com.example.app.SyncService",
          "action": "com.example.ACTION_SYNC",
          "extras": {
            "force": {"type": "boolean", "value": true}
          }
        }
        """.trimIndent()

    @Test
    fun `StartServiceCommand toJson`() {
        assertJsonEqual(startServiceCommandBase().toJson(), startServiceCommandJson())
    }

    @Test
    fun `StartServiceCommand fromJson`() {
        val deserialized = ApplicationCommand.fromJson(startServiceCommandJson())
        assertEquals(startServiceCommandBase().toString(), deserialized.toString())
    }

    // ---------- AccessProviderCommand ----------
    fun accessProviderCommandBase() =
        AccessProviderCommand(
            displayName = "Read Contacts",
            packageName = "com.android.contacts",
            authority = "com.android.contacts",
            operation = ProviderOperation.QUERY,
            path = "contacts",
            extras = mapOf("limit" to ExtraValue.IntValue(10)),
        )

    fun accessProviderCommandJson() =
        """
        {
          "type": "access_provider",
          "displayName": "Read Contacts",
          "packageName": "com.android.contacts",
          "authority": "com.android.contacts",
          "operation": "QUERY",
          "path": "contacts",
          "extras": {
            "limit": {"type": "int", "value": 10}
          }
        }
        """.trimIndent()

    @Test
    fun `AccessProviderCommand toJson`() {
        assertJsonEqual(accessProviderCommandBase().toJson(), accessProviderCommandJson())
    }

    @Test
    fun `AccessProviderCommand fromJson`() {
        val deserialized = ApplicationCommand.fromJson(accessProviderCommandJson())
        assertEquals(accessProviderCommandBase().toString(), deserialized.toString())
    }

    // ---------- Polymorphic & Edge Cases ----------
    @Test
    fun `Polymorphic deserialization uses type discriminator`() {
        val jsonStrings =
            listOf(
                """{"type":"launch_activity","displayName":"Test","packageName":"pkg","className":"cls"}""",
                """{"type":"send_broadcast","displayName":"Test","packageName":"pkg","action":"act"}""",
                """{"type":"start_service","displayName":"Test","packageName":"pkg","className":"cls"}""",
                """{"type":"access_provider","displayName":"Test","packageName":"pkg","authority":"auth","operation":"QUERY"}""",
            )

        assertTrue(ApplicationCommand.fromJson(jsonStrings[0]) is LaunchActivityCommand)
        assertTrue(ApplicationCommand.fromJson(jsonStrings[1]) is SendBroadcastCommand)
        assertTrue(ApplicationCommand.fromJson(jsonStrings[2]) is StartServiceCommand)
        assertTrue(ApplicationCommand.fromJson(jsonStrings[3]) is AccessProviderCommand)
    }

    @Test
    fun `ExtraValue polymorphic serialization`() {
        val extras =
            mapOf(
                "str" to ExtraValue.StringValue("hello"),
                "num" to ExtraValue.IntValue(42),
                "lng" to ExtraValue.LongValue(123L),
                "bool" to ExtraValue.BooleanValue(true),
                "flt" to ExtraValue.FloatValue(3.14f),
            )

        val serialized = json.encodeToString(extras)
        assertTrue(serialized.contains("\"string\""))
        assertTrue(serialized.contains("\"int\""))
        assertTrue(serialized.contains("\"long\""))
        assertTrue(serialized.contains("\"boolean\""))
        assertTrue(serialized.contains("\"float\""))

        val deserialized = json.decodeFromString<Map<String, ExtraValue>>(serialized)
        assertEquals(
            (extras["str"] as ExtraValue.StringValue).value,
            (deserialized["str"] as ExtraValue.StringValue).value,
        )
        assertEquals((extras["num"] as ExtraValue.IntValue).value, (deserialized["num"] as ExtraValue.IntValue).value)
        assertEquals(
            (extras["bool"] as ExtraValue.BooleanValue).value,
            (deserialized["bool"] as ExtraValue.BooleanValue).value,
        )
    }

    @Test
    fun `Null and empty fields are handled correctly`() {
        val command = LaunchActivityCommand(displayName = "Minimal", packageName = "pkg", className = "cls")
        val deserialized = ApplicationCommand.fromJson(command.toJson()) as LaunchActivityCommand
        assertEquals(null, deserialized.action)
        assertEquals(null, deserialized.dataUri)
        assertTrue(deserialized.extras.isEmpty())
        assertTrue(deserialized.flagList.isEmpty())
    }

    @Test
    fun `toJson and fromJson are inverses for all command types`() {
        val commands =
            listOf<ApplicationCommand>(
                LaunchActivityCommand("A", "p", "c", flagList = listOf("NEW_TASK")),
                SendBroadcastCommand("B", "p", "act"),
                StartServiceCommand("C", "p", "c"),
                AccessProviderCommand("D", "p", "auth", ProviderOperation.QUERY),
            )
        commands.forEach { original ->
            val restored = ApplicationCommand.fromJson(original.toJson())
            assertEquals(original::class, restored::class)
            assertEquals(original.displayName, restored.displayName)
            assertEquals(original.packageName, restored.packageName)
        }
    }
}

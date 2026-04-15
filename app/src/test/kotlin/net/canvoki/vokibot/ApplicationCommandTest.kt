package net.canvoki.vokibot

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationCommandTest {
    private val json =
        Json {
            explicitNulls = false
            encodeDefaults = true
            classDiscriminator = "type"
        }

    @Test
    fun `LaunchActivityCommand serializes and deserializes`() {
        val command =
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

        val serialized = command.toJson()
        val deserialized = ApplicationCommand.fromJson(serialized) as LaunchActivityCommand
        assertEquals(
            deserialized.toString(),
            "LaunchActivityCommand(" +
                "displayName=Open Maps, " +
                "packageName=com.google.android.apps.maps, " +
                "className=com.google.android.apps.maps.MapsActivity, " +
                "action=android.intent.action.VIEW, " +
                "dataUri=geo:0,0?q=Madrid, " +
                "extras={query=StringValue(" +
                "value=gas stations), " +
                "zoom=IntValue(value=15), " +
                "favorite=BooleanValue(value=true)" +
                "}, " +
                "flagList=[NEW_TASK, CLEAR_TOP]" +
                ")",
        )
    }

    @Test
    fun `SendBroadcastCommand serializes and deserializes`() {
        val command =
            SendBroadcastCommand(
                displayName = "Send SMS",
                packageName = "com.android.messaging",
                action = "android.intent.action.SENDTO",
                dataUri = "smsto:+1234567890",
                extras = mapOf("sms_body" to ExtraValue.StringValue("Hello")),
                permission = "android.permission.SEND_SMS",
            )

        val serialized = command.toJson()
        val deserialized = ApplicationCommand.fromJson(serialized) as SendBroadcastCommand
        assertEquals(
            deserialized.toString(),
            "SendBroadcastCommand(" +
                "displayName=Send SMS, " +
                "packageName=com.android.messaging, " +
                "action=android.intent.action.SENDTO, " +
                "dataUri=smsto:+1234567890, " +
                "extras={sms_body=StringValue(value=Hello)}, " +
                "permission=android.permission.SEND_SMS" +
                ")",
        )
    }

    @Test
    fun `StartServiceCommand serializes and deserializes`() {
        val command =
            StartServiceCommand(
                displayName = "Sync Data",
                packageName = "com.example.app",
                className = "com.example.app.SyncService",
                action = "com.example.ACTION_SYNC",
                extras = mapOf("force" to ExtraValue.BooleanValue(true)),
            )

        val serialized = command.toJson()
        val deserialized = ApplicationCommand.fromJson(serialized) as StartServiceCommand

        assertEquals(
            deserialized.toString(),
            "StartServiceCommand(" +
                "displayName=Sync Data, " +
                "packageName=com.example.app, " +
                "className=com.example.app.SyncService, " +
                "action=com.example.ACTION_SYNC, " +
                "extras={force=BooleanValue(value=true)}" +
                ")",
        )
        assertEquals(command.className, deserialized.className)
        assertEquals(command.action, deserialized.action)
        assertEquals(
            (command.extras["force"] as ExtraValue.BooleanValue).value,
            (deserialized.extras["force"] as ExtraValue.BooleanValue).value,
        )
    }

    @Test
    fun `AccessProviderCommand serializes and deserializes`() {
        val command =
            AccessProviderCommand(
                displayName = "Read Contacts",
                packageName = "com.android.contacts",
                authority = "com.android.contacts",
                operation = ProviderOperation.QUERY,
                path = "contacts",
                extras = mapOf("limit" to ExtraValue.IntValue(10)),
            )

        val serialized = command.toJson()
        val deserialized = ApplicationCommand.fromJson(serialized) as AccessProviderCommand
        assertEquals(
            deserialized.toString(),
            "AccessProviderCommand(" +
                "displayName=Read Contacts, " +
                "packageName=com.android.contacts, " +
                "authority=com.android.contacts, " +
                "operation=QUERY, " +
                "path=contacts, " +
                "mimeType=null, " +
                "extras={limit=IntValue(value=10)}" +
                ")",
        )
    }

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

        // Verify type discriminators are present
        assertTrue(serialized.contains("\"string\""))
        assertTrue(serialized.contains("\"int\""))
        assertTrue(serialized.contains("\"long\""))
        assertTrue(serialized.contains("\"boolean\""))
        assertTrue(serialized.contains("\"float\""))

        // Round-trip
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
        val command =
            LaunchActivityCommand(
                displayName = "Minimal",
                packageName = "pkg",
                className = "cls",
                // All optional fields omitted/null
            )

        val serialized = command.toJson()
        val deserialized = ApplicationCommand.fromJson(serialized) as LaunchActivityCommand

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
            val json = original.toJson()
            val restored = ApplicationCommand.fromJson(json)
            assertEquals(original::class, restored::class)
            assertEquals(original.displayName, restored.displayName)
            assertEquals(original.packageName, restored.packageName)
        }
    }
}

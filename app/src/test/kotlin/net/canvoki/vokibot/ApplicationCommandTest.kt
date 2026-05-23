package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class ApplicationCommandTest {
    // ---------- LaunchActivityCommand ----------
    fun launchActivityCommandBase() =
        LaunchActivityCommand(
            displayName = "Open Maps",
            packageName = "com.google.android.apps.maps",
            className = "com.android.gl.maps.MainActivity",
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
          "className": "com.android.gl.maps.MainActivity",
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

    @Test
    fun `LaunchActivityCommand title returns displayName`() {
        val cmd = launchActivityCommandBase()
        assertEquals(cmd.displayName, cmd.title)
    }

    @Test
    fun `LaunchActivityCommand description returns packageName className`() {
        val cmd = launchActivityCommandBase()
        assertEquals("com.google.android.apps.maps/com.android.gl.maps.MainActivity", cmd.description)
    }

    @Test
    fun `LaunchActivityCommand description when className shares package prefix returns shortened`() {
        val cmd =
            LaunchActivityCommand(
                displayName = "Open Maps",
                packageName = "com.google.android.apps.maps",
                className = "com.google.android.apps.maps.MapsActivity",
            )
        assertEquals("com.google.android.apps.maps/.MapsActivity", cmd.description)
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

    @Test
    fun `SendBroadcastCommand title returns displayName`() {
        val cmd = sendBroadcastCommandBase()
        assertEquals(cmd.displayName, cmd.title)
    }

    @Test
    fun `SendBroadcastCommand description returns packageName action`() {
        val cmd = sendBroadcastCommandBase()
        assertEquals("com.android.messaging/android.intent.action.SENDTO", cmd.description)
    }

    // ---------- StartServiceCommand ----------
    fun startServiceCommandBase() =
        StartServiceCommand(
            displayName = "Sync Data",
            packageName = "com.example.app",
            className = "com.android.sync.SyncWorker",
            action = "com.example.ACTION_SYNC",
            extras = mapOf("force" to ExtraValue.BooleanValue(true)),
        )

    fun startServiceCommandJson() =
        """
        {
          "type": "start_service",
          "displayName": "Sync Data",
          "packageName": "com.example.app",
          "className": "com.android.sync.SyncWorker",
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

    @Test
    fun `StartServiceCommand title returns displayName`() {
        val cmd = startServiceCommandBase()
        assertEquals(cmd.displayName, cmd.title)
    }

    @Test
    fun `StartServiceCommand description returns packageName className`() {
        val cmd = startServiceCommandBase()
        assertEquals("com.example.app/com.android.sync.SyncWorker", cmd.description)
    }

    @Test
    fun `StartServiceCommand description when className shares package prefix returns shortened`() {
        val cmd =
            StartServiceCommand(
                displayName = "Sync Data",
                packageName = "com.example.app",
                className = "com.example.app.SyncService",
            )
        assertEquals("com.example.app/.SyncService", cmd.description)
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

    @Test
    fun `AccessProviderCommand title returns displayName`() {
        val cmd = accessProviderCommandBase()
        assertEquals(cmd.displayName, cmd.title)
    }

    @Test
    fun `AccessProviderCommand description returns packageName authority`() {
        val cmd = accessProviderCommandBase()
        assertEquals("com.android.contacts/com.android.contacts", cmd.description)
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

        val serialized = JsonConfig.encodeToString(extras)
        assertTrue(serialized.contains("\"string\""))
        assertTrue(serialized.contains("\"int\""))
        assertTrue(serialized.contains("\"long\""))
        assertTrue(serialized.contains("\"boolean\""))
        assertTrue(serialized.contains("\"float\""))

        val deserialized = JsonConfig.decodeFromString<Map<String, ExtraValue>>(serialized)
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

    // ---------- ExtraValue type mapping ----------

    private fun checkDefaultValue(
        spec: ExtraSpec,
        expectedClass: KClass<*>,
    ) = assertEquals(expectedClass, spec.defaultValue()::class)

    @Test
    fun `defaultValue for URI returns UriValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.URI), ExtraValue.UriValue::class)
    }

    @Test
    fun `defaultValue for STRING returns StringValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.STRING), ExtraValue.StringValue::class)
    }

    @Test
    fun `defaultValue for INT returns IntValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.INT), ExtraValue.IntValue::class)
    }

    @Test
    fun `defaultValue for BOOLEAN returns BooleanValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.BOOLEAN), ExtraValue.BooleanValue::class)
    }

    @Test
    fun `defaultValue for STRING_ARRAY returns StringArrayValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.STRING_ARRAY), ExtraValue.StringArrayValue::class)
    }

    @Test
    fun `defaultValue for URI_LIST returns UriListValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.URI_LIST), ExtraValue.UriListValue::class)
    }

    @Test
    fun `StringValue serializes with string discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.StringValue("hello") as ExtraValue)
        assertJsonEqual("""{"type": "string", "value": "hello"}""", json)
    }

    @Test
    fun `IntValue serializes with int discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.IntValue(42) as ExtraValue)
        assertJsonEqual("""{"type": "int", "value": 42}""", json)
    }

    @Test
    fun `LongValue serializes with long discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.LongValue(123L) as ExtraValue)
        assertJsonEqual("""{"type": "long", "value": 123}""", json)
    }

    @Test
    fun `BooleanValue serializes with boolean discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.BooleanValue(true) as ExtraValue)
        assertJsonEqual("""{"type": "boolean", "value": true}""", json)
    }

    @Test
    fun `FloatValue serializes with float discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.FloatValue(3.14f) as ExtraValue)
        assertJsonEqual("""{"type": "float", "value": 3.14}""", json)
    }

    @Test
    fun `UriValue serializes with uri discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.UriValue("geo:0,0") as ExtraValue)
        assertJsonEqual("""{"type": "uri", "value": "geo:0,0"}""", json)
    }

    @Test
    fun `StringArrayValue serializes with string_array discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.StringArrayValue(listOf("a")) as ExtraValue)
        assertJsonEqual("""{"type": "string_array", "values": ["a"]}""", json)
    }

    @Test
    fun `UriListValue serializes with uri_list discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.UriListValue(listOf("geo:0,0")) as ExtraValue)
        assertJsonEqual("""{"type": "uri_list", "values": ["geo:0,0"]}""", json)
    }

    // ---------- isDefault ----------

    private fun checkDefault(
        value: ExtraValue,
        expectDefault: Boolean,
    ) = assertEquals(expectDefault, value.isDefault())

    @Test
    fun `StringValue with empty string is default`() {
        checkDefault(ExtraValue.StringValue(""), true)
    }

    @Test
    fun `StringValue with non-empty string is not default`() {
        checkDefault(ExtraValue.StringValue("hello"), false)
    }

    @Test
    fun `IntValue with zero is default`() {
        checkDefault(ExtraValue.IntValue(0), true)
    }

    @Test
    fun `IntValue with non-zero is not default`() {
        checkDefault(ExtraValue.IntValue(42), false)
    }

    @Test
    fun `BooleanValue with false is default`() {
        checkDefault(ExtraValue.BooleanValue(false), true)
    }

    @Test
    fun `BooleanValue with true is not default`() {
        checkDefault(ExtraValue.BooleanValue(true), false)
    }

    @Test
    fun `UriValue with empty string is default`() {
        checkDefault(ExtraValue.UriValue(""), true)
    }

    @Test
    fun `UriValue with non-empty string is not default`() {
        checkDefault(ExtraValue.UriValue("geo:0,0"), false)
    }

    @Test
    fun `StringArrayValue with empty list is default`() {
        checkDefault(ExtraValue.StringArrayValue(emptyList()), true)
    }

    @Test
    fun `StringArrayValue with non-empty list is not default`() {
        checkDefault(ExtraValue.StringArrayValue(listOf("a")), false)
    }

    @Test
    fun `UriListValue with empty list is default`() {
        checkDefault(ExtraValue.UriListValue(emptyList()), true)
    }

    @Test
    fun `UriListValue with non-empty list is not default`() {
        checkDefault(ExtraValue.UriListValue(listOf("geo:0,0")), false)
    }

    // ---------- toExtraType ----------

    private fun checkExtraType(
        value: ExtraValue,
        expected: ExtraType,
    ) = assertEquals(expected, value.toExtraType())

    @Test
    fun `StringValue toExtraType is STRING`() {
        checkExtraType(ExtraValue.StringValue(""), ExtraType.STRING)
    }

    @Test
    fun `IntValue toExtraType is INT`() {
        checkExtraType(ExtraValue.IntValue(0), ExtraType.INT)
    }

    @Test
    fun `LongValue toExtraType is INT`() {
        checkExtraType(ExtraValue.LongValue(0L), ExtraType.INT)
    }

    @Test
    fun `BooleanValue toExtraType is BOOLEAN`() {
        checkExtraType(ExtraValue.BooleanValue(false), ExtraType.BOOLEAN)
    }

    @Test
    fun `FloatValue toExtraType is STRING`() {
        checkExtraType(ExtraValue.FloatValue(0f), ExtraType.STRING)
    }

    @Test
    fun `UriValue toExtraType is URI`() {
        checkExtraType(ExtraValue.UriValue(""), ExtraType.URI)
    }

    @Test
    fun `StringArrayValue toExtraType is STRING_ARRAY`() {
        checkExtraType(ExtraValue.StringArrayValue(emptyList()), ExtraType.STRING_ARRAY)
    }

    @Test
    fun `UriListValue toExtraType is URI_LIST`() {
        checkExtraType(ExtraValue.UriListValue(emptyList()), ExtraType.URI_LIST)
    }
}

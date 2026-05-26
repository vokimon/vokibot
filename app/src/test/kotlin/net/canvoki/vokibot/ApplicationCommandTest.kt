package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationCommandTest {
    // ---------- LaunchActivityCommand ----------
    fun launchActivityCommandBase() =
        LaunchActivityCommand(
            id = "test-launch-id",
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
          "id": "test-launch-id",
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
            id = "test-send-id",
            displayName = "Send SMS",
            packageName = "com.android.messaging",
            className = "com.android.messaging.services.SmsReceiver",
            action = "android.intent.action.SENDTO",
            dataUri = "smsto:+1234567890",
            extras = mapOf("sms_body" to ExtraValue.StringValue("Hello")),
        )

    fun sendBroadcastCommandJson() =
        """
        {
          "type": "send_broadcast",
          "id": "test-send-id",
          "displayName": "Send SMS",
          "packageName": "com.android.messaging",
          "className": "com.android.messaging.services.SmsReceiver",
          "action": "android.intent.action.SENDTO",
          "dataUri": "smsto:+1234567890",
          "extras": {
            "sms_body": {"type": "string", "value": "Hello"}
          }
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
        assertEquals("com.android.messaging/.services.SmsReceiver", cmd.description)
    }

    // ---------- StartServiceCommand ----------
    fun startServiceCommandBase() =
        StartServiceCommand(
            id = "test-start-id",
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
          "id": "test-start-id",
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
            id = "test-access-id",
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
          "id": "test-access-id",
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
                LaunchActivityCommand(
                    displayName = "A",
                    packageName = "p",
                    className = "c",
                    flagList = listOf("NEW_TASK"),
                ),
                SendBroadcastCommand(displayName = "B", packageName = "p", className = null, action = "act"),
                StartServiceCommand(displayName = "C", packageName = "p", className = "c"),
                AccessProviderCommand(
                    displayName = "D",
                    packageName = "p",
                    authority = "auth",
                    operation = ProviderOperation.QUERY,
                ),
            )
        commands.forEach { original ->
            val restored = ApplicationCommand.fromJson(original.toJson())
            assertEquals(original::class, restored::class)
            assertEquals(original.displayName, restored.displayName)
            assertEquals(original.packageName, restored.packageName)
        }
    }
}

package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Test
import kotlin.test.assertIs

class ApplicationCommandTest {
    private fun assertRoundtrip(command: ApplicationCommand) {
        val restored = ApplicationCommand.fromJson(command.toJson())
        assertEquals(command, restored)
    }

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

    @Test
    fun `LaunchActivityCommand polymorphic load`() {
        assertIs<LaunchActivityCommand>(Command.fromJson(launchActivityCommandJson()))
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

    @Test
    fun `SendBroadcastCommand polymorphic load`() {
        assertIs<SendBroadcastCommand>(Command.fromJson(sendBroadcastCommandJson()))
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

    @Test
    fun `StartServiceCommand polymorphic load`() {
        assertIs<StartServiceCommand>(Command.fromJson(startServiceCommandJson()))
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

    @Test
    fun `AccessProviderCommand polymorphic load`() {
        assertIs<AccessProviderCommand>(Command.fromJson(accessProviderCommandJson()))
    }

    // ---------- Null and empty fields ----------
    @Test
    fun `Null and empty fields are handled correctly`() {
        val command = LaunchActivityCommand(displayName = "Minimal", packageName = "pkg", className = "cls")
        val deserialized = ApplicationCommand.fromJson(command.toJson())
        assertEquals(command, deserialized)
    }

    // ---------- Roundtrip ----------
    @Test
    fun `LaunchActivityCommand toJson and fromJson are inverses`() {
        assertRoundtrip(
            LaunchActivityCommand(
                displayName = "My Activity",
                packageName = "com.mypackage",
                className = "com.mypackage.MyActivity",
                flagList = listOf("NEW_TASK"),
            ),
        )
    }

    @Test
    fun `SendBroadcastCommand toJson and fromJson are inverses`() {
        assertRoundtrip(
            SendBroadcastCommand(
                displayName = "My Broadcast",
                packageName = "com.mypackage",
                className = "com.mypackage.MyReceiver",
                action = "com.mypackage.ACTION_TEST",
            ),
        )
    }

    @Test
    fun `StartServiceCommand toJson and fromJson are inverses`() {
        assertRoundtrip(
            StartServiceCommand(
                displayName = "My Service",
                packageName = "com.mypackage",
                className = "com.mypackage.MyService",
            ),
        )
    }

    @Test
    fun `AccessProviderCommand toJson and fromJson are inverses`() {
        assertRoundtrip(
            AccessProviderCommand(
                displayName = "My Provider",
                packageName = "com.mypackage",
                authority = "com.mypackage",
                operation = ProviderOperation.QUERY,
            ),
        )
    }
}

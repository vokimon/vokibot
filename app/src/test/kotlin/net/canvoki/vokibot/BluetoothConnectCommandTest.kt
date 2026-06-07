package net.canvoki.vokibot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertIsUUID
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en")
class BluetoothConnectCommandTest {
    fun commandBase(
        deviceName: String = "Device Name",
        action: ConnectionAction = ConnectionAction.CONNECT,
    ) = BluetoothConnectCommand(
        id = "my_id",
        macAddress = "AA:BB:CC:DD:EE:FF",
        deviceName = deviceName,
        action = action,
    )

    fun commandJson() =
        """
        {
          "id": "my_id",
          "type": "bluetooth_connect",
          "macAddress": "AA:BB:CC:DD:EE:FF",
          "deviceName": "Device Name",
          "action": "CONNECT",
          "affectedRoles": []
        }
        """.trimIndent()

    fun legacyCommandJson() =
        // No id attribute
        """
        {
          "type": "bluetooth_connect",
          "macAddress": "AA:BB:CC:DD:EE:FF",
          "deviceName": "Device Name",
          "action": "CONNECT",
          "affectedRoles": []
        }
        """.trimIndent()

    fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test fun `type is bluetooth_connect`() {
        val command: StorableEntity = commandBase()
        assertEquals("bluetooth_connect", command.type)
    }

    @Test fun `getTitle returns deviceName`() {
        val command = commandBase()
        assertEquals("Connect Device Name", command.getTitle(context()))
    }

    @Test fun `getTitle when disconnect`() {
        val command = commandBase(action = ConnectionAction.DISCONNECT)
        assertEquals("Disconnect Device Name", command.getTitle(context()))
    }

    @Test fun `getTitle falls back to macAddress when deviceName empty`() {
        val command = commandBase(deviceName = "")
        assertEquals("Connect AA:BB:CC:DD:EE:FF", command.getTitle(context()))
    }

    @Config(qualifiers = "ca")
    @Test
    fun `getTitle in other language`() {
        val command = commandBase()
        assertEquals("Connecta Device Name", command.getTitle(context()))
    }

    @Test fun `description returns macAddress`() {
        assertEquals("AA:BB:CC:DD:EE:FF", commandBase().description)
    }

    @Test fun `toJson`() {
        val cmd = commandBase()
        assertJsonEqual(cmd.toJson(), commandJson())
    }

    @Test fun `fromJson`() {
        val deserialized = BluetoothConnectCommand.fromJson(commandJson())
        assertEquals(commandBase().toString(), deserialized.toString())
    }

    @Test fun `fromJson legacy use content based implicity id`() {
        val deserialized = BluetoothConnectCommand.fromJson(legacyCommandJson())
        assertEquals("bluetooth_connect_AA_BB_CC_DD_EE_FF_connect", deserialized.id)
    }

    @Test fun `polymorphic Command fromJson`() {
        assertIs<BluetoothConnectCommand>(Command.fromJson(commandJson()))
    }

    @Test fun `registered with correct entityClass`() {
        val types = StorableEntity.registry.getRegisteredTypes(BluetoothConnectCommand::class)
        val typeKeys = types.map { it.typeKey }.sorted().joinToString("\n")
        assertEquals("bluetooth_connect", typeKeys)
    }

    @Test fun `editor returns BluetoothConnectCommandEditor, without id`() {
        val editor = StorableEntity.getEditorScreen("bluetooth_connect", null)
        assertEquals(BluetoothConnectCommandEditor(), editor)
    }

    @Test fun `editor returns BluetoothConnectCommandEditor, with id`() {
        val editor = StorableEntity.getEditorScreen("bluetooth_connect", "my_id")
        assertEquals(BluetoothConnectCommandEditor("my_id"), editor)
    }

    // Helper passes id as String? → forces secondary constructor.
    // Primary's id: String rejects nullable, so Kotlin resolves here.
    fun commandNullableId(id: String? = null) =
        BluetoothConnectCommand(
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "",
            action = ConnectionAction.CONNECT,
            affectedRoles = emptySet(),
            id = id,
        )

    @Test fun `id is UUID when passed null`() {
        val cmd = commandNullableId(id = null)
        assertIsUUID(cmd.id)
    }

    @Test fun `id is explicit`() {
        val cmd = commandNullableId(id = "explicit_id")
        assertEquals("explicit_id", cmd.id)
    }
}

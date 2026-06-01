package net.canvoki.vokibot

import android.content.Context
import io.mockk.mockk
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Test
import kotlin.test.assertIs

class BluetoothConnectCommandTest {
    fun commandBase(deviceName: String = "Device Name") =
        BluetoothConnectCommand(
            macAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = deviceName,
        )

    @Test fun `type is bluetooth_connect`() {
        val command: StorableEntity = commandBase()
        assertEquals("bluetooth_connect", command.type)
    }

    @Test fun `getTitle returns deviceName`() {
        assertEquals("Device Name", commandBase().getTitle(mockk<Context>()))
    }

    @Test fun `description returns macAddress`() {
        assertEquals("AA:BB:CC:DD:EE:FF", commandBase().description)
    }

    @Test fun `getTitle falls back to macAddress when deviceName empty`() {
        assertEquals("AA:BB:CC:DD:EE:FF", commandBase(deviceName = "").getTitle(mockk<Context>()))
    }

    fun commandJson() =
        """
        {
  "type": "bluetooth_connect",
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "deviceName": "Device Name",
  "action": "CONNECT"
}
        """.trimIndent()

    @Test fun `toJson`() {
        val cmd =
            BluetoothConnectCommand(
                macAddress = "AA:BB:CC:DD:EE:FF",
                deviceName = "Device Name",
            )
        assertJsonEqual(cmd.toJson(), commandJson())
    }

    @Test fun `id is constructed from mac and action`() {
        val cmd = commandBase()
        assertEquals("bluetooth_connect_AA_BB_CC_DD_EE_FF_connect", cmd.id)
    }

    @Test fun `fromJson`() {
        val deserialized = BluetoothConnectCommand.fromJson(commandJson())
        assertEquals(commandBase().toString(), deserialized.toString())
    }

    @Test fun `polymorphic Command fromJson`() {
        assertIs<BluetoothConnectCommand>(Command.fromJson(commandJson()))
    }

    @Test fun `registered with correct entityClass`() {
        val types = StorableEntity.registry.getRegisteredTypes(BluetoothConnectCommand::class)
        val typeKeys = types.map { it.typeKey }.sorted().joinToString("\n")
        assertEquals("bluetooth_connect", typeKeys)
    }
}

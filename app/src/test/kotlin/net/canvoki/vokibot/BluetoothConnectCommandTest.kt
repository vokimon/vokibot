package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test

class BluetoothConnectCommandTest {
    fun commandBase() = BluetoothConnectCommand(
        macAddress = "AA:BB:CC:DD:EE:FF",
        deviceName = "Test Device",
    )

    @Test fun `type is bluetooth_connect`() {
        val command: StorableEntity = commandBase()
        assertEquals("bluetooth_connect", command.type)
    }
}

package net.canvoki.vokibot

import android.content.Context
import io.mockk.mockk
import net.canvoki.shared.test.assertEquals
import org.junit.Test

class BluetoothConnectCommandTest {
    fun commandBase() = BluetoothConnectCommand(
        macAddress = "AA:BB:CC:DD:EE:FF",
        deviceName = "Device Name",
    )

    @Test fun `type is bluetooth_connect`() {
        val command: StorableEntity = commandBase()
        assertEquals("bluetooth_connect", command.type)
    }

    @Test fun `getTitle returns deviceName`() {
        assertEquals("Device Name", commandBase().getTitle(mockk<Context>()))
    }
}

package net.canvoki.vokibot

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import net.canvoki.shared.log

class BluetoothTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        log("BluetoothTriggerReceiver receiving: $intent")
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return

        val device =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            } ?: return

        val mac = device.safeAddress()
        val name = device.safeDisplayName()
        log("Bluetooth connected: $name ($mac)")

        val repo = FileDataRepository.fromContext(context)
        val triggerId = BluetoothDeviceTrigger.idFromMac(mac)
        if (!Automation.executeByTrigger(repo, triggerId, context)) {
            log("BluetoothTriggerReceiver: No automation for $name ($mac)")
        }
    }
}

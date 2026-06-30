package net.canvoki.vokibot.bluetooth

import android.content.Context
import android.graphics.drawable.Drawable
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.R
import net.canvoki.vokibot.EntityMetadata
import net.canvoki.vokibot.JsonConfig
import net.canvoki.vokibot.StorableEntity
import net.canvoki.vokibot.Trigger
import net.canvoki.vokibot.toFileSystemId
import net.canvoki.vokibot.common.BadgeDrawable

@Serializable
data class BluetoothDeviceTrigger(
    val name: String,
    val macAddress: String,
) : Trigger() {
    companion object : EntityMetadata {
        const val ID_PREFIX = "bluetooth_device_"

        override val typeKey = "trigger_bluetooth_device"
        override val entityClass = BluetoothDeviceTrigger::class
        override val labelRes = R.string.triggerlist_option_bluetooth_device
        override val iconRes = R.drawable.ic_bluetooth
        override val editorFactory =
            { triggerId: String? -> BluetoothDeviceTriggerEditor(triggerId) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.trigger_bluetooth_device_help

        fun idFromMac(mac: String) = "$ID_PREFIX${toFileSystemId(mac)}"

        fun register() = StorableEntity.register(this)

        fun fromJson(jsonString: String): BluetoothDeviceTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type: String = BluetoothDeviceTrigger.typeKey
    override val iconRes: Int get() = BluetoothDeviceTrigger.iconRes
    override val id: String get() = idFromMac(macAddress)

    override fun getTitle(context: Context): String {
        val device = bluetoothDeviceFromMac(context, macAddress)
        val label = device?.let { bluetoothDeviceLabelRes(it) }?.let { context.getString(it) }
        return if (label != null) "$name ($label)" else name
    }

    override val description: String get() = macAddress

    override fun loadIcon(context: Context): Drawable {
        val device = bluetoothDeviceFromMac(context, macAddress)
        val deviceIcon = bluetoothDeviceIcon(device)

        if (deviceIcon == BluetoothDeviceTrigger.iconRes) {
            return context.getDrawable(deviceIcon)!!
        }

        return BadgeDrawable(
            main = context.getDrawable(deviceIcon)!!,
            badge = context.getDrawable(BluetoothDeviceTrigger.iconRes)!!,
        )
    }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

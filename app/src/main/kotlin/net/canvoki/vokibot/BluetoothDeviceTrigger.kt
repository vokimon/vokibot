package net.canvoki.vokibot

import android.content.Context
import kotlinx.serialization.Serializable

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
        override val editorFactory = { _: String? -> NotYetImplementedEditor }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.trigger_bluetooth_device_help

        fun idFromMac(mac: String) = "$ID_PREFIX${toFileSystemId(mac)}"

        fun register() = StorableEntity.register(this)

        fun fromJson(jsonString: String): BluetoothDeviceTrigger =
            JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type: String = BluetoothDeviceTrigger.typeKey
    override val iconRes: Int get() = BluetoothDeviceTrigger.iconRes
    override val id: String get() = idFromMac(macAddress)

    override fun getTitle(context: Context): String = name

    override val description: String get() = macAddress

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

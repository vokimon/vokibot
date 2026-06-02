package net.canvoki.vokibot

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import kotlinx.serialization.Serializable
import net.canvoki.shared.log

@Serializable
enum class ConnectionAction { CONNECT, DISCONNECT }

@Serializable
data class BluetoothConnectCommand(
    val macAddress: String,
    val deviceName: String = "",
    val action: ConnectionAction = ConnectionAction.CONNECT,
) : Command() {
    override val id: String get() = "bluetooth_connect_${toFileSystemId(macAddress)}_${action.name.lowercase()}"
    override val type = "bluetooth_connect"
    override val iconRes: Int get() = R.drawable.ic_bluetooth

    override fun getTitle(context: Context): String =
        deviceName.ifEmpty { macAddress }.let {
            when (action) {
                ConnectionAction.CONNECT -> "Connect $it"
                ConnectionAction.DISCONNECT -> "Disonnect $it"
            }
        }

    override val description: String get() = macAddress

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        val device =
            bluetoothDeviceFromMac(context, macAddress) ?: run {
                log("BluetoothConnect: device not found for $macAddress")
                return
            }
        val adapter =
            context.getSystemService(BluetoothManager::class.java)?.adapter ?: run {
                log("BluetoothConnect: no adapter")
                return
            }
        val methodName = action.name.lowercase()

        adapter.getProfileProxy(
            context,
            object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(
                    profile: Int,
                    proxy: BluetoothProfile,
                ) {
                    try {
                        val method =
                            proxy::class.java.getDeclaredMethod(
                                methodName,
                                BluetoothDevice::class.java,
                            )
                        method.isAccessible = true
                        method.invoke(proxy, device)
                    } catch (e: Exception) {
                        log("BluetoothConnect: $methodName failed: $e")
                    }
                    adapter.closeProfileProxy(profile, proxy)
                }

                override fun onServiceDisconnected(profile: Int) {}
            },
            BluetoothProfile.A2DP,
        )
    }

    companion object : EntityMetadata {
        override val typeKey = "bluetooth_connect"
        override val entityClass = BluetoothConnectCommand::class
        override val labelRes = R.string.command_bluetooth_connect_label
        override val iconRes = R.drawable.ic_bluetooth
        override val editorFactory = { id: String? -> BluetoothConnectCommandEditor(id) }
        override val deserializer: ((String) -> StorableEntity)? = { fromJson(it) }
        override val helpRes = R.string.command_bluetooth_connect_help

        fun fromJson(jsonString: String): BluetoothConnectCommand =
            JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
    }
}

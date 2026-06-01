package net.canvoki.vokibot

import android.content.Context
import kotlinx.serialization.Serializable

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

    override fun getTitle(context: Context): String = deviceName.ifEmpty { macAddress }

    override val description: String get() = macAddress

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        // TODO
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

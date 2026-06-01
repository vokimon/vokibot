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

    override suspend fun execute(context: Context) {}

    companion object : EntityMetadata {
        override val typeKey = "bluetooth_connect"
        override val entityClass = BluetoothConnectCommand::class
        override val labelRes = 0  // TODO
        override val iconRes = 0  // TODO
        override val editorFactory = { _: String? -> NotYetImplementedEditor }
        override val deserializer: ((String) -> StorableEntity)? = { fromJson(it) }
        override val helpRes = 0  // TODO

        fun fromJson(jsonString: String): BluetoothConnectCommand =
            JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
    }
}

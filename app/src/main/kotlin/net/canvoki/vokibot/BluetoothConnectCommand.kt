package net.canvoki.vokibot

import android.content.Context
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ConnectionAction { CONNECT, DISCONNECT }

@Serializable
data class BluetoothConnectCommand(
    val macAddress: String,
    val deviceName: String = "",
    val action: ConnectionAction = ConnectionAction.CONNECT,
    val affectedRoles: Set<DisconnectableRole> = emptySet(),
    override val id: String = "bluetooth_connect_${toFileSystemId(macAddress)}_${action.name.lowercase()}",
) : Command() {
    // Secondary constructor accepts nullable id.
    // Because primary's id: String rejects nullable,
    // Kotlin resolves all id: String? calls to this secondary.
    // Params reordered (id before affectedRoles) to avoid JVM signature clash.
    constructor(
        macAddress: String,
        deviceName: String = "",
        action: ConnectionAction = ConnectionAction.CONNECT,
        id: String?,
        affectedRoles: Set<DisconnectableRole> = emptySet(),
    ) : this(
        macAddress = macAddress,
        deviceName = deviceName,
        action = action,
        affectedRoles = affectedRoles,
        id = UUID.randomUUID().toString(),
    )

    override val type = "bluetooth_connect"
    override val iconRes: Int get() = R.drawable.ic_bluetooth

    override fun getTitle(context: Context): String =
        deviceName.ifEmpty { macAddress }.let { displayName ->
            context.getString(
                when (action) {
                    ConnectionAction.CONNECT -> R.string.command_bluetooth_connect_title_connect
                    ConnectionAction.DISCONNECT -> R.string.command_bluetooth_connect_title_disconnect
                },
                displayName,
            )
        }

    override val description: String get() = macAddress

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {
        when (action) {
            ConnectionAction.CONNECT -> bluetoothConnect(context, macAddress)
            ConnectionAction.DISCONNECT -> bluetoothDisconnect(context, macAddress)
        }
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

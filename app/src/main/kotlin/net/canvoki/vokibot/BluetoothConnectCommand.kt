package net.canvoki.vokibot

import android.content.Context
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ConnectionAction { CONNECT, DISCONNECT }

@Serializable
data class BluetoothConnectCommand(
    override val id: String = UUID.randomUUID().toString(),
    val macAddress: String,
    val deviceName: String = "",
    val action: ConnectionAction = ConnectionAction.CONNECT,
) : Command() {
    override val type = "bluetooth_connect"
    override val iconRes: Int get() = R.drawable.ic_bluetooth

    override fun getTitle(context: Context): String = deviceName.ifEmpty { macAddress }
    override val description: String get() = macAddress

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override suspend fun execute(context: Context) {}
}

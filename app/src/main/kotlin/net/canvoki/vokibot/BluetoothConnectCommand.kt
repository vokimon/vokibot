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
    override val iconRes = R.drawable.ic_bluetooth

    override fun getTitle(context: Context): String = deviceName
    override val description: String = "TODO"

    override fun toJson(): String = """{"wrong":true}"""

    override suspend fun execute(context: Context) {}
}

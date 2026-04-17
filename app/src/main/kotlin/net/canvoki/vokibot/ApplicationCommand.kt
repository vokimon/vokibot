package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Type-safe extra values for Intents.
 */
@Serializable
sealed class ExtraValue {
    abstract fun addToIntent(
        intent: Intent,
        key: String,
    )

    @Serializable
    @SerialName("string")
    data class StringValue(
        val value: String,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, value)
        }
    }

    @Serializable
    @SerialName("int")
    data class IntValue(
        val value: Int,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, value)
        }
    }

    @Serializable
    @SerialName("long")
    data class LongValue(
        val value: Long,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, value)
        }
    }

    @Serializable
    @SerialName("boolean")
    data class BooleanValue(
        val value: Boolean,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, value)
        }
    }

    @Serializable
    @SerialName("float")
    data class FloatValue(
        val value: Float,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, value)
        }
    }
}

/**
 * Base class for automation commands that interact with other applications.
 */
@Serializable
sealed class ApplicationCommand {
    abstract val displayName: String
    abstract val packageName: String

    /**
     * String resource ID for the human-readable command type label.
     * Used for grouping, filtering, and display in lists.
     */
    @get:StringRes
    abstract val typeLabelRes: Int

    val id: String
        get() = displayName.toFileSystemId()

    private fun String.toFileSystemId(): String =
        replace(Regex("[^a-zA-Z0-9_.-]"), "_")
            .replace(Regex("_+"), "_")
            .take(64)
            .trim('_')
            .ifBlank { "unnamed" }

    /**
     * Execute this command when the trigger condition is met.
     */
    abstract suspend fun execute(context: Context)

    /**
     * Serialize this command to JSON.
     * Instance method because you already have the object.
     */
    fun toJson(): String = Companion.json.encodeToString(serializer(), this)

    companion object {
        private val json =
            Json {
                explicitNulls = false
                encodeDefaults = true
                classDiscriminator = "type"
            }

        /**
         * Deserialize a command from JSON.
         * Companion method because you don't have an instance yet.
         */
        fun fromJson(jsonString: String): ApplicationCommand = json.decodeFromString(serializer(), jsonString)
    }
}

/**
 * Launch an Activity (with optional action, data URI, and extras).
 */
@Serializable
@SerialName("launch_activity")
data class LaunchActivityCommand(
    override val displayName: String,
    override val packageName: String,
    val className: String,
    val action: String? = null,
    val dataUri: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
    val flagList: List<String> = emptyList(),
) : ApplicationCommand() {
    override val typeLabelRes: Int = R.string.command_type_launch_activity

    override suspend fun execute(context: Context) {
        val intent = Intent()
        intent.setClassName(packageName, className)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        action?.let { intent.action = it }
        dataUri?.let { intent.data = Uri.parse(it) }

        extras.entries.forEach { (key, value) ->
            value.addToIntent(intent, key)
        }

        for (flagName in flagList) {
            val flagValue =
                when (flagName) {
                    "NEW_TASK" -> Intent.FLAG_ACTIVITY_NEW_TASK
                    "CLEAR_TASK" -> Intent.FLAG_ACTIVITY_CLEAR_TASK
                    "NO_HISTORY" -> Intent.FLAG_ACTIVITY_NO_HISTORY
                    "MULTIPLE_TASK" -> Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    "CLEAR_TOP" -> Intent.FLAG_ACTIVITY_CLEAR_TOP
                    "SINGLE_TOP" -> Intent.FLAG_ACTIVITY_SINGLE_TOP
                    "REORDER_TO_FRONT" -> Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    else -> null
                } ?: continue
            intent.addFlags(flagValue)
        }
        context.startActivity(intent)
    }
}

/**
 * Send a Broadcast to a Receiver.
 */
@Serializable
@SerialName("send_broadcast")
data class SendBroadcastCommand(
    override val displayName: String,
    override val packageName: String,
    val action: String,
    val dataUri: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
    val permission: String? = null,
) : ApplicationCommand() {
    override val typeLabelRes: Int = R.string.command_type_send_broadcast

    override suspend fun execute(context: Context) {
        val intent = Intent(action)
        intent.setPackage(packageName)
        dataUri?.let { intent.data = Uri.parse(it) }

        extras.entries.forEach { (key, value) ->
            value.addToIntent(intent, key)
        }

        if (permission != null) {
            context.sendBroadcast(intent, permission)
        } else {
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Start a Service.
 */
@Serializable
@SerialName("start_service")
data class StartServiceCommand(
    override val displayName: String,
    override val packageName: String,
    val className: String,
    val action: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
) : ApplicationCommand() {
    override val typeLabelRes: Int = R.string.command_type_start_service

    override suspend fun execute(context: Context) {
        val intent = Intent()
        intent.setClassName(packageName, className)

        action?.let { intent.action = it }
        extras.entries.forEach { (key, value) ->
            value.addToIntent(intent, key)
        }

        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            context.startService(intent)
        }
    }
}

/**
 * Access a ContentProvider (read/write data).
 */
@Serializable
@SerialName("access_provider")
data class AccessProviderCommand(
    override val displayName: String,
    override val packageName: String,
    val authority: String,
    val operation: ProviderOperation,
    val path: String? = null,
    val mimeType: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
) : ApplicationCommand() {
    override val typeLabelRes: Int = R.string.command_type_access_provider

    override suspend fun execute(context: Context) {
        val uri = buildUri()

        when (operation) {
            ProviderOperation.READ -> {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.close()
            }
            ProviderOperation.WRITE -> {
                // Requires payload data
            }
            ProviderOperation.QUERY -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.close()
            }
        }
    }

    private fun buildUri(): Uri {
        val baseUri = Uri.parse("content://$authority")
        return path?.let { Uri.withAppendedPath(baseUri, it) } ?: baseUri
    }
}

/**
 * Operations for ContentProvider access.
 */
@Serializable
enum class ProviderOperation {
    READ,
    WRITE,
    QUERY,
}

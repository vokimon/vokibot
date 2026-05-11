package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.net.toUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun toFileSystemId(id: String): String =
    id
        .replace(Regex("[^a-zA-Z0-9_.-]"), "_")
        .replace(Regex("_+"), "_")
        .take(64)
        .trim('_')
        .ifBlank { "unnamed" }

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
sealed class ApplicationCommand : Command() {
    abstract val displayName: String
    abstract val packageName: String

    /**
     * String resource ID for the human-readable command type label.
     * Used for grouping, filtering, and display in lists.
     */
    @get:StringRes
    abstract val typeLabelRes: Int

    override val id: String
        get() = toFileSystemId(displayName)

    override val title: String
        get() = displayName

    // TODO: Make description composable to show "$packageName/$className" per type
    override val description: String
        get() = packageName

    protected fun descriptionWithClassName(className: String): String =
        if (className.startsWith(packageName)) {
            "$packageName/${className.removePrefix(packageName)}"
        } else {
            "$packageName/$className"
        }

    // TODO: Make iconRes composable to load dynamic icon from package manager
    @get:DrawableRes
    override val iconRes: Int
        get() = R.drawable.ic_brand

    /**
     * Serialize this command to JSON.
     * Instance method because you already have the object.
     */
    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        /**
         * Deserialize a command from JSON.
         */
        fun fromJson(jsonString: String): ApplicationCommand =
            StorableEntity.fromJson(jsonString, ApplicationCommand::class)
                ?: throw kotlinx.serialization.SerializationException(
                    "Failed to deserialize command",
                )
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
    override val type: String = "launch_activity"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_launch_activity

    override val description: String
        get() = descriptionWithClassName(className)

    override fun loadIcon(context: Context): Drawable? = getAppIcon(context, packageName, className)

    override suspend fun execute(context: Context) {
        val intent = Intent()
        intent.setClassName(packageName, className)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        action?.let { intent.action = it }
        dataUri?.let { intent.data = it.toUri() }

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

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "launch_activity"

        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = LaunchActivityCommand::class,
                labelRes = R.string.command_type_launch_activity,
                iconRes = R.drawable.ic_brand,
                editorFactory = { NotYetImplementedEditor },
                deserializer = { jsonString -> fromJson(jsonString) },
                helpRes = R.string.command_launch_activity_help,
            )

        fun fromJson(jsonString: String): LaunchActivityCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(TYPE_INFO)
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
    override val type: String = "send_broadcast"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_send_broadcast

    override val description: String
        get() = "$packageName/$action"

    override fun loadIcon(context: Context): Drawable? = getAppIcon(context, packageName)

    override suspend fun execute(context: Context) {
        val intent = Intent(action)
        intent.setPackage(packageName)
        dataUri?.let { intent.data = it.toUri() }

        extras.entries.forEach { (key, value) ->
            value.addToIntent(intent, key)
        }

        if (permission != null) {
            context.sendBroadcast(intent, permission)
        } else {
            context.sendBroadcast(intent)
        }
    }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "send_broadcast"

        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = SendBroadcastCommand::class,
                labelRes = R.string.command_type_send_broadcast,
                iconRes = R.drawable.ic_brand,
                editorFactory = { NotYetImplementedEditor },
                deserializer = { jsonString -> fromJson(jsonString) },
                helpRes = R.string.command_send_broadcast_help,
            )

        fun fromJson(jsonString: String): SendBroadcastCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(TYPE_INFO)
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
    override val type: String = "start_service"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_start_service

    override val description: String
        get() = descriptionWithClassName(className)

    override fun loadIcon(context: Context): Drawable? = getAppIcon(context, packageName, className)

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

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "start_service"

        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = StartServiceCommand::class,
                labelRes = R.string.command_type_start_service,
                iconRes = R.drawable.ic_brand,
                editorFactory = { NotYetImplementedEditor },
                deserializer = { jsonString -> fromJson(jsonString) },
                helpRes = R.string.command_control_service_help,
            )

        fun fromJson(jsonString: String): StartServiceCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(TYPE_INFO)
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
    override val type: String = "access_provider"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_access_provider

    override val description: String
        get() = "$packageName/$authority"

    override fun loadIcon(context: Context): Drawable? = getAppIcon(context, packageName)

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
        val baseUri = "content://$authority".toUri()
        return path?.let { Uri.withAppendedPath(baseUri, it) } ?: baseUri
    }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        const val TYPE = "access_provider"

        val TYPE_INFO =
            EntityTypeInfo(
                typeKey = TYPE,
                entityClass = AccessProviderCommand::class,
                labelRes = R.string.command_type_access_provider,
                iconRes = R.drawable.ic_brand,
                editorFactory = { NotYetImplementedEditor },
                deserializer = { jsonString -> fromJson(jsonString) },
                helpRes = R.string.command_access_provider_help,
            )

        fun fromJson(jsonString: String): AccessProviderCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(TYPE_INFO)
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

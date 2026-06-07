package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.net.toUri
import kotlinx.serialization.Serializable
import java.util.UUID

fun toFileSystemId(id: String): String =
    id
        .replace(Regex("[^a-zA-Z0-9_.-]"), "_")
        .replace(Regex("_+"), "_")
        .take(64)
        .trim('_')
        .ifBlank { "unnamed" }

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

    override fun getTitle(context: Context): String = displayName

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
        get() = R.drawable.ic_apps

    /**
     * Serialize this command to JSON.
     * Instance method because you already have the object.
     */
    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        fun resolveId(id: String?): String = UUID.randomUUID().toString()

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
data class LaunchActivityCommand(
    override val id: String,
    override val displayName: String,
    override val packageName: String,
    val className: String,
    val action: String? = null,
    val dataUri: String? = null,
    val dataMimeType: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
    val flagList: List<String> = emptyList(),
) : ApplicationCommand() {
    constructor(
        displayName: String,
        packageName: String,
        className: String,
        action: String? = null,
        dataUri: String? = null,
        dataMimeType: String? = null,
        extras: Map<String, ExtraValue> = emptyMap(),
        id: String?,
        flagList: List<String> = emptyList(),
    ) : this(
        id = UUID.randomUUID().toString(),
        displayName = displayName,
        packageName = packageName,
        className = className,
        action = action,
        dataUri = dataUri,
        dataMimeType = dataMimeType,
        extras = extras,
        flagList = flagList,
    )

    override val type: String = "launch_activity"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_launch_activity

    override val description: String
        get() = descriptionWithClassName(className)

    override fun loadIcon(context: Context): Drawable =
        getAppIcon(context, packageName, className) ?: context.getDrawable(iconRes)!!

    override suspend fun execute(context: Context) {
        val intent = Intent()
        intent.setClassName(packageName, className)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        action?.let { intent.action = it }
        val data = dataUri?.toUri()
        if (data != null && dataMimeType != null) {
            intent.setDataAndType(data, dataMimeType)
        } else {
            data?.let { intent.data = it }
            dataMimeType?.let { intent.type = it }
        }

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

    companion object : EntityMetadata {
        override val typeKey = "launch_activity"
        override val entityClass = LaunchActivityCommand::class
        override val labelRes = R.string.command_type_launch_activity
        override val iconRes = R.drawable.ic_apps
        override val editorFactory = { id: String? -> ApplicationCommandEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_launch_activity_help

        fun fromJson(jsonString: String): LaunchActivityCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
    }
}

/**
 * Send a Broadcast to a Receiver.
 *
 * Note: we omit specifying permission because the sender-side
 * permission filter is redundant when targeting a specific receiver class.
 */
@Serializable
data class SendBroadcastCommand(
    override val id: String,
    override val displayName: String,
    override val packageName: String,
    val className: String?,
    val action: String,
    val dataUri: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
) : ApplicationCommand() {
    constructor(
        displayName: String,
        packageName: String,
        className: String?,
        action: String,
        dataUri: String? = null,
        extras: Map<String, ExtraValue> = emptyMap(),
        id: String?,
    ) : this(
        displayName = displayName,
        packageName = packageName,
        className = className,
        action = action,
        dataUri = dataUri,
        extras = extras,
        id = UUID.randomUUID().toString(),
    )

    override val type: String = "send_broadcast"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_send_broadcast

    override val description: String
        get() = className?.let { descriptionWithClassName(it) } ?: "$packageName/$action"

    override fun loadIcon(context: Context): Drawable =
        getAppIcon(context, packageName) ?: context.getDrawable(iconRes)!!

    override suspend fun execute(context: Context) {
        val intent = Intent(action)
        if (className != null) {
            intent.setClassName(packageName, className)
        } else {
            intent.setPackage(packageName)
        }
        dataUri?.let { intent.data = it.toUri() }

        extras.entries.forEach { (key, value) ->
            value.addToIntent(intent, key)
        }

        context.sendBroadcast(intent)
    }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object : EntityMetadata {
        override val typeKey = "send_broadcast"
        override val entityClass = SendBroadcastCommand::class
        override val labelRes = R.string.command_type_send_broadcast
        override val iconRes = R.drawable.ic_apps
        override val editorFactory = { id: String? -> ApplicationCommandEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_send_broadcast_help

        fun fromJson(jsonString: String): SendBroadcastCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
    }
}

/**
 * Start a Service.
 */
@Serializable
data class StartServiceCommand(
    override val id: String,
    override val displayName: String,
    override val packageName: String,
    val className: String,
    val action: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
) : ApplicationCommand() {
    constructor(
        displayName: String,
        packageName: String,
        className: String,
        action: String? = null,
        extras: Map<String, ExtraValue> = emptyMap(),
        id: String?,
    ) : this(
        displayName = displayName,
        packageName = packageName,
        className = className,
        action = action,
        extras = extras,
        id = UUID.randomUUID().toString(),
    )

    override val type: String = "start_service"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_start_service

    override val description: String
        get() = descriptionWithClassName(className)

    override fun loadIcon(context: Context): Drawable =
        getAppIcon(context, packageName, className) ?: context.getDrawable(iconRes)!!

    override suspend fun execute(context: Context) {
        val intent = Intent()
        intent.setClassName(packageName, className)

        action?.let { intent.action = it }
        extras.entries.forEach { (key, value) ->
            value.addToIntent(intent, key)
        }

        if (isForegroundService(context, packageName, className)) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object : EntityMetadata {
        override val typeKey = "start_service"
        override val entityClass = StartServiceCommand::class
        override val labelRes = R.string.command_type_start_service
        override val iconRes = R.drawable.ic_apps
        override val editorFactory = { id: String? -> ApplicationCommandEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_control_service_help

        fun fromJson(jsonString: String): StartServiceCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
    }
}

/**
 * Access a ContentProvider (read/write data).
 */
@Serializable
data class AccessProviderCommand(
    override val id: String,
    override val displayName: String,
    override val packageName: String,
    val authority: String,
    val operation: ProviderOperation,
    val path: String? = null,
    val mimeType: String? = null,
    val extras: Map<String, ExtraValue> = emptyMap(),
) : ApplicationCommand() {
    constructor(
        displayName: String,
        packageName: String,
        authority: String,
        operation: ProviderOperation,
        path: String? = null,
        mimeType: String? = null,
        extras: Map<String, ExtraValue> = emptyMap(),
        id: String?,
    ) : this(
        id = UUID.randomUUID().toString(),
        displayName = displayName,
        packageName = packageName,
        authority = authority,
        operation = operation,
        path = path,
        mimeType = mimeType,
        extras = extras,
    )

    override val type: String = "access_provider"

    @kotlinx.serialization.Transient
    override val typeLabelRes: Int = R.string.command_type_access_provider

    override val description: String
        get() = "$packageName/$authority"

    override fun loadIcon(context: Context): Drawable =
        getAppIcon(context, packageName) ?: context.getDrawable(iconRes)!!

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

    companion object : EntityMetadata {
        override val typeKey = "access_provider"
        override val entityClass = AccessProviderCommand::class
        override val labelRes = R.string.command_type_access_provider
        override val iconRes = R.drawable.ic_apps
        override val editorFactory = { _: String? -> NotYetImplementedEditor }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.command_access_provider_help

        fun fromJson(jsonString: String): AccessProviderCommand = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)
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

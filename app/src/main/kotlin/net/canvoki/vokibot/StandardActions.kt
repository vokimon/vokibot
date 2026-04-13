package net.canvoki.vokibot

import android.content.Intent
import android.provider.MediaStore
import android.speech.RecognizerIntent

enum class ExtraType {
    STRING,
    URI,
    INT,
    BOOLEAN,
    STRING_ARRAY,
    URI_LIST,
}

data class ExtraSpec(
    val key: String,
    val type: ExtraType,
    val required: Boolean = false,
    val label: String = key,
)

/**
 * Strategy for probing whether a component accepts an action.
 * Describes how to construct the test intent for resolution checks.
 */
enum class ProbeStrategy {
    /** Action works with just the action string (e.g., MAIN, SEND) */
    ACTION_ONLY,

    /** Action typically requires a data URI to match filters (e.g., VIEW, DIAL) */
    REQUIRES_URI,

    /** Action typically requires specific extras to match filters (e.g., IMAGE_CAPTURE) */
    REQUIRES_EXTRAS,
}

data class ActionDefinition(
    val action: String,
    val label: String,
    val iconRes: Int,
    val probeStrategy: ProbeStrategy = ProbeStrategy.ACTION_ONLY,
    val extras: List<ExtraSpec> = emptyList(),
)

object StandardActions {
    private val registry: Map<String, ActionDefinition> =
        listOf(
            ActionDefinition(
                action = Intent.ACTION_MAIN,
                label = "Main",
                iconRes = R.drawable.ic_apps,
            ),
            ActionDefinition(
                action = Intent.ACTION_VIEW,
                label = "View",
                iconRes = R.drawable.ic_visibility,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                extras =
                    listOf(
                        ExtraSpec(
                            key = "data",
                            type = ExtraType.URI,
                            required = true,
                            label = "URI",
                        ),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_SEND,
                label = "Send",
                iconRes = R.drawable.ic_send,
                extras =
                    listOf(
                        ExtraSpec(Intent.EXTRA_TEXT, ExtraType.STRING, label = "Text"),
                        ExtraSpec(Intent.EXTRA_SUBJECT, ExtraType.STRING, label = "Subject"),
                        ExtraSpec(Intent.EXTRA_STREAM, ExtraType.URI, label = "Attachment"),
                        ExtraSpec(Intent.EXTRA_EMAIL, ExtraType.STRING_ARRAY, label = "To"),
                        ExtraSpec(Intent.EXTRA_CC, ExtraType.STRING_ARRAY, label = "CC"),
                        ExtraSpec(Intent.EXTRA_BCC, ExtraType.STRING_ARRAY, label = "BCC"),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_SEND_MULTIPLE,
                label = "Send Multiple",
                iconRes = R.drawable.ic_send,
                extras =
                    listOf(
                        ExtraSpec(
                            key = Intent.EXTRA_STREAM,
                            type = ExtraType.URI_LIST,
                            required = true,
                            label = "Attachments",
                        ),
                        ExtraSpec(
                            key = Intent.EXTRA_TEXT,
                            type = ExtraType.STRING,
                            label = "Text",
                        ),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_SENDTO,
                label = "Send To",
                iconRes = R.drawable.ic_mail,
                extras =
                    listOf(
                        ExtraSpec(Intent.EXTRA_SUBJECT, ExtraType.STRING, label = "Subject"),
                        ExtraSpec(Intent.EXTRA_TEXT, ExtraType.STRING, label = "Body"),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_DIAL,
                label = "Dial",
                iconRes = R.drawable.ic_call,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                extras =
                    listOf(
                        ExtraSpec(
                            key = "data",
                            type = ExtraType.URI,
                            required = true,
                            label = "Phone number",
                        ),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_CALL,
                label = "Call",
                iconRes = R.drawable.ic_phone,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                extras =
                    listOf(
                        ExtraSpec(
                            key = "data",
                            type = ExtraType.URI,
                            required = true,
                            label = "Phone number",
                        ),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_EDIT,
                label = "Edit",
                iconRes = R.drawable.ic_edit,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
            ),
            ActionDefinition(
                action = Intent.ACTION_PICK,
                label = "Pick",
                iconRes = R.drawable.ic_photo_library,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                extras =
                    listOf(
                        ExtraSpec(
                            key = "data",
                            type = ExtraType.URI,
                            required = true,
                            label = "Content URI",
                        ),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_GET_CONTENT,
                label = "Get Content",
                iconRes = R.drawable.ic_folder,
            ),
            ActionDefinition(
                action = MediaStore.ACTION_IMAGE_CAPTURE,
                label = "Take Photo",
                iconRes = R.drawable.ic_photo_camera,
                probeStrategy = ProbeStrategy.REQUIRES_EXTRAS,
                extras =
                    listOf(
                        ExtraSpec(
                            key = MediaStore.EXTRA_OUTPUT,
                            type = ExtraType.URI,
                            required = true,
                            label = "Output URI",
                        ),
                    ),
            ),
            ActionDefinition(
                action = MediaStore.ACTION_VIDEO_CAPTURE,
                label = "Record Video",
                iconRes = R.drawable.ic_videocam,
                probeStrategy = ProbeStrategy.REQUIRES_EXTRAS,
                extras =
                    listOf(
                        ExtraSpec(
                            key = MediaStore.EXTRA_OUTPUT,
                            type = ExtraType.URI,
                            label = "Output URI",
                        ),
                        ExtraSpec(
                            key = MediaStore.EXTRA_DURATION_LIMIT,
                            type = ExtraType.INT,
                            label = "Max duration (seconds)",
                        ),
                        ExtraSpec(
                            key = MediaStore.EXTRA_VIDEO_QUALITY,
                            type = ExtraType.INT,
                            label = "Quality (0 low / 1 high)",
                        ),
                    ),
            ),
            ActionDefinition(
                action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH,
                label = "Speech Recognition",
                iconRes = R.drawable.ic_mic,
                probeStrategy = ProbeStrategy.REQUIRES_EXTRAS,
                extras =
                    listOf(
                        ExtraSpec(
                            key = RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            type = ExtraType.STRING,
                            required = true,
                            label = "Language model",
                        ),
                        ExtraSpec(
                            key = RecognizerIntent.EXTRA_PROMPT,
                            type = ExtraType.STRING,
                            label = "Prompt",
                        ),
                        ExtraSpec(
                            key = RecognizerIntent.EXTRA_MAX_RESULTS,
                            type = ExtraType.INT,
                            label = "Max results",
                        ),
                    ),
            ),
        ).associateBy { it.action }

    fun all(): List<ActionDefinition> = registry.values.toList()

    fun get(action: String?): ActionDefinition? = action?.let { registry[it] }

    fun icon(action: String?): Int = get(action)?.iconRes ?: R.drawable.ic_brand

    fun label(action: String?): String? = get(action)?.label

    fun extras(action: String?): List<ExtraSpec> = get(action)?.extras ?: emptyList()

    fun probeStrategy(action: String?): ProbeStrategy = get(action)?.probeStrategy ?: ProbeStrategy.ACTION_ONLY
}

package net.canvoki.vokibot.apps

import android.content.Intent
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.annotation.StringRes
import net.canvoki.vokibot.ExtraType
import net.canvoki.vokibot.R

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
    @get:StringRes val labelRes: Int,
    val iconRes: Int,
    val probeStrategy: ProbeStrategy = ProbeStrategy.ACTION_ONLY,
    val extras: List<ExtraSpec> = emptyList(),
    val allowedSchemes: List<String>? = null,
)

object StandardActions {
    private val registry: Map<String, ActionDefinition> =
        listOf(
            ActionDefinition(
                action = Intent.ACTION_MAIN,
                labelRes = R.string.standard_action_main,
                iconRes = R.drawable.ic_apps,
            ),
            ActionDefinition(
                action = Intent.ACTION_VIEW,
                labelRes = R.string.standard_action_view,
                iconRes = R.drawable.ic_visibility,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
            ),
            ActionDefinition(
                action = Intent.ACTION_SEND,
                labelRes = R.string.standard_action_send,
                iconRes = R.drawable.ic_send,
                extras =
                    listOf(
                        ExtraSpec(Intent.EXTRA_TEXT, ExtraType.String, labelRes = R.string.standard_extra_text),
                        ExtraSpec(Intent.EXTRA_SUBJECT, ExtraType.String, labelRes = R.string.standard_extra_subject),
                        ExtraSpec(Intent.EXTRA_STREAM, ExtraType.Uri, labelRes = R.string.standard_extra_attachment),
                        ExtraSpec(Intent.EXTRA_EMAIL, ExtraType.StringArray, labelRes = R.string.standard_extra_to),
                        ExtraSpec(Intent.EXTRA_CC, ExtraType.StringArray, labelRes = R.string.standard_extra_cc),
                        ExtraSpec(Intent.EXTRA_BCC, ExtraType.StringArray, labelRes = R.string.standard_extra_bcc),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_SEND_MULTIPLE,
                labelRes = R.string.standard_action_send_multiple,
                iconRes = R.drawable.ic_send,
                extras =
                    listOf(
                        ExtraSpec(
                            key = Intent.EXTRA_STREAM,
                            type = ExtraType.UriList,
                            required = true,
                            labelRes = R.string.standard_extra_attachments,
                        ),
                        ExtraSpec(
                            key = Intent.EXTRA_TEXT,
                            type = ExtraType.String,
                            labelRes = R.string.standard_extra_text,
                        ),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_SENDTO,
                labelRes = R.string.standard_action_send_to,
                iconRes = R.drawable.ic_mail,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                allowedSchemes = listOf("mailto", "smsto"),
                extras =
                    listOf(
                        ExtraSpec(Intent.EXTRA_SUBJECT, ExtraType.String, labelRes = R.string.standard_extra_subject),
                        ExtraSpec(Intent.EXTRA_TEXT, ExtraType.String, labelRes = R.string.standard_extra_body),
                    ),
            ),
            ActionDefinition(
                action = Intent.ACTION_DIAL,
                labelRes = R.string.standard_action_dial,
                iconRes = R.drawable.ic_call,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                allowedSchemes = listOf("tel"),
            ),
            ActionDefinition(
                action = Intent.ACTION_CALL,
                labelRes = R.string.standard_action_call,
                iconRes = R.drawable.ic_phone,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                allowedSchemes = listOf("tel"),
            ),
            ActionDefinition(
                action = Intent.ACTION_EDIT,
                labelRes = R.string.standard_action_edit,
                iconRes = R.drawable.ic_edit,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                allowedSchemes = listOf("content"),
            ),
            ActionDefinition(
                action = Intent.ACTION_PICK,
                labelRes = R.string.standard_action_pick,
                iconRes = R.drawable.ic_photo_library,
                probeStrategy = ProbeStrategy.REQUIRES_URI,
                allowedSchemes = listOf("content"),
            ),
            ActionDefinition(
                action = Intent.ACTION_GET_CONTENT,
                labelRes = R.string.standard_action_get_content,
                iconRes = R.drawable.ic_folder,
            ),
            ActionDefinition(
                action = MediaStore.ACTION_IMAGE_CAPTURE,
                labelRes = R.string.standard_action_take_photo,
                iconRes = R.drawable.ic_photo_camera,
                probeStrategy = ProbeStrategy.REQUIRES_EXTRAS,
                extras =
                    listOf(
                        ExtraSpec(
                            key = MediaStore.EXTRA_OUTPUT,
                            type = ExtraType.Uri,
                            required = true,
                            labelRes = R.string.standard_extra_output_uri,
                        ),
                    ),
            ),
            ActionDefinition(
                action = MediaStore.ACTION_VIDEO_CAPTURE,
                labelRes = R.string.standard_action_record_video,
                iconRes = R.drawable.ic_videocam,
                probeStrategy = ProbeStrategy.REQUIRES_EXTRAS,
                extras =
                    listOf(
                        ExtraSpec(
                            key = MediaStore.EXTRA_OUTPUT,
                            type = ExtraType.Uri,
                            labelRes = R.string.standard_extra_output_uri,
                        ),
                        ExtraSpec(
                            key = MediaStore.EXTRA_DURATION_LIMIT,
                            type = ExtraType.Int(),
                            labelRes = R.string.standard_extra_max_duration,
                        ),
                        ExtraSpec(
                            key = MediaStore.EXTRA_VIDEO_QUALITY,
                            type = ExtraType.Int(),
                            labelRes = R.string.standard_extra_quality,
                        ),
                    ),
            ),
            ActionDefinition(
                action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH,
                labelRes = R.string.standard_action_speech_recognition,
                iconRes = R.drawable.ic_mic,
                probeStrategy = ProbeStrategy.REQUIRES_EXTRAS,
                extras =
                    listOf(
                        ExtraSpec(
                            key = RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            type = ExtraType.String,
                            required = true,
                            labelRes = R.string.standard_extra_language_model,
                        ),
                        ExtraSpec(
                            key = RecognizerIntent.EXTRA_PROMPT,
                            type = ExtraType.String,
                            labelRes = R.string.standard_extra_prompt,
                        ),
                        ExtraSpec(
                            key = RecognizerIntent.EXTRA_MAX_RESULTS,
                            type = ExtraType.Int(),
                            labelRes = R.string.standard_extra_max_results,
                        ),
                    ),
            ),
        ).associateBy { it.action }

    fun all(): List<ActionDefinition> = registry.values.toList()

    fun get(action: String?): ActionDefinition? = action?.let { registry[it] }

    fun icon(action: String?): Int = get(action)?.iconRes ?: R.drawable.ic_brand

    fun extras(action: String?): List<ExtraSpec> = get(action)?.extras ?: emptyList()

    fun probeStrategy(action: String?): ProbeStrategy = get(action)?.probeStrategy ?: ProbeStrategy.ACTION_ONLY

    fun allowedSchemes(action: String?): List<String>? = get(action)?.allowedSchemes
}

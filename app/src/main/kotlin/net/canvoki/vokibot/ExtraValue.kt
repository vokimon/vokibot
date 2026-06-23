package net.canvoki.vokibot

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.common.UriField

@Serializable
sealed class ExtraType {
    override fun toString(): kotlin.String = this::class.simpleName!!

    @Serializable
    @SerialName("STRING")
    object String : ExtraType()

    @Serializable
    @SerialName("URI")
    object Uri : ExtraType()

    @Serializable
    @SerialName("INT")
    object Int : ExtraType()

    @Serializable
    @SerialName("BOOLEAN")
    object Boolean : ExtraType()

    @Serializable
    @SerialName("STRING_ARRAY")
    object StringArray : ExtraType()

    @Serializable
    @SerialName("URI_LIST")
    object UriList : ExtraType()

    companion object {
        val intentExtraTypes: List<ExtraType> = listOf(String, Uri, Int, Boolean, StringArray, UriList)
    }
}

val ExtraType.labelRes get() =
    when (this) {
        ExtraType.String -> R.string.extra_value_type_text
        ExtraType.Uri -> R.string.extra_value_type_uri
        ExtraType.Int -> R.string.extra_value_type_number
        ExtraType.Boolean -> R.string.extra_value_type_boolean
        ExtraType.StringArray -> R.string.extra_value_type_text_list
        ExtraType.UriList -> R.string.extra_value_type_uri_list
    }

@Serializable
data class ExtraSpec(
    val key: String,
    val type: ExtraType,
    val required: Boolean = false,
    @get:StringRes val labelRes: Int = 0,
)

@Composable
fun ExtraSpec.displayLabel(): String = if (labelRes != 0) stringResource(labelRes) else key

fun ExtraSpec.defaultValue(): ExtraValue =
    when (type) {
        ExtraType.String -> ExtraValue.StringValue("")
        ExtraType.Uri -> ExtraValue.UriValue("")
        ExtraType.Int -> ExtraValue.IntValue(0)
        ExtraType.Boolean -> ExtraValue.BooleanValue(false)
        ExtraType.StringArray -> ExtraValue.StringArrayValue(emptyList())
        ExtraType.UriList -> ExtraValue.UriListValue(emptyList())
    }

/**
 * Type-safe extra values for Intents.
 */
@Serializable
sealed class ExtraValue {
    abstract fun addToIntent(
        intent: Intent,
        key: String,
    )

    abstract fun toStoredSettingValue(): String

    @Composable
    open fun Editor(
        spec: ExtraSpec,
        onChanged: (ExtraValue) -> Unit,
    ) {
    }

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

        override fun toStoredSettingValue(): String = value

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            var text by remember { mutableStateOf(value) }
            LaunchedEffect(value) { text = value }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onChanged(copy(value = it))
                },
                label = { Text(spec.displayLabel()) },
                modifier = Modifier.fillMaxWidth(),
            )
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

        override fun toStoredSettingValue(): String = value.toString()

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            var text by remember { mutableStateOf(value.toString()) }
            LaunchedEffect(value) { text = value.toString() }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toIntOrNull()?.let { v -> onChanged(copy(value = v)) }
                },
                label = { Text(spec.displayLabel()) },
                modifier = Modifier.fillMaxWidth(),
            )
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

        override fun toStoredSettingValue(): String = value.toString()

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            var text by remember { mutableStateOf(value.toString()) }
            LaunchedEffect(value) { text = value.toString() }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toLongOrNull()?.let { v -> onChanged(copy(value = v)) }
                },
                label = { Text(spec.displayLabel()) },
                modifier = Modifier.fillMaxWidth(),
            )
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

        override fun toStoredSettingValue(): String = if (value) "1" else "0"

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            Row {
                Text(spec.displayLabel(), modifier = Modifier.weight(1f))
                Switch(checked = value, onCheckedChange = { onChanged(copy(value = it)) })
            }
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

        override fun toStoredSettingValue(): String = value.toString()

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            var text by remember { mutableStateOf(value.toString()) }
            LaunchedEffect(value) { text = value.toString() }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toFloatOrNull()?.let { v -> onChanged(copy(value = v)) }
                },
                label = { Text(spec.displayLabel()) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Serializable
    @SerialName("uri")
    data class UriValue(
        val value: String,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, value.toUri())
        }

        override fun toStoredSettingValue(): String = value

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            UriField(
                uri = value.ifBlank { null },
                onUriChanged = { onChanged(copy(value = it.orEmpty())) },
                label = spec.displayLabel(),
            )
        }
    }

    @Serializable
    @SerialName("string_array")
    data class StringArrayValue(
        val values: List<String>,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putExtra(key, values.toTypedArray())
        }

        override fun toStoredSettingValue(): String = values.joinToString(",")

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            var text by remember { mutableStateOf(values.joinToString(", ")) }
            LaunchedEffect(values) { text = values.joinToString(", ") }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onChanged(copy(values = it.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                },
                label = {
                    Text(
                        "${spec.displayLabel()} ${stringResource(R.string.intent_extras_editor_comma_separated)}",
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Serializable
    @SerialName("uri_list")
    data class UriListValue(
        val values: List<String>,
    ) : ExtraValue() {
        override fun addToIntent(
            intent: Intent,
            key: String,
        ) {
            intent.putParcelableArrayListExtra(key, ArrayList(values.map { it.toUri() }))
        }

        override fun toStoredSettingValue(): String = values.joinToString(",")

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            var text by remember { mutableStateOf(values.joinToString(", ")) }
            LaunchedEffect(values) { text = values.joinToString(", ") }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onChanged(copy(values = it.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                },
                label = {
                    Text(
                        "${spec.displayLabel()} ${stringResource(R.string.intent_extras_editor_comma_separated)}",
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

fun ExtraValue.isDefault(): Boolean =
    when (this) {
        is ExtraValue.StringValue -> value.isEmpty()
        is ExtraValue.IntValue -> value == 0
        is ExtraValue.LongValue -> value == 0L
        is ExtraValue.BooleanValue -> !value
        is ExtraValue.FloatValue -> value == 0f
        is ExtraValue.UriValue -> value.isEmpty()
        is ExtraValue.StringArrayValue -> values.isEmpty()
        is ExtraValue.UriListValue -> values.isEmpty()
    }

fun ExtraValue.toExtraType(): ExtraType =
    when (this) {
        is ExtraValue.StringValue -> ExtraType.String
        is ExtraValue.IntValue, is ExtraValue.LongValue -> ExtraType.Int
        is ExtraValue.BooleanValue -> ExtraType.Boolean
        is ExtraValue.FloatValue -> ExtraType.String
        is ExtraValue.UriValue -> ExtraType.Uri
        is ExtraValue.StringArrayValue -> ExtraType.StringArray
        is ExtraValue.UriListValue -> ExtraType.UriList
    }

fun computeNewCustomSpecs(
    extrasState: Map<String, ExtraValue>,
    newActionExtras: List<ExtraSpec>,
): List<ExtraSpec> {
    val actionKeys = newActionExtras.map { it.key }.toSet()
    return extrasState
        .filterKeys { it !in actionKeys }
        .filterValues { !it.isDefault() }
        .map { (k, v) -> ExtraSpec(k, v.toExtraType()) }
}

fun rebuildExtras(
    values: Map<String, ExtraValue>,
    actionSpecs: List<ExtraSpec>,
    customSpecs: List<ExtraSpec>,
): Map<String, ExtraValue> =
    (actionSpecs + customSpecs)
        .map { spec ->
            val existing = values[spec.key]
            spec.key to if (existing != null && existing.toExtraType() == spec.type) existing else spec.defaultValue()
        }.toMap()

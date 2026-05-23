package net.canvoki.vokibot

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ExtraType {
    STRING,
    URI,
    INT,
    BOOLEAN,
    STRING_ARRAY,
    URI_LIST,
}

val ExtraType.displayName: String get() =
    when (this) {
        ExtraType.STRING -> "Text"
        ExtraType.URI -> "URI"
        ExtraType.INT -> "Number"
        ExtraType.BOOLEAN -> "Boolean"
        ExtraType.STRING_ARRAY -> "Text list"
        ExtraType.URI_LIST -> "URI list"
    }

@Serializable
data class ExtraSpec(
    val key: String,
    val type: ExtraType,
    val required: Boolean = false,
    val label: String = key,
)

fun ExtraSpec.defaultValue(): ExtraValue =
    when (type) {
        ExtraType.STRING -> ExtraValue.StringValue("")
        ExtraType.URI -> ExtraValue.UriValue("")
        ExtraType.INT -> ExtraValue.IntValue(0)
        ExtraType.BOOLEAN -> ExtraValue.BooleanValue(false)
        ExtraType.STRING_ARRAY -> ExtraValue.StringArrayValue(emptyList())
        ExtraType.URI_LIST -> ExtraValue.UriListValue(emptyList())
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
                label = { Text(spec.label) },
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
                label = { Text(spec.label) },
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
                label = { Text(spec.label) },
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

        @Composable
        override fun Editor(
            spec: ExtraSpec,
            onChanged: (ExtraValue) -> Unit,
        ) {
            Row {
                Text(spec.label, modifier = Modifier.weight(1f))
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
                label = { Text(spec.label) },
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
                label = { Text(spec.label) },
                modifier = Modifier.fillMaxWidth(),
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
                label = { Text("${spec.label} (comma separated)") },
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
                label = { Text("${spec.label} (comma separated)") },
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
        is ExtraValue.StringValue -> ExtraType.STRING
        is ExtraValue.IntValue, is ExtraValue.LongValue -> ExtraType.INT
        is ExtraValue.BooleanValue -> ExtraType.BOOLEAN
        is ExtraValue.FloatValue -> ExtraType.STRING
        is ExtraValue.UriValue -> ExtraType.URI
        is ExtraValue.StringArrayValue -> ExtraType.STRING_ARRAY
        is ExtraValue.UriListValue -> ExtraType.URI_LIST
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
        .map {
            it.key to (values[it.key] ?: it.defaultValue())
        }.toMap()

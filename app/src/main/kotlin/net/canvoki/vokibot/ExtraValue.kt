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
import net.canvoki.vokibot.common.EnumField
import net.canvoki.vokibot.common.EnumOption
import net.canvoki.vokibot.common.FlagField
import net.canvoki.vokibot.common.FlagOption
import net.canvoki.vokibot.common.FlagSerialization
import net.canvoki.vokibot.common.UriField

@Serializable
sealed class ExtraType {
    override fun toString(): kotlin.String = this::class.simpleName!!

    @get:StringRes abstract val labelRes: kotlin.Int

    abstract fun defaultValue(): ExtraValue

    abstract fun fromStoredSetting(raw: kotlin.String): ExtraValue

    abstract fun toStoredSetting(value: ExtraValue): kotlin.String

    @Composable
    abstract fun Editor(
        label: kotlin.String,
        value: ExtraValue,
        onChanged: (ExtraValue) -> Unit,
    )

    @Serializable
    @SerialName("STRING")
    object String : ExtraType() {
        override val labelRes = R.string.extra_value_type_text

        override fun defaultValue() = ExtraValue.StringValue("")

        override fun fromStoredSetting(raw: kotlin.String) = ExtraValue.StringValue(raw)

        override fun toStoredSetting(value: ExtraValue): kotlin.String = (value as? ExtraValue.StringValue)?.value ?: ""

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val textValue = (value as? ExtraValue.StringValue)?.value ?: ""
            var text by remember { mutableStateOf(textValue) }
            LaunchedEffect(textValue) { text = textValue }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onChanged(ExtraValue.StringValue(it))
                },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Serializable
    @SerialName("URI")
    object Uri : ExtraType() {
        override val labelRes = R.string.extra_value_type_uri

        override fun defaultValue() = ExtraValue.UriValue("")

        override fun fromStoredSetting(raw: kotlin.String) = ExtraValue.UriValue(raw)

        override fun toStoredSetting(value: ExtraValue): kotlin.String = (value as? ExtraValue.UriValue)?.value ?: ""

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val uriValue = (value as? ExtraValue.UriValue)?.value ?: ""
            UriField(
                uri = uriValue.ifBlank { null },
                onUriChanged = { onChanged(ExtraValue.UriValue(it.orEmpty())) },
                label = label,
            )
        }
    }

    @Serializable
    @SerialName("INT")
    object Int : ExtraType() {
        override val labelRes = R.string.extra_value_type_number

        override fun defaultValue() = ExtraValue.IntValue(0)

        override fun fromStoredSetting(raw: kotlin.String) = ExtraValue.IntValue(raw.toIntOrNull() ?: 0)

        override fun toStoredSetting(value: ExtraValue): kotlin.String =
            (value as? ExtraValue.IntValue)?.value?.toString() ?: "0"

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val intValue = (value as? ExtraValue.IntValue)?.value ?: 0
            var text by remember { mutableStateOf(intValue.toString()) }
            LaunchedEffect(intValue) { text = intValue.toString() }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    it.toIntOrNull()?.let { v -> onChanged(ExtraValue.IntValue(v)) }
                },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Serializable
    @SerialName("BOOLEAN")
    object Boolean : ExtraType() {
        override val labelRes = R.string.extra_value_type_boolean

        override fun defaultValue() = ExtraValue.BooleanValue(false)

        override fun fromStoredSetting(raw: kotlin.String) = ExtraValue.BooleanValue(raw == "1")

        override fun toStoredSetting(value: ExtraValue): kotlin.String =
            if ((value as? ExtraValue.BooleanValue)?.value == true) "1" else "0"

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val boolValue = (value as? ExtraValue.BooleanValue)?.value ?: false
            Row {
                Text(label, modifier = Modifier.weight(1f))
                Switch(checked = boolValue, onCheckedChange = { onChanged(ExtraValue.BooleanValue(it)) })
            }
        }
    }

    @Serializable
    @SerialName("STRING_ARRAY")
    object StringArray : ExtraType() {
        override val labelRes = R.string.extra_value_type_text_list

        override fun defaultValue() = ExtraValue.StringArrayValue(emptyList())

        override fun fromStoredSetting(raw: kotlin.String) =
            ExtraValue.StringArrayValue(raw.split(",").map { it.trim() })

        override fun toStoredSetting(value: ExtraValue): kotlin.String =
            (value as? ExtraValue.StringArrayValue)?.values?.joinToString(",") ?: ""

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val arrayValue = (value as? ExtraValue.StringArrayValue)?.values ?: emptyList()
            var text by remember { mutableStateOf(arrayValue.joinToString(", ")) }
            LaunchedEffect(arrayValue) { text = arrayValue.joinToString(", ") }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onChanged(ExtraValue.StringArrayValue(it.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                },
                label = {
                    Text("$label ${stringResource(R.string.intent_extras_editor_comma_separated)}")
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Serializable
    @SerialName("URI_LIST")
    object UriList : ExtraType() {
        override val labelRes = R.string.extra_value_type_uri_list

        override fun defaultValue() = ExtraValue.UriListValue(emptyList())

        override fun fromStoredSetting(raw: kotlin.String) = ExtraValue.UriListValue(raw.split(",").map { it.trim() })

        override fun toStoredSetting(value: ExtraValue): kotlin.String =
            (value as? ExtraValue.UriListValue)?.values?.joinToString(",") ?: ""

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val listValue = (value as? ExtraValue.UriListValue)?.values ?: emptyList()
            var text by remember { mutableStateOf(listValue.joinToString(", ")) }
            LaunchedEffect(listValue) { text = listValue.joinToString(", ") }
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onChanged(ExtraValue.UriListValue(it.split(",").map { it.trim() }.filter { it.isNotEmpty() }))
                },
                label = {
                    Text("$label ${stringResource(R.string.intent_extras_editor_comma_separated)}")
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    @Serializable
    @SerialName("ENUM")
    data class Enum(
        val options: List<EnumOption>,
    ) : ExtraType() {
        override val labelRes = R.string.extra_value_type_enum

        override fun defaultValue() =
            ExtraValue.StringValue(
                options.firstOrNull()?.value ?: "",
            )

        override fun fromStoredSetting(raw: kotlin.String) = ExtraValue.StringValue(raw)

        override fun toStoredSetting(value: ExtraValue): kotlin.String = (value as? ExtraValue.StringValue)?.value ?: ""

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val rawValue = (value as? ExtraValue.StringValue)?.value ?: ""
            EnumField(
                options = options,
                selectedValue = rawValue,
                onValueChanged = { onChanged(ExtraValue.StringValue(it)) },
                label = label,
            )
        }
    }

    @Serializable
    @SerialName("FLAG")
    data class Flags(
        val options: List<FlagOption>,
    ) : ExtraType() {
        override val labelRes = R.string.extra_value_type_flags

        override fun defaultValue() = ExtraValue.StringArrayValue(emptyList())

        override fun fromStoredSetting(raw: kotlin.String) =
            ExtraValue.StringArrayValue(
                FlagSerialization.BitMask.fromString(raw, options),
            )

        override fun toStoredSetting(value: ExtraValue): kotlin.String {
            val values = (value as? ExtraValue.StringArrayValue)?.values ?: emptyList()
            return FlagSerialization.BitMask.toString(values, options)
        }

        @Composable
        override fun Editor(
            label: kotlin.String,
            value: ExtraValue,
            onChanged: (ExtraValue) -> Unit,
        ) {
            val serial = FlagSerialization.BitMask
            val selection = (value as? ExtraValue.StringArrayValue)?.values ?: emptyList<kotlin.String>()
            FlagField(
                label = label,
                options = options,
                selection = selection,
                onSelectionChanged = { newSelection ->
                    onChanged(ExtraValue.StringArrayValue(newSelection))
                },
            )
        }
    }

    companion object {
        val intentExtraTypes by lazy {
            listOf<ExtraType>(String, Uri, Int, Boolean, StringArray, UriList)
        }
    }
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

fun ExtraSpec.defaultValue(): ExtraValue = type.defaultValue()

/**
 * Type-safe extra values for Intents.
 */
@Serializable
sealed class ExtraValue {
    abstract fun addToIntent(
        intent: Intent,
        key: String,
    )

    abstract fun isDefault(): Boolean

    abstract fun getExtraType(): ExtraType

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

        override fun isDefault(): Boolean = value.isEmpty()

        override fun getExtraType() = ExtraType.String
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

        override fun isDefault(): Boolean = value == 0

        override fun getExtraType() = ExtraType.Int
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

        override fun isDefault(): Boolean = value == false

        override fun getExtraType() = ExtraType.Boolean
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

        override fun isDefault(): Boolean = value.isEmpty()

        override fun getExtraType() = ExtraType.Uri
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

        override fun isDefault(): Boolean = values.isEmpty()

        override fun getExtraType() = ExtraType.StringArray
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

        override fun isDefault(): Boolean = values.isEmpty()

        override fun getExtraType() = ExtraType.UriList
    }
}

fun computeNewCustomSpecs(
    extrasState: Map<String, ExtraValue>,
    newActionExtras: List<ExtraSpec>,
): List<ExtraSpec> {
    val actionKeys = newActionExtras.map { it.key }.toSet()
    return extrasState
        .filterKeys { it !in actionKeys }
        .filterValues { !it.isDefault() }
        .map { (k, v) -> ExtraSpec(k, v.getExtraType()) }
}

fun rebuildExtras(
    values: Map<String, ExtraValue>,
    actionSpecs: List<ExtraSpec>,
    customSpecs: List<ExtraSpec>,
): Map<String, ExtraValue> =
    (actionSpecs + customSpecs)
        .map { spec ->
            val existing = values[spec.key]
            spec.key to if (existing != null && existing.getExtraType() == spec.type) existing else spec.defaultValue()
        }.toMap()

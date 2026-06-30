package net.canvoki.vokibot.apps

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.ExtraType
import net.canvoki.vokibot.ExtraValue

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

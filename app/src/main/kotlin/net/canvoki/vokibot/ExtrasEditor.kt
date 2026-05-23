package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.serializer

val ExtraValueMapSaver: Saver<Map<String, ExtraValue>, String> =
    Saver(
        save = { JsonConfig.encodeToString(MapSerializer(serializer(), serializer()), it) },
        restore = { JsonConfig.decodeFromString(MapSerializer(serializer(), serializer()), it) },
    )

val ExtraSpecListSaver: Saver<List<ExtraSpec>, String> =
    Saver(
        save = { JsonConfig.encodeToString(it) },
        restore = { JsonConfig.decodeFromString(it) },
    )

@Composable
fun ExtrasSection(
    specs: List<ExtraSpec>,
    extras: Map<String, ExtraValue>,
    onExtraChanged: (key: String, value: ExtraValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        specs.forEach { spec ->
            val value = extras[spec.key] ?: spec.defaultValue()
            value.Editor(
                spec = spec,
                onChanged = { newValue -> onExtraChanged(spec.key, newValue) },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

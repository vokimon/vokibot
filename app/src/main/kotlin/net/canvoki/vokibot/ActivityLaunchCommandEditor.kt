package net.canvoki.vokibot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

@Serializable
data object ActivityLaunchCommandEditor : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        var packageName by remember { mutableStateOf<String?>(null) }
        var componentName by remember { mutableStateOf<String?>(null) }
        var selectedAction by remember { mutableStateOf<ActionDefinition?>(null) }
        var customAction by remember { mutableStateOf("") }
        var currentComponent by remember { mutableStateOf<PublicComponent?>(null) }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        LaunchedEffect(packageName, componentName) {
            if (packageName != null && componentName != null) {
                currentComponent =
                    queryPublicComponents(context, packageName!!)
                        .components
                        .find { it.name == componentName }
            }
        }

        val actionsToShow =
            remember(currentComponent?.actions) {
                val mapped =
                    currentComponent?.let {
                        it.actions.mapNotNull { actionStr ->
                            StandardActions.all().find { it.action == actionStr }
                        }
                    } ?: emptyList<ActionDefinition>()

                if (mapped.isNotEmpty()) mapped else StandardActions.all()
            }

        var extrasState by rememberSaveable(stateSaver = ExtraValueMapSaver) {
            mutableStateOf(emptyMap<String, ExtraValue>())
        }
        var customExtraSpecs by rememberSaveable(stateSaver = ExtraSpecListSaver) {
            mutableStateOf(listOf<ExtraSpec>())
        }
        var intentData by remember { mutableStateOf<String?>(null) }
        var intentMime by remember { mutableStateOf<String?>(null) }

        val allSpecs = (selectedAction?.extras ?: emptyList()) + customExtraSpecs

        LaunchedEffect(selectedAction) {
            val actionExtras = selectedAction?.extras ?: emptyList()
            customExtraSpecs = computeNewCustomSpecs(extrasState, actionExtras)
            extrasState = rebuildExtras(extrasState, actionExtras, customExtraSpecs)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            EditorHeader(
                icon = painterResource(LaunchActivityCommand.iconRes),
                title = stringResource(LaunchActivityCommand.labelRes),
                actionText = stringResource(R.string.automation_done),
                action = { nav.pop() },
            )

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val component = currentComponent
                if (component != null) {
                    ActivityHeader(
                        packageName = packageName!!,
                        component = component,
                    )
                } else {
                    Button(
                        onClick = {
                            // TODO: push AppList and take it from there
                            packageName = "net.canvoki.carburoid"
                            componentName = "net.canvoki.carburoid.MainActivity"
                        },
                    ) {
                        Icon(painterResource(R.drawable.ic_add), null)
                        Text(stringResource(R.string.intent_editor_select_app_and_screen))
                    }
                }
                IntentActionSelector(
                    supportedActions = actionsToShow,
                    onSelected = { selectedAction = it },
                    onCustomChanged = { customAction = it },
                )
                IntentDataEditor(
                    dataUri = intentData,
                    mimeType = intentMime,
                    onDataChanged = { intentData = it },
                    onMimeChanged = { intentMime = it },
                )
                ExtrasEditor(
                    specs = allSpecs,
                    extras = extrasState,
                    onExtraChanged = { key, value -> extrasState = extrasState + (key to value) },
                    onAddExtra = { spec ->
                        customExtraSpecs = customExtraSpecs + spec
                        extrasState = extrasState + (spec.key to spec.defaultValue())
                    },
                )
            }
        }
    }
}

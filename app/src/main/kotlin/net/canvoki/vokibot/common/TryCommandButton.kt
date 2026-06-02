package net.canvoki.vokibot.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.canvoki.shared.usermessage.UserMessage
import net.canvoki.vokibot.Command
import net.canvoki.vokibot.R

@Composable
fun TryCommandButton(
    enabled: Boolean,
    buildCommand: () -> Command,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    OutlinedButton(
        onClick = {
            scope.launch {
                val command = buildCommand()
                try {
                    command.execute(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                    UserMessage
                        .Info(
                            e.message ?: context.getString(R.string.command_run_error_fallback),
                        ).post()
                }
            }
        },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play_arrow),
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.intent_editor_try))
    }
}

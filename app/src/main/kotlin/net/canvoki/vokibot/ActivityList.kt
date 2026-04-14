package net.canvoki.vokibot

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import net.canvoki.shared.component.AsyncList

@Composable
private fun drawableToPainter(drawable: Drawable?): Painter =
    drawable?.let {
        BitmapPainter(it.toBitmap().asImageBitmap())
    } ?: painterResource(R.drawable.ic_brand)

@Composable
private fun ActionIcons(actions: List<String>) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.take(3).forEach { action ->
            val iconRes = StandardActions.icon(action)
            Icon(
                painter = painterResource(iconRes),
                contentDescription = action,
                modifier = Modifier.size(22.dp).padding(start = 4.dp),
            )
        }
    }
}

@Composable
fun ActivityList(
    packageName: String,
    onSelected: (PublicComponent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AsyncList(
        refreshKeys = listOf(packageName),
        loader = {
            queryPublicComponents(context, packageName, exportedOnly = true)
                .components
                .sortedWith(compareBy({ it.type }, { !it.exported }, { it.name }))
        },
        itemKey = { it.name },
        groupBy = { it.type.displayName },
        notFoundMessage = stringResource(R.string.activitylist_not_found),
    ) { component ->
        ActivityRow(component, onSelected)
    }
}

@Composable
private fun ActivityRow(
    component: PublicComponent,
    onClick: (PublicComponent) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick(component) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = drawableToPainter(component.icon),
            contentDescription = component.label,
            modifier = Modifier.size(40.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = component.label ?: component.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = component.name, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.width(8.dp))
        ActionIcons(component.actions)
    }
}

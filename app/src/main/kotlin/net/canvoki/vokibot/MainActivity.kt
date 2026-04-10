package net.canvoki.vokibot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.res.painterResource
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.WatermarkBox
import net.canvoki.shared.log
import net.canvoki.vokibot.AppList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScaffold {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    AppList(
                        onAppSelected = { app ->
                            log("Selected: ${app.packageName}")
                        },
                    )
                }
            }
        }
    }
}

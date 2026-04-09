package net.canvoki.vokibot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import net.canvoki.vokibot.ui.BrandedText
import net.canvoki.vokibot.ui.AppScaffold

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScaffold {
                BrandedText(
                    text = "Hello, World!",
                    icon = painterResource(R.drawable.ic_brand),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

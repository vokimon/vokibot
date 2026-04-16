package net.canvoki.puppet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

abstract class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityName = this::class.java.simpleName
        setContent {
            IntentReport(activityName, intent)
        }
    }
}

class MainActivity : BaseActivity()

class UnfilteredActivity : BaseActivity()

class PrivateActivity : BaseActivity()




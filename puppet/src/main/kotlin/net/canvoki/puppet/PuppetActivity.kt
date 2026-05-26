package net.canvoki.puppet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Service
import android.os.IBinder
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

private fun Intent.displayIntent(context: Context, source: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = this@displayIntent.action
        data = this@displayIntent.data
        putExtras(this@displayIntent)
        putExtra(DISPATCH_SOURCE, source)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

class PuppetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startActivity(intent.displayIntent(context, "PuppetReceiver"))
    }
}

class PuppetService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { startActivity(it.displayIntent(this, "PuppetService")) }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

package net.canvoki.puppet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat
import androidx.core.content.PermissionChecker

private const val TAG = "Puppet"
private const val CHANNEL_ID = "puppet_service"
private const val NOTIFICATION_ID = 1000

abstract class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityName = this::class.java.simpleName
        setContent {
            IntentReport(activityName, intent)
        }
    }
}

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 35) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.FOREGROUND_SERVICE,
                    "android.permission.FOREGROUND_SERVICE_DATA_SYNC",
                ),
                0,
            )
        }
    }
}

class UnfilteredActivity : BaseActivity()

class PrivateActivity : BaseActivity()

class PuppetReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.d(TAG, "Receiver: action=${intent.action}, data=${intent.data}, extras=${intent.extras}")
    }
}

class PuppetForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Puppet Service",
                    NotificationManager.IMPORTANCE_LOW,
                )
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun isGranted(permission: String) =
        PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (Build.VERSION.SDK_INT >= 35 &&
            !isGranted(android.Manifest.permission.FOREGROUND_SERVICE) ||
            !isGranted(android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
        ) {
            Log.w(TAG, "Missing foreground service permissions, stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setContentTitle("Puppet Service")
                .setContentText(intent?.action ?: "no action")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build()
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Service: action=${intent?.action}, data=${intent?.data}, extras=${intent?.extras}")
        //stopForeground(STOP_FOREGROUND_REMOVE)
        //stopSelf()
        return START_NOT_STICKY // Do not restart if i got killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Puppet Service destroyed")
    }
}

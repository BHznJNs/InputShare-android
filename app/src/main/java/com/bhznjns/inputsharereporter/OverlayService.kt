package com.bhznjns.inputsharereporter

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bhznjns.inputsharereporter.utils.PACKAGE_NAME
import com.bhznjns.inputsharereporter.utils.PREFERENCE_FILE_NAME

class OverlayService : AccessibilityService() {
    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_STOP_SERVICE -> {
                    Log.i("AccessibilityService", "Stop intent received, stopping...")
                    disableSelf()
                }
                ACTION_RESET_DIRECTION -> {
                    Log.i("AccessibilityService", "Try to restart overlay.")
                    overlay.close()
                    startOverlay()
                }
            }
        }
    }

    private var reporterServerBinder: ReporterServer.LocalBinder? = null
    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            reporterServerBinder = service as ReporterServer.LocalBinder
            isServiceBound = true
            Log.i("OverlayService", "TCP server service connected")
        }
        override fun onServiceDisconnected(className: ComponentName) {
            reporterServerBinder = null
            isServiceBound = false
            Log.i("OverlayService", "TCP server service disconnected")

            // try to rebind
            Handler(Looper.getMainLooper()).postDelayed({
                startReporterServer()
            }, 2000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction(ACTION_STOP_SERVICE)
        filter.addAction(ACTION_RESET_DIRECTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        startOverlay()
        startReporterServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)

        overlay.close()
        if (isServiceBound) {
            unbindService(serviceConnection)
            reporterServerBinder = null
            isServiceBound = false
        }
    }

    private fun startReporterServer() {
        val serviceIntent = Intent(this, ReporterServer::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private lateinit var overlay: SideLineOverlay
    private fun startOverlay() {
        // get direction param from sharedPreference
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE)
        val isDebugParam = sharedPref.getBoolean("is-debug", false)
        val directionParam = sharedPref.getString("direction", "DEFAULT")!!
        Log.i("AccessibilityService", "Parameter from SharedPreferences: $directionParam")

        overlay = SideLineOverlay(this)
            .setIsDebug(isDebugParam)
            .setDirection(directionParam)
            .setTriggeredCallback { reporterServerBinder?.sendEvent(SERVER_EVENT_TOGGLE) }
        overlay.launch()
    }

    companion object {
        const val SERVICE_NAME =  "${PACKAGE_NAME}/.OverlayService"
        const val ACTION_STOP_SERVICE = "${PACKAGE_NAME}.ACTION_STOP_ACCESSIBILITY_SERVICE"
        const val ACTION_RESET_DIRECTION = "${PACKAGE_NAME}.ACTION_RESET_DIRECTION"
    }
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

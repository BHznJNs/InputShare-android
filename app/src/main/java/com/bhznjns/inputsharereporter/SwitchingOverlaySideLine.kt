package com.bhznjns.inputsharereporter

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

enum class Direction {
    UP, DOWN,
    RIGHT, LEFT,
}

@SuppressLint("ViewConstructor", "RtlHardcoded")
class SwitchingOverlaySideLine(context: Context, attrs: AttributeSet?, direction_: String?) : View(context, attrs) {
    var params: WindowManager.LayoutParams
    private var serviceBinder: TcpSocketServer.LocalBinder? = null
    private var direction: Direction
    private var isBound = false
    private var triggered = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceBinder = service as TcpSocketServer.LocalBinder
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBinder = null
            isBound = false
        }
    }

    init {
        Intent(context, TcpSocketServer::class.java).also {
            context.startService(it)
            context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        direction = parseDirection(direction_)
        params = when (direction) {
            Direction.UP, Direction.DOWN -> WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            Direction.LEFT, Direction.RIGHT -> WindowManager.LayoutParams(
                1,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }
        params.gravity = when (direction) {
            Direction.LEFT  -> Gravity.LEFT
            Direction.RIGHT -> Gravity.RIGHT
            Direction.UP    -> Gravity.TOP
            Direction.DOWN  -> Gravity.BOTTOM
        }
        setBackgroundColor(Color.TRANSPARENT)
        setBackgroundColor(Color.RED)
        requirePermission()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_HOVER_ENTER) {
            if (!triggered && isBound) {
                val data = byteArrayOf(SERVER_EVENT_TOGGLE.toByte())
                serviceBinder!!.sendBytes(data)
            }
            triggered = true
        } else
        if (event.action == MotionEvent.ACTION_HOVER_EXIT) {
            triggered = false
        }
        return super.onGenericMotionEvent(event)
    }

    private fun requirePermission() {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)
            )
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun parseDirection(direction: String?): Direction {
        return when (direction) {
            "up"    -> Direction.UP
            "right" -> Direction.RIGHT
            "left"  -> Direction.LEFT
            // "down"  -> Direction.DOWN
            else    -> Direction.LEFT
        }
    }
}
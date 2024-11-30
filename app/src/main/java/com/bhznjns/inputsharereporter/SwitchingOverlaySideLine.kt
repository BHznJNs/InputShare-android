package com.bhznjns.inputsharereporter

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity.WINDOW_SERVICE

enum class Direction {
    UP, DOWN,
    RIGHT, LEFT,
}

@SuppressLint("ViewConstructor", "RtlHardcoded")
class SwitchingOverlaySideLine(context: Context, attrs: AttributeSet?, direction_: String?) : View(context, attrs) {
    private var params: WindowManager.LayoutParams
    private var serviceBinder: TcpSocketServer.LocalBinder? = null
    private var direction: Direction
    private var isBound = false
    private var triggered = false
    private var edgeTogglingEnabled = true
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceBinder = service as TcpSocketServer.LocalBinder
            isBound = true
            serviceBinder!!.registerCallback(object: ServiceCallback {
                override fun getEdgeTogglingEnabled(): Boolean {
                    return edgeTogglingEnabled
                }
            })
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
    }

    fun launch() {
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, this.params)
    }

    fun toggleEdgeTogglingEnabled(enabled: Boolean) {
        edgeTogglingEnabled = enabled
        if (!isBound) return
        val event = if (enabled) SERVER_EVENT_RESUME else SERVER_EVENT_PAUSE
        serviceBinder!!.sendEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_HOVER_ENTER) {
            if (!triggered && isBound && edgeTogglingEnabled) {
                serviceBinder!!.sendEvent(SERVER_EVENT_TOGGLE)
            }
            triggered = true
        } else
        if (event.action == MotionEvent.ACTION_HOVER_EXIT) {
            triggered = false
        }
        return super.onGenericMotionEvent(event)
    }

    private fun parseDirection(direction: String?): Direction {
        return when (direction) {
            "up"    -> Direction.UP
            "right" -> Direction.RIGHT
            "left"  -> Direction.LEFT
            "down"  -> Direction.DOWN
            else    -> Direction.LEFT
        }
    }
}
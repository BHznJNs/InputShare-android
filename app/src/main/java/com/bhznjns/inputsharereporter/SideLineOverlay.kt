package com.bhznjns.inputsharereporter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.bhznjns.inputsharereporter.utils.Direction

typealias TriggeredCallback = () -> Unit

class SideLineOverlay : View {
    private lateinit var triggerCallback: TriggeredCallback
    private lateinit var params: WindowManager.LayoutParams

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setDirection(direction: String?): SideLineOverlay {
        val direction = parseDirection(direction)
        setParamWithDirection(direction)
        return this
    }

    fun setTriggeredCallback(callback: TriggeredCallback): SideLineOverlay {
        triggerCallback = callback
        return this
    }

    fun launch() {
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, this.params)
    }

    fun close() {
        if (!isAttachedToWindow) return
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        try {
            windowManager.removeView(this)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun setParamWithDirection(direction: Direction) {
        params = when (direction) {
            Direction.UP, Direction.DOWN -> WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                1,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            Direction.LEFT, Direction.RIGHT -> WindowManager.LayoutParams(
                1,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
        }
        params.gravity = when (direction) {
            Direction.LEFT  -> Gravity.LEFT
            Direction.RIGHT -> Gravity.RIGHT
            Direction.UP    -> Gravity.TOP
            Direction.DOWN  -> Gravity.BOTTOM
        }
    }

    private fun parseDirection(direction: String?): Direction {
        Log.d("SideLineOverlay", "Received direction: $direction")
        return when (direction) {
            "up"    -> Direction.UP
            "right" -> Direction.RIGHT
            "left"  -> Direction.LEFT
            "down"  -> Direction.DOWN
            else    -> Direction.LEFT
        }
    }

    private var triggered = false
    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_HOVER_ENTER) {
            if (!triggered) {
                triggerCallback()
            }
            triggered = true
        } else if (event?.action == MotionEvent.ACTION_HOVER_EXIT) {
            triggered = false
        }
        return super.onGenericMotionEvent(event)
    }
}
package com.bhznjns.inputsharereporter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

enum class Direction {
//    UP, DOWN,
    RIGHT, LEFT
}

@SuppressLint("ViewConstructor")
class SwitchingOverlaySideLine(context: Context, attrs: AttributeSet?, direction_: String?) : View(context, attrs) {
    var direction: Direction
    var params: WindowManager.LayoutParams
    var triggered = false

    init {
        direction = parseDirection(direction_)
        params = WindowManager.LayoutParams(
            1,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
//        params = when (direction) {
//            Direction.UP, Direction.DOWN -> WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                1,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT
//            )
//            Direction.LEFT, Direction.RIGHT -> WindowManager.LayoutParams(
//                1,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT
//            )
//        }
        params.gravity = when (direction) {
            Direction.LEFT  -> Gravity.START
            Direction.RIGHT -> Gravity.END
//            Direction.UP    -> Gravity.TOP
//            Direction.DOWN  -> Gravity.BOTTOM
        }
        setBackgroundColor(Color.TRANSPARENT)
        //        setBackgroundColor(Color.RED)
        requirePermission()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_HOVER_ENTER) {
            Log.i("Mouse", "Entered");
            triggered = true
        } else
        if (event.action == MotionEvent.ACTION_HOVER_EXIT) {
            triggered = false
            Log.i("Mouse", "Exited");
        }
//        val x = event.x
//        val y = event.y
//        Log.d("Mouse Position", "X: $x, Y: $y")
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
        if (direction == null) {
            return Direction.LEFT
        }
        return when (direction) {
//            "up"    -> Direction.UP
//            "down"  -> Direction.DOWN
            "right" -> Direction.RIGHT
            "left"  -> Direction.LEFT
            else    -> Direction.LEFT
        }
    }
}
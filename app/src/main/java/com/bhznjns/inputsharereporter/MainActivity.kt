package com.bhznjns.inputsharereporter

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    var overlayView: SwitchingOverlaySideLine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = intent
        val direction = intent.getStringExtra("direction")

        overlayView = SwitchingOverlaySideLine(
            this,
            attrs = null,
            direction_ = direction,
        )
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, overlayView!!.params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }
}
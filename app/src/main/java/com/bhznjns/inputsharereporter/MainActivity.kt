package com.bhznjns.inputsharereporter

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.accessibility.AccessibilityManager
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bhznjns.inputsharereporter.utils.I18n
import com.bhznjns.inputsharereporter.utils.MarkdownRenderer
import com.bhznjns.inputsharereporter.utils.PREFERENCE_FILE_NAME
import com.bhznjns.inputsharereporter.utils.getAccessibilityServiceEnabled
import com.bhznjns.inputsharereporter.utils.targetShortcuts
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Initialized.")
        setDirectionPref()
        setDebugPref()
        renderShortcuts()
        setupFab()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "Received new intent.")
        setIntent(intent)
        setDirectionPref()
        setDebugPref()
        val actionIntent = Intent(OverlayService.ACTION_RESET_DIRECTION)
        LocalBroadcastManager.getInstance(this).sendBroadcast(actionIntent)
    }

    override fun onConfigurationChanged(p0: Configuration) {
        // prevent activity refreshing after UHID state changed
        Log.d("MainActivity", "Configuration changed.")
        super.onConfigurationChanged(p0)
    }

    private fun renderShortcuts() {
        val webview = findViewById<WebView>(R.id.webview)

        // set webview background color
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.background, typedValue, true)
        val backgroundColor = typedValue.data
        webview.setBackgroundColor(backgroundColor)

        // render shortcuts
        val html = MarkdownRenderer().renderMarkdown(targetShortcuts)
        webview.loadData(html, "text/html", "UTF-8")
    }

    private fun setupFab() {
        fun getIconFromState(isExtended: Boolean): Int =
            if (isExtended) R.drawable.play_64 else R.drawable.stop_64

        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fab)
        fab.isExtended = !isAccessibilityServiceEnabled
        fab.setOnClickListener {
            Log.d("MainActivity", "FAB clicked, isExtended: ${fab.isExtended}")
            if (fab.isExtended) {
                showAccessibilityServiceDialog()
            } else {
                // send stop intent
                val actionIntent = Intent(OverlayService.ACTION_STOP_SERVICE)
                LocalBroadcastManager.getInstance(this).sendBroadcast(actionIntent)
            }
        }
        if (!isAccessibilityServiceEnabled)
            fab.performClick()

        // set Accessibility Change Event Listener
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener {
            Handler(Looper.getMainLooper()).postDelayed({
                val accessibilityState = isAccessibilityServiceEnabled
                Log.i("AccessibilityState", "Accessibility state changed: $accessibilityState")
                val toBeExtended = !accessibilityState
                fab.setIconResource(getIconFromState(toBeExtended))
                if (toBeExtended) fab.extend() else fab.shrink()
            }, 500)
        }
    }

    private fun setDebugPref() {
        val paramName = "is-debug"
        val isDebugParam = intent.getBooleanExtra(paramName, false)
        Log.d("MainActivity", "Debug param: $isDebugParam")

        val sharedPref = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean(paramName, isDebugParam)
            apply()
        }
    }

    private fun setDirectionPref() {
        val paramName = "direction"
        val directionParam = intent.getStringExtra(paramName)
        Log.d("MainActivity", "Direction param: $directionParam")

        if (directionParam == null) return
        val sharedPref = getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString(paramName, directionParam)
            apply()
        }
    }

    private fun showAccessibilityServiceDialog() {
        if (isAccessibilityServiceEnabled) return
        AlertDialog.Builder(this)
            .setTitle(I18n.choose(listOf("Enable Service","启用无障碍服务")))
            .setMessage(I18n.choose(listOf(
                "To use the features of this application properly, please enable the accessibility service in the settings.",
                "为了正常使用本应用的功能，请在设置中启用无障碍服务。")))
            .setPositiveButton(I18n.choose(listOf("Confirm", "确定"))) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(I18n.choose(listOf("Cancel", "取消")), null)
            .setCancelable(false)
            .show()
    }

    private val isAccessibilityServiceEnabled: Boolean
        get() = getAccessibilityServiceEnabled(this, OverlayService.SERVICE_NAME)
}
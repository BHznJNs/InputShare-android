package com.bhznjns.inputsharereporter

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bhznjns.inputsharereporter.utils.I18n
import com.bhznjns.inputsharereporter.utils.MarkdownRenderer
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class MainActivity : AppCompatActivity() {
    private var fab: ExtendedFloatingActionButton? = null
    private var overlayView: SwitchingOverlaySideLine? = null
    private var isFABExtended = true
    private val requestOverlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        if (Settings.canDrawOverlays(this)) overlayView!!.launch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        startOverlayView()
        renderShortcuts()
        setFABEventListener()
        fab!!.performClick()
    }

    private fun startOverlayView() {
        val directionParam = intent.getStringExtra("direction")
        overlayView = SwitchingOverlaySideLine(this, null, directionParam)
        if (Settings.canDrawOverlays(this)) {
            overlayView!!.launch()
            return
        }

        val goToSettingsClickListener = DialogInterface.OnClickListener { _, _ ->
            val requestPermissionIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            requestPermissionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            requestOverlayPermissionLauncher.launch(requestPermissionIntent)
        }
        val canceledClickListener = DialogInterface.OnClickListener { _, _ ->
            Toast.makeText(this, I18n.choose(listOf(
                "Permission not granted. The application may not function properly",
                "未授予权限，程序可能无法正常运行",
            )), Toast.LENGTH_SHORT).show()
        }

        val dialogEN = AlertDialog.Builder(this)
            .setTitle("Permission Missing")
            .setMessage("The app requires overlay permission to function properly. Please enable this permission in the settings.")
            .setPositiveButton("Go to Settings", goToSettingsClickListener)
            .setNegativeButton("Cancel", canceledClickListener)
        val dialogZH = AlertDialog.Builder(this)
            .setTitle("权限缺失")
            .setMessage("应用需要悬浮窗权限才能正常显示功能，请在设置中启用该权限。")
            .setPositiveButton("去设置", goToSettingsClickListener)
            .setNegativeButton("取消", canceledClickListener)

        I18n.choose(listOf(dialogEN, dialogZH)).show()
    }

    private fun renderShortcuts() {
        val shortcutsEN = "The shortcuts following are available after connection:\n" +
                "\n" +
                "| Shortcut | Description |\n" +
                "| --- | --- |\n" +
                "| `<Ctrl>+<Alt>+s` | Toggle the sharing state |\n" +
                "| `<Ctrl>+<Alt>+q` | Quit the program |\n" +
                "| `F1` | Multi-task switching |\n" +
                "| `F2` | Return to Home |\n" +
                "| `F3` | Back |\n" +
                "| `F4` | Previous Media |\n" +
                "| `F5` | Play / Pause Media |\n" +
                "| `F6` | Next Media |\n" +
                "| `F7` | Volume Down |\n" +
                "| `F8` | Volume Up |\n" +
                "| `F9` | Brightness Down |\n" +
                "| `F10` | Brightness Up |\n" +
                "| `F11` | Screen Sleep |\n" +
                "| `F12` | Wake Up |\n" +
                "\n" +
                "The shortcuts following are available after connection and not sharing:\n" +
                "\n" +
                "| Shortcut | Description |\n" +
                "| --- | --- |\n" +
                "| `<Alt>+UP` | Scroll Up |\n" +
                "| `<Alt>+DOWN` | Scroll Down |\n" +
                "| `<Alt>+[` | Previous Media |\n" +
                "| `<Alt>+]` | Next Media |\n" +
                "| `<Alt>+\\` | Play / Pause Media |"
        val shortcutsZH = "下面的快捷键在你连接设备完成后可用：\n" +
                "\n" +
                "| 快捷键 | 功能描述 |\n" +
                "| --- | --- |\n" +
                "| `<Ctrl>+<Alt>+s` | 切换控制 |\n" +
                "| `<Ctrl>+<Alt>+q` | 退出程序 |\n" +
                "| `F1` | 多任务切换 |\n" +
                "| `F2` | 回到桌面 |\n" +
                "| `F3` | 返回 |\n" +
                "| `F4` | 上一首歌曲 |\n" +
                "| `F5` | 播放 / 暂停 歌曲 |\n" +
                "| `F6` | 下一首歌曲 |\n" +
                "| `F7` | 降低音量 |\n" +
                "| `F8` | 提升音量 |\n" +
                "| `F9` | 降低亮度 |\n" +
                "| `F10` | 提升亮度 |\n" +
                "| `F11` | 熄屏 |\n" +
                "| `F12` | 亮屏 |\n" +
                "\n" +
                "下面的快捷键在你连接设备完成后且没有共享输入时可用：\n" +
                "\n" +
                "| 快捷键 | 功能描述 |\n" +
                "| --- | --- |\n" +
                "| `<Alt>+UP` | 向上滚动 |\n" +
                "| `<Alt>+DOWN` | 向下滚动 |\n" +
                "| `<Alt>+[` | 上一首歌曲 |\n" +
                "| `<Alt>+]` | 下一首歌曲 |\n" +
                "| `<Alt>+\\` | 播放 / 暂停 歌曲 |"
        val webview = findViewById<WebView>(R.id.webview)

        // set webview background color
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.background, typedValue, true)
        val backgroundColor = typedValue.data
        webview.setBackgroundColor(backgroundColor)

        // render shortcuts
        val targetShortcuts = I18n.choose(listOf(shortcutsEN, shortcutsZH))
        val html = MarkdownRenderer().renderMarkdown(targetShortcuts)
        webview.loadData(html, "text/html", "UTF-8")
    }

    private fun setFABEventListener() {
        fab = findViewById(R.id.fab)
        fab!!.setOnClickListener {
            val isEdgeTogglingEnabled = isFABExtended
            overlayView!!.toggleEdgeTogglingEnabled(isEdgeTogglingEnabled)
            toggleIsExtended(!isFABExtended)
        }
    }

    private fun toggleIsExtended(toBeExtended: Boolean) {
        isFABExtended = toBeExtended
        if (toBeExtended) {
            fab!!.extend()
            fab!!.setIconResource(R.drawable.play_64)
        } else {
            fab!!.shrink()
            fab!!.setIconResource(R.drawable.pause_64)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null && overlayView!!.isViewAdded && windowManager != null) {
            windowManager.removeView(overlayView)
        }
    }
}
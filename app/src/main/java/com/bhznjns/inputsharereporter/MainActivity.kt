package com.bhznjns.inputsharereporter

import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    var overlayView: SwitchingOverlaySideLine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        renderShortcuts()

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

    fun renderShortcuts() {
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
                "| `<Ctrl>+<Alt>+s` | 在你的电脑和 Android 设备间切换控制 |\n" +
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
        val targetShortcuts = when (Locale.getDefault().language) {
            "en" -> shortcutsEN
            "zh" -> shortcutsZH
            else -> shortcutsEN
        }
        val html = MarkdownRenderer().renderMarkdown(targetShortcuts)
        webview.loadData(html, "text/html", "UTF-8")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }
}
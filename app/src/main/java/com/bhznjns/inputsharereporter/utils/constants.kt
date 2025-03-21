package com.bhznjns.inputsharereporter.utils

const val PACKAGE_NAME = "com.bhznjns.inputsharereporter"
const val PREFERENCE_FILE_NAME = PACKAGE_NAME + "_preference"

const val SHORTCUTS_EN = "The shortcuts following are available after connection:\n" +
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
const val SHORTCUTS_ZH = "下面的快捷键在你连接设备完成后可用：\n" +
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
val targetShortcuts = I18n.choose(listOf(SHORTCUTS_EN, SHORTCUTS_ZH))

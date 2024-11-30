package com.bhznjns.inputsharereporter.utils

import android.annotation.SuppressLint
import java.util.Locale

object I18n {
    @SuppressLint("ConstantLocale")
    private val targetIndex = when (Locale.getDefault().language) {
        "en" -> 0
        "zh" -> 1
        else -> 0
    }

    fun<T> choose(params: List<T>): T {
        return params[targetIndex]
    }
}
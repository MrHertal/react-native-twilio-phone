package com.reactnativetwiliophone.utils

import android.content.Intent
import android.os.Bundle
import androidx.annotation.Nullable
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class NotificationsHeadlessReceiver : HeadlessJsTaskService() {
    @Nullable
    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            val notification =
                    intent.getBundleExtra(NotificationUtils.EXTRA_NOTIFICATION)
            val notificationMap: WritableMap = Arguments.fromBundle(notification)
            notification?.let {
                return HeadlessJsTaskConfig(
                        "NotificationsListenerTask",
                        notificationMap,
                        5000,  // timeout for the task
                        true // optional: defines whether or not  the task is allowed in foreground. Default is false
                )
            }
        }
        return null
    }
}


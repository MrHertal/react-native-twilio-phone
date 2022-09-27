package com.reactnativetwiliophone.boradcastReceivers

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.Nullable
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.callView.ViewService
import com.reactnativetwiliophone.log

class NotificationsHeadlessReceiver : HeadlessJsTaskService() {

    @Nullable
    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            val notification =
                    intent.getBundleExtra(Const.EXTRA_NOTIFIER)
            val notificationMap: WritableMap = Arguments.fromBundle(notification)
            notification?.let {
             // stopViewService(intent)

                return HeadlessJsTaskConfig(
                        "NotificationsListenerTask",
                        notificationMap,
                        5000,  // timeout for the task
                        true // optional: defines whether or not  the task is allowed in foreground. Default is false
                )
            }


          //stopViewService(intent)
        }
        return null
    }

   fun stopViewService(name: Intent?) {
     log("NotificationsHeadlessReceiver stopService")

     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
       stopForeground(STOP_FOREGROUND_REMOVE)
       ViewService().doUnbindService()
      stopService(name)
    }
  }
  override fun onDestroy() {
    super.onDestroy()
    // cancel any running threads here
   // LocalBroadcastManager.getInstance(this).unregisterReceiver(NotificationsHeadlessReceiver)
  }
}


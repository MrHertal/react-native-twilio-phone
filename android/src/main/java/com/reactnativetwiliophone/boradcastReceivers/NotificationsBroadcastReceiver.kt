package com.reactnativetwiliophone.boradcastReceivers

import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.ReactApplication
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.TwilioPhoneModule
import com.reactnativetwiliophone.log


class NotificationsBroadcastReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val extras: Bundle? = intent.extras
    if (extras != null) {
      val notification =
        intent.getParcelableExtra(Const.EXTRA_NOTIFIER) as Bundle?
      if (notification != null) {
        val action = intent.getStringExtra("action")
        val callSid = intent.getStringExtra("callSid")
        val notificationId = intent.getIntExtra("notificationId", 1)
        val mainActivityClassName = intent.getStringExtra("activityName")
        val appIntent = Intent(context, mainActivityClassName?.let { Class.forName(it) })
        val contentIntent: PendingIntent = getActivity(context, 0, appIntent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        try {
          contentIntent.send()
        } catch (e: CanceledException) {
          e.printStackTrace()
        }
        /**
         * collapse notification bar
         */
//                val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
//                context.sendBroadcast(closeIntent)
        /**
         * ###################################
         */
        try {
          val headlessIntent = Intent(
            context,
            NotificationsHeadlessReceiver::class.java
          )
          notification.putString("action", action)
          notification.putInt("notificationId", notificationId)
          headlessIntent.putExtra(Const.EXTRA_NOTIFIER, notification)
          val name: ComponentName? = context.startService(headlessIntent)
          val rnApp = context.applicationContext as ReactApplication
          val parent = rnApp.reactNativeHost.reactInstanceManager.currentReactContext?.getNativeModule(TwilioPhoneModule::class.java)

          if (name != null) {
            HeadlessJsTaskService.acquireWakeLockNow(context)
          }
          if (action == "answered") {
            if (parent != null) {
              if (callSid != null) {
                parent.acceptCallInvite(callSid)
              }
            }
          }
          if (action == "rejected") {
            if (parent != null) {
              if (callSid != null) {
                parent.rejectCallInvite(callSid)
              }
            }
          }
          if (action != "tabbed") {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
          }
          if (intent.action == Actions.STOP.name) {
            log("Starting the service in NotificationsBroadcastReceiver")
            context.stopService(intent)
          }
        } catch (ignored: IllegalStateException) {
        }
      }
    }
  }
}

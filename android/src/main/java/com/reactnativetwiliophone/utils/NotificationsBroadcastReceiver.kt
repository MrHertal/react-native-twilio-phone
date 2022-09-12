package com.reactnativetwiliophone.utils

import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.*
import android.os.Bundle
import com.facebook.react.HeadlessJsTaskService
import androidx.core.app.NotificationManagerCompat

class NotificationsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            val notification =
                    intent.getParcelableExtra(NotificationUtils.EXTRA_NOTIFICATION) as Bundle?
            if (notification != null) {
                val action = intent.getStringExtra("action")
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
                try{
                    val headlessIntent = Intent(
                            context,
                            NotificationsHeadlessReceiver::class.java
                    )
                    notification.putString("action", action)
                    notification.putInt("notificationId", notificationId)
                    headlessIntent.putExtra(NotificationUtils.EXTRA_NOTIFICATION, notification)
                    val name: ComponentName? = context.startService(headlessIntent)
                    if (name != null) {
                        HeadlessJsTaskService.acquireWakeLockNow(context)
                    }
                    if(action != "tabbed") {
                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.cancel(notificationId)
                    }
                }catch (ignored: IllegalStateException){
                }
            }
        }
    }
}

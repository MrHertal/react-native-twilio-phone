package com.reactnativetwiliophone.utils

import android.app.*
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.reactnativetwiliophone.Const
import java.util.*
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.boradcastReceivers.NotificationsBroadcastReceiver

object NotificationUtils {

  private fun createCallChannel(notificationManager: NotificationManagerCompat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannelCompat.Builder(
        Const.INCOMING_CALL_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_HIGH
      )
        .setName("Incoming calls")
        .setDescription("Incoming audio call alerts")
        .build()
      notificationManager.createNotificationChannel(channel)
    }
  }
  private fun createNotificationIntent(
    context: Context,
    notificationDataBundle: Bundle,
    actionType: String,
    notificationId: Int,
    activityName: String,
  ): PendingIntent? {
    val clickIntentData = Intent(context, NotificationsBroadcastReceiver::class.java)
    clickIntentData.putExtra("action", actionType)
    clickIntentData.putExtra("notificationId", notificationId)
    clickIntentData.putExtra("activityName", activityName)

    clickIntentData.putExtra(Const.EXTRA_NOTIFIER, notificationDataBundle)
    val requestCode = UUID.randomUUID().hashCode()
    return getBroadcast(context, requestCode, clickIntentData, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
  }

  fun showCallNotification(context: Context, notificationData: ReadableMap, notificationId: Int) {
    val notificationManager = NotificationManagerCompat.from(context)
    val notificationDataBundle = Arguments.toBundle(notificationData)
    val activityClass = context.packageName+".MainActivity"
    val answerIntent = notificationDataBundle?.let {
      createNotificationIntent(context,
        it,"answer", notificationId, activityClass)
    }
    val rejectIntent = notificationDataBundle?.let {
      createNotificationIntent(context,
        it,"reject", notificationId, activityClass)
    }
    val bodyIntent = notificationDataBundle?.let {
      createNotificationIntent(context,
        it,"tabbed", notificationId, activityClass)
    }
    createCallChannel(notificationManager)
    val channelId = Const.INCOMING_CALL_CHANNEL_ID;
    val callerName = notificationDataBundle?.getString("callerName", "")
    val notificationIcon: Int = R.drawable.logo_round
    val notificationBuilder: NotificationCompat.Builder =
      NotificationCompat.Builder(context, channelId)
        .setSmallIcon(notificationIcon)
        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setVibrate(longArrayOf(1L, 2L, 3L))
        .setTicker("Call_STATUS")
        .setOngoing(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      notificationBuilder.priority = NotificationManager.IMPORTANCE_HIGH
    }
    val remoteView = RemoteViews(context.packageName, R.layout.notification_view)
    remoteView.setOnClickPendingIntent(R.id.imgAnswer, answerIntent)
    remoteView.setOnClickPendingIntent(R.id.imgDecline, rejectIntent)
    remoteView.setTextViewText(R.id.callerNameN, callerName);

    notificationBuilder.setCustomContentView(remoteView)
    notificationBuilder.setFullScreenIntent(bodyIntent, true)
    notificationManager.notify(notificationId, notificationBuilder.build())
  }

  fun cancelPushNotification(context: Context, notificationId: Int) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(notificationId)
  }
}

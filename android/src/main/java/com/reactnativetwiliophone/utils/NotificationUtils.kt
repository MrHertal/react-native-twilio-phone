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
import java.util.*


object NotificationUtils {
    const val EXTRA_NOTIFICATION = "com.reactnativetwiliophone.EXTRA_NOTIFICATION"
    private const val INCOMING_CALL_CHANNEL_ID = "incoming_call_channel_id"

  private fun createCallChannel(notificationManager: NotificationManagerCompat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannelCompat.Builder(
        INCOMING_CALL_CHANNEL_ID,
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

    clickIntentData.putExtra(EXTRA_NOTIFICATION, notificationDataBundle)
    val requestCode = UUID.randomUUID().hashCode()
    return getBroadcast(context, requestCode, clickIntentData, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
  }

  fun showCallNotification(context: Context, notificationData: ReadableMap, activity: Activity, notificationId: Int) {
    val notificationManager = NotificationManagerCompat.from(context)
    val notificationDataBundle = Arguments.toBundle(notificationData)
    val answerIntent = notificationDataBundle?.let {
      createNotificationIntent(context,
        it,"answer", notificationId, activity.componentName.className)
    }
    val rejectIntent = notificationDataBundle?.let {
      createNotificationIntent(context,
        it,"reject", notificationId, activity.componentName.className)
    }
    val bodyIntent = notificationDataBundle?.let {
      createNotificationIntent(context,
        it,"tabbed", notificationId, activity.componentName.className)
    }
    createCallChannel(notificationManager)
    val channelId = INCOMING_CALL_CHANNEL_ID;
    val callerName = notificationDataBundle?.getString("callerName", "")
    val notificationIcon: Int = context.resources.getIdentifier("ic_notify", "drawable", context.packageName)
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
    val layoutId = context.resources.getIdentifier("notification_custom", "layout", context.packageName)
    val answerButton = context.resources.getIdentifier("btnAnswer", "id", context.packageName)
    val rejectButton = context.resources.getIdentifier("btnDecline", "id", context.packageName)
    val callerNamePlaceHolder = context.resources.getIdentifier("callerName", "id", context.packageName)
    val remoteView = RemoteViews(context.packageName, layoutId)

    remoteView.setOnClickPendingIntent(answerButton, answerIntent)
    remoteView.setOnClickPendingIntent(rejectButton, rejectIntent)
    remoteView.setTextViewText(callerNamePlaceHolder, callerName)

    notificationBuilder.setCustomContentView(remoteView)
    notificationBuilder.setFullScreenIntent(bodyIntent, true)
    notificationManager.notify(notificationId, notificationBuilder.build())
  }

  fun cancelPushNotification(context: Context, notificationId: Int) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(notificationId)
  }
}

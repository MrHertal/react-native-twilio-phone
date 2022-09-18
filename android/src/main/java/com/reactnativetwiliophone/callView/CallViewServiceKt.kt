package com.reactnativetwiliophone.callView

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.facebook.react.HeadlessJsTaskService
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver
import com.reactnativetwiliophone.callView.CallView
import com.reactnativetwiliophone.callView.ViewService
import java.util.*


class CallViewServiceKt : ViewService() {
  private var mIntent: Intent? = null
  private var extras: Bundle? = null
  private val textView: TextView? = null
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    mIntent = intent
    Log.d("callMyService", "onStartCommand")
    extras = intent!!.extras
    if (extras != null && textView != null) {
      textView.text = extras!!.getString(Const.CALLER_NAME)
    }
    return super.onStartCommand(intent, flags, startId)
  }

  private fun createNotificationIntent(
    context: Context,  //  Bundle notificationDataBundle  ,
    actionType: String,
    notificationId: Int,
    activityName: String
  ): PendingIntent {
    val pendingFlags: Int
    pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_IMMUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }
    var clickIntentData: Intent? = null
    try {
      clickIntentData = Intent(this, Class.forName(context.packageName + ".MainActivity"))
    } catch (e: ClassNotFoundException) {
      e.printStackTrace()
    }
    clickIntentData!!.putExtra("action", actionType)
    clickIntentData.putExtra("notificationId", notificationId)
    clickIntentData.putExtra("activityName", activityName)
    val requestCode = UUID.randomUUID().hashCode()
    return PendingIntent.getBroadcast(
      context, requestCode,
      clickIntentData, pendingFlags
    )
  }

  // for android 8 and above
  override fun setupNotificationBuilder(channelId: String): Notification {
    val remoteView = RemoteViews(this.packageName, R.layout.notification_view)
    // Bundle notificationDataBundle = Arguments.toBundle(notificationData);
    val bodyIntent = createNotificationIntent(
      this,
      "tabbed",
      Const.NOTIFICATION_ID,
      this.packageName + ".MainActivity"
    )
    //remoteView.setOnClickPendingIntent(R.id.imgAnswer, answerIntent)
    // remoteView.setOnClickPendingIntent(R.id.imgDecline, rejectIntent)
    remoteView.setTextViewText(R.id.callerNameN, "test")
    val notificationBuilder: NotificationCompat.Builder =
      NotificationCompat.Builder(this, channelId)
        .setOngoing(true)
        .setSmallIcon(R.drawable.logo_round)
        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setTicker("Call_STATUS")
        .setOngoing(true)
    // .setCustomContentView(remoteView)
    // .setFullScreenIntent(bodyIntent, true);
    return notificationBuilder.build()
  }

  override fun setupCallView(action: CallView.Action): CallView.Builder? {
    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout: View = inflater.inflate(R.layout.call_view, null)
    layout.findViewById<View>(R.id.expanded).setOnClickListener { v: View? ->
//            Toast.makeText(this, "hello from card view from java", Toast.LENGTH_SHORT).show();
      action.popCallView()
    }
    val textView = layout.findViewById<TextView>(R.id.callerNameV)
    if (extras != null) {
      textView.text = extras!!.getString(Const.CALLER_NAME)
    }
    layout.findViewById<View>(R.id.imgDecline).setOnClickListener { v: View? ->
      handleIntent(extras, Const.REJECT)
      tryStopService()
    }
    layout.findViewById<View>(R.id.imgAnswer).setOnClickListener { v: View? ->
      handleIntent(extras, Const.ANSWER)
      tryStopService()
    }
    return CallView.Builder()
      .with(this)
      .setCallView(layout)
      .setDimAmount(0.8f)
      .addCallViewListener(object : CallView.Action {
        override fun popCallView() {
          popCallView()
        }

        override fun onOpenCallView() {
          Log.d("<>", "onOpenFloatingView: ")
        }

       /* override fun onCloseCallView() {
          Log.d("<>", "onCloseFloatingView: ")
        }*/
      })
  }

  fun handleIntent(extras: Bundle?, type: String?) {
    Log.d("callMyService", "callExtra not null")
    var appIntent: Intent? = null
    try {
      appIntent = Intent(this, Class.forName("com.iriscrm.MainActivity"))
    } catch (e: ClassNotFoundException) {
      e.printStackTrace()
    }
    val pendingFlags: Int
    pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_IMMUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }
    val contentIntent = PendingIntent.getActivity(
      this,
      0,
      appIntent,
      pendingFlags
    )
    try {
      Log.d("callMyService", " contentIntent.send")
      contentIntent.send()
    } catch (e: PendingIntent.CanceledException) {
      Log.d("callMyService", " contentIntent.send CanceledException = \$e")
    }
    try {
      val headlessIntent = Intent(
        this,
        NotificationsHeadlessReceiver::class.java
      )
      extras!!.putString(Const.ACTION, type)
      headlessIntent.putExtra(Const.EXTRA_NOTIFIER, extras)
      val name = startService(headlessIntent)
      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(this)
      }
      tryStopService()
    } catch (ignored: IllegalStateException) {
    }
  }
}

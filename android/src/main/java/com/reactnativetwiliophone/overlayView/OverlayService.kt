package com.reactnativetwiliophone.overlyView

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.facebook.react.HeadlessJsTaskService
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver


abstract class OverlayService : OverlayServiceConfig(), Logger by LoggerImpl() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (isDrawOverlaysPermissionGranted()) {

            setupViewAppearance()

            if (isHigherThanAndroid8()) {
                startViewForeground()
            }

        } else throw PermissionDeniedException()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // overridable func ----------------------------------------------------------------------------

    open fun channelId() = Const.INCOMING_CALL_CHANNEL_ID
    open fun channelName() = Const.INCOMING_CALL_CHANNEL_NAME

    open fun notificationId() = 101

    open fun startViewForeground() {

        val channelId = if (isHigherThanAndroid8()) {
            createNotificationChannel(channelId(), channelName())
        } else {
            // In earlier version, channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }
        val notification = setupNotificationBuilder(channelId)

        startForeground(notificationId(), notification)
    }


/*  override fun setupOverLyView(action: OverLyView.Action): OverLyView.Builder? {
    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout = inflater.inflate(R.layout.overly_view, null)
    layout.findViewById<View>(R.id.expanded).setOnClickListener { v: View? ->
//            Toast.makeText(this, "hello from card view from java", Toast.LENGTH_SHORT).show();
      action.popOverLyView()
    }
    val textView = layout.findViewById<TextView>(R.id.callerNameV)
    Handler(Looper.getMainLooper()).postDelayed(
      {
        textView.text = extras.getString(Const.CALLER_NAME)
        Log.d("callMyService", "callExtra CALLER_NAME 3 second");
      },
      3000 // value in milliseconds
    )

    layout.findViewById<View>(R.id.imgDecline).setOnClickListener { v: View? ->
      handleIntent(extras,Const.ANSWER)
      tryStopService()
    }
    layout.findViewById<View>(R.id.imgAnswer).setOnClickListener { v: View? ->
      handleIntent(extras,Const.ANSWER)
      tryStopService()
    }
    return OverLyView.Builder()
      .with(this)
      .setOverLyView(layout)
      .setDimAmount(0.8f)
      .addOverLyViewListener(object : OverLyView.Action {
        override fun popOverLyView() {
          popOverLyView()
        }

        override fun onOpenOverLyView() {
          Log.d("<>", "onOpenFloatingView: ")
        }

        override fun onCloseOverLyView() {
          Log.d("<>", "onCloseFloatingView: ")
        }
      })
  }*/


    open fun setupNotificationBuilder(channelId: String): Notification {

        return NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.logo_round)
            .setContentTitle("icomming")
            //.setContentText(extras.getString(Const.CALLER_NAME))
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }


    @Deprecated("this function may not work properly", ReplaceWith("true"))
    open fun setLoggerEnabled(): Boolean = true

    // helper --------------------------------------------------------------------------------------

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String,
        channelName: String
    ): String {
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }


    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    private fun isHigherThanAndroid8() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

}

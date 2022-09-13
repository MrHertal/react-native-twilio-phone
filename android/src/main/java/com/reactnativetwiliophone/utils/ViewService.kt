package com.reactnativetwiliophone.utils

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import com.facebook.react.HeadlessJsTaskService
import com.reactnativetwiliophone.R


class ViewService : Service() {
  private var mWindowManager: WindowManager? = null
  private var mFloatingView: View? = null
  var mIntent = Intent()
  var extras= Bundle()
  override fun onBind(intent: Intent?): IBinder? {
    mIntent= intent!!
    Log.d("callMyService", "onBind");

    return null
  }
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    mIntent=intent!!
    Log.d("callMyService", "onStartCommand");
     extras = intent.extras!!
    val mainActivityClassName = mIntent.getStringExtra(ViewUtils.ACTIVITY_NAME)
    val callerName = mIntent.getStringExtra(ViewUtils.CALLER_NAME)
    Log.d("callMyService", "onStartCommand callerName="+callerName);
    Log.d("callMyService", "onStartCommand mainActivityClassName="+mainActivityClassName);

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onCreate() {
    super.onCreate()
    Log.d("callMyService", "onCreate");

    mFloatingView = LayoutInflater.from(this).inflate(R.layout.notification_custom, null)
    val params: WindowManager.LayoutParams

    params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams(
        MATCH_PARENT,
        WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
      )
    } else {
      WindowManager.LayoutParams(
        MATCH_PARENT,
        WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
      )
    }
    //Specify the view position
    params.gravity = Gravity.CENTER

    params.x = 8;
    params.y = 8;
    //Add the view to the window
    mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager?

    mWindowManager?.addView(mFloatingView, params)
        Log.d("callMyService", "callExtra not null");

        val text: TextView = mFloatingView!!.findViewById(R.id.callerName)

      Handler(Looper.getMainLooper()).postDelayed(
        {
          text.text = extras.getString(ViewUtils.CALLER_NAME)
          Log.d("callMyService", "callExtra CALLER_NAME 3 second");
        },
        3000 // value in milliseconds
      )

    val openButton: ImageView = mFloatingView!!.findViewById(R.id.imgAnswer)
      openButton.setOnClickListener {
        Log.d("callMyService", "click ANSWER");
        if(extras!=null){
          handleIntent(extras,ViewUtils.ANSWER)
          stopSelf()
         // android.os.Process.killProcess(android.os.Process.myPid())
        }
      }
      val cancelButton: ImageView = mFloatingView!!.findViewById(R.id.imgDecline)
      cancelButton.setOnClickListener {
        if(extras!=null){
          handleIntent(extras,ViewUtils.REJECT)
          stopSelf()
        //  stopService(mIntent)
         // stopForeground(true)
        }

      }

  }
  fun handleIntent(extras:Bundle,type:String) {
    Log.d("callMyService", "callExtra not null");

    val mainActivityClassName = mIntent.getStringExtra(ViewUtils.ACTIVITY_NAME)
    val appIntent = Intent(this, mainActivityClassName?.let { Class.forName(it) })
    val contentIntent: PendingIntent = PendingIntent.getActivity(
      this,
      0,
      appIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    try {
      Log.d("callMyService", " contentIntent.send");

      contentIntent.send()
    } catch (e: PendingIntent.CanceledException) {

      Log.d("callMyService", " contentIntent.send CanceledException = "+e.toString());

    }
    try {
      val headlessIntent = Intent(
        this,
        NotificationsHeadlessReceiver::class.java
      )
      extras.putString(ViewUtils.ACTION,type)
      headlessIntent.putExtra(ViewUtils.EXTRA_NOTIFIER, extras)
      val name: ComponentName? = startService(headlessIntent)

      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(this)
      }
    } catch (ignored: IllegalStateException) {
    }
  }
  override fun onDestroy() {
    super.onDestroy()
    if (mFloatingView != null) {
      mWindowManager?.removeView(mFloatingView)
      mFloatingView?.visibility=View.GONE
    }
  }
}

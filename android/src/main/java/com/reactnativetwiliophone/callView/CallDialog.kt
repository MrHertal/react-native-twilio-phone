package com.reactnativetwiliophone.callView

import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import com.facebook.react.HeadlessJsTaskService
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver


class CallDialog : Dialog, View.OnClickListener {

  private lateinit var buttonOK: ImageButton
  private lateinit var buttonCancel: ImageButton
  private var mContext:Context ? = null

  private var callerName:String ? = ""
  private var extras: Bundle? = null

  constructor(context: Context,intent:Intent) : this(context, android.R.style.Theme_Light) {
    this.mContext=context
    extras=intent.extras
  }

  constructor(context: Context, themeResId: Int) : super(context, themeResId) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (extras != null)
      callerName = extras!!.getString(Const.CALLER_NAME)
    //request no Title Dialog
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    //set its ContentView
    setContentView(R.layout.call_view)
    //set the window to Full Screen with a Transparent Background
    window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    window?.setBackgroundDrawableResource(android.R.color.transparent)
    //set dialog Cancelable
    setCancelable(true)
    //get views
    buttonOK = findViewById(R.id.imgAnswer)
    buttonCancel = findViewById(R.id.imgDecline)
    val textView = findViewById<TextView>(R.id.callerNameV)
    if (callerName != null ) {
      textView.text =callerName
    }
    //set click listeners
    buttonOK.setOnClickListener(this)
    buttonCancel.setOnClickListener(this)
  }

  override fun onClick(v: View) {

    //ok button click
     if(v.id == buttonOK.id){
       handleIntent(extras, Const.ANSWER)
      dismiss()
    }
    //cancel button click
    else if(v.id == buttonCancel.id){
       handleIntent(extras, Const.REJECT)
      dismiss()
    }
  }

  fun handleIntent(extras: Bundle?, type: String?) {
    Log.d("callMyService", "callExtra not null")
    var appIntent: Intent? = null
    try {
      appIntent = Intent(mContext, Class.forName("com.iriscrm.MainActivity"))
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
      mContext,
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
        mContext,
        NotificationsHeadlessReceiver::class.java
      )
      extras!!.putString(Const.ACTION, type)
      headlessIntent.putExtra(Const.EXTRA_NOTIFIER, extras)
      val name = mContext?.startService(headlessIntent)
      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(mContext)
      }
      dismiss()
    } catch (ignored: IllegalStateException) {
    }
  }
}

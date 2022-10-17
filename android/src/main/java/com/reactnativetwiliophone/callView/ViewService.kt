package com.reactnativetwiliophone.callView

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.graphics.Color
import android.media.Ringtone
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.HeadlessJsTaskService
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.data.Call
import com.reactnativetwiliophone.data.Contact
import com.reactnativetwiliophone.data.NotificationHelper
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver
import com.reactnativetwiliophone.log


class ViewService : ViewServiceConfig(), Logger by LoggerImpl() {

  var mNotificationManager: NotificationManager? = null
  private var notificationHelper: NotificationHelper? = null

  private var mBinder: IBinder = LocalBinder()
  private var mIntent: Intent? = null
  private var extras: Bundle? = null
  private var mBound: Boolean = false
  private var isStarted: Boolean? = false
  private var isServiceStarted = false
  private var mStartId = 0
  var binder: Binder? = null
  var callerName: String? = ""
  var callSid: String? = "0"
  var textMessage: String? = ""
  var callerImage: String? = ""
  var backageName :String = ""

  companion object {
    @JvmStatic
    lateinit var instance: ViewService
    private const val REQUEST_CONTENT = 1
    private const val REQUEST_BUBBLE = 2
  }

  init {
    instance = this
  }

  fun doUnbindService() {
    log("ViewService ====================== doUnbindService  $mBound")
    if (mBound) {
      unbindService(connection)
      mBound = false
    }
  }

  val connection: ServiceConnection = object : ServiceConnection {
    override fun onServiceConnected(
      className: ComponentName,
      service: IBinder
    ) {
      binder = service as LocalBinder
      mBound = true
      log("ViewService ====================== onServiceConnected  $mBound")

    }

    override fun onServiceDisconnected(arg0: ComponentName) {
      mBound = false
      log("ViewService ====================== onServiceDisconnected  $mBound")

    }
  }

  inner class LocalBinder : Binder() {
    fun getService(): ViewService = this@ViewService
  }

  override fun onBind(intent: Intent?): IBinder? {
    stopForeground(true)
    log("ViewService ====================== onBind  service")
    return mBinder
  }

  override fun onRebind(intent: Intent?) {
    log("ViewService ====================== onRebind  service")
    stopForeground(true)
  }

  override fun onUnbind(intent: Intent?): Boolean {
    log("ViewService ====================== onUnbind  service ")
    stopForeground(true)
    return true
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun startViewForeground() {
    notificationHelper= NotificationHelper(this)
    val channelId = if (isHigherThanAndroid8()) {
       notificationHelper!!.setUpNotificationChannels(Const.INCOMING_CALL_CHANNEL_ID, Const.INCOMING_CALL_CHANNEL_NAME)
    } else {
      // In earlier version, channel ID is not used
      // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
      ""
    }
    val notification = setupNotificationBuilder(channelId)
    log("startViewForeground")

    startForeground(Const.NOTIFICATION_ID, notification)
    this.isStarted = true

  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    notificationHelper= NotificationHelper(this)

    val startIntent = Intent(this, RingtonePlayingService::class.java)
    startService(startIntent)

    val prefs: SharedPreferences = this.getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE)
     backageName = prefs.getString(Const.BAKAGE_NAME, "com.iriscrm").toString() //"No name defined" is the default value.
    log("======================== onStartCommand get Bakage name =====================${backageName}")

    if (intent != null) {
      //val action = intent.action
      mIntent = intent
      mStartId = startId
      extras = intent.extras
      callerName=  extras!!.getString(Const.EXTRA_CALLER_NAME)
      callSid=  extras!!.getString(Const.EXTRA_CALL_SID)
      callerImage= extras!!.getString(Const.EXTRA_CALLER_IMAGE)
      textMessage=  extras!!.getString(Const.EXTRA_TXT_MESSAGE)
      log("onStartCommand extras callerName $callerName")

      // if (action === Actions.STOP.name) {
      //  stopSelf()
      //  stopSelfResult(startId)
      // } else {
      if (isDrawOverlaysPermissionGranted()) {
        setupViewAppearance()
        if (isHigherThanAndroid8()) {
          if (this.isStarted == false) {
            log("onStartCommand startViewForeground")

            startViewForeground()
          }
        }

      } else throw PermissionDeniedException()
      //  }
    }
    return START_STICKY;
  }

  override fun onCreate() {
    super.onCreate()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if(extras!=null){
        startViewForeground()

      }
    }
  }

  override fun setupCallView(action: CallView.Action): CallView.Builder? {
    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout = inflater.inflate(R.layout.call_view, null)
    val textView: TextView = layout.findViewById(R.id.callerNameV)
    if (callerName != ""||callerName != null) {
      textView.text = callerName
    }
    val imgDeclineBtn: ImageButton = layout.findViewById(R.id.imgDecline)
    val imgAnswerBtn: ImageButton = layout.findViewById(R.id.imgAnswer)
    imgAnswerBtn.setOnClickListener { v: View? ->
      extras?.let {
        handleIntent(
          it,
          Const.EXTRA_ANSWER
        )
      }
    }

    imgDeclineBtn.setOnClickListener { v: View? ->
      extras?.let {
        handleIntent(
          it,
          Const.EXTRA_REJECT
        )
      }
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
          isServiceStarted = true
        }
      })
  }


  fun setupNotificationBuilder(channelId: String): Notification {

    val contact: Contact = object : Contact(callSid!!, "contact_$callSid", callerName!!,textMessage!!, callerImage!!) {
      override fun reply(text: String) = buildReply().apply { this.text = textMessage }
    }

    log("setupNotificationBuilder callerName ${contact.name}")
    log("setupNotificationBuilder id ${contact.id}  callSid =${callSid}")
    log("setupNotificationBuilder scld ${contact.scId}")
    log("setupNotificationBuilder callerImage ${contact.icon}")
    log("setupNotificationBuilder textMessage ${contact.message}")
    val call  = Call(contact)
    return notificationHelper!!.setupNotificationBuilder(call,true,true,channelId)

  }

  override fun onDestroy() {
    log("====================== onDestroy  ViewService")
    stopService(this)
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    stopService(this)
    log("======================== onTaskRemoved =====================")
    super.onTaskRemoved(rootIntent)
  }


  fun stopService(context: Context) {
    try {
      val stopIntent = Intent(this, RingtonePlayingService::class.java)
      stopService(stopIntent)

      notificationHelper?.removeShortcut("contact_$callSid")
      notificationHelper?.removeAllShortcuts();
      doUnbindService()
      //stopSelfResult(mStartId);
      tryStopService();
      val closeIntent = Intent(context, ViewService::class.java)
      stopService(closeIntent)
    } catch (e: Exception) {
      tryStopService()
    }
    isServiceStarted = false
  }

  fun handleIntent(extras: Bundle, type: String?) {

    try {
      val pendingFlags: Int
      pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_IMMUTABLE
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }
      val appIntent = Intent(this, Class.forName(packageName + ".MainActivity"))
      appIntent.action = Actions.STOP.name
      val contentIntent = PendingIntent.getActivity(
        this,
        0,
        appIntent,
        pendingFlags
      )
      contentIntent.send()
      val headlessIntent = Intent(
        this,
        NotificationsHeadlessReceiver::class.java
      )
      extras.putString(Const.EXTRA_ACTION, type)
      headlessIntent.putExtra(Const.EXTRA_NOTIFIER, extras)
      val name = startService(headlessIntent)
      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(this)
      }
      log("finish service A")
      stopService(this)
    } catch (e: java.lang.Exception) {
      log("Exception =$e")
      stopService(Intent(this, ViewService::class.java))
    }
  }

  @Deprecated("this function may not work properly", ReplaceWith("true"))
  open fun setLoggerEnabled(): Boolean = true

  // helper --------------------------------------------------------------------------------------

  @SuppressLint("WrongConstant")
  @TargetApi(Build.VERSION_CODES.O)
  @RequiresApi(Build.VERSION_CODES.O)
  private fun createNotificationChannel(
    channelId: String,
    channelName: String
  ): String {
    val channel = NotificationChannel(
      channelId,
      channelName, NotificationManager.IMPORTANCE_LOW
    )
    channel.lightColor = Color.BLUE
    channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
    mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    mNotificationManager!!.createNotificationChannel(channel)
    return channelId
  }


  @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
  private fun isHigherThanAndroid8() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

  fun cancelPushNotification(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(Const.NOTIFICATION_ID)
  }
}

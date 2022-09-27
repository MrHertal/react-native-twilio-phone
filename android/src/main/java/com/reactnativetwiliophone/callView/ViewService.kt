package com.reactnativetwiliophone.callView

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.RemoteViews
import android.widget.TextView
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.facebook.react.HeadlessJsTaskService
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver
import com.reactnativetwiliophone.log
import java.util.*


class ViewService : ViewServiceConfig(), Logger by LoggerImpl() {

  var mNotificationManager: NotificationManager? = null
  var  mBinder: IBinder = LocalBinder()
  private var mIntent: Intent? = null
  private var extras: Bundle? = null
   private var mNotification: Notification? = null
  var mBound:Boolean=false
  var notificationLayout: RemoteViews ? = null

  var isStarted: Boolean? = false
  // private PowerManager = PowerManager.WakeLock;
  private var isServiceStarted = false
   private var isServiceStoped = false

   private var mStartId = 0
  var binder: Binder? = null
   companion object {
     @JvmStatic lateinit var instance: ViewService
   }

   init {
     instance = this
   }

   fun doBindService(intent: Intent) {
     val mIntent = intent
     if(mIntent !=null){
       val binded=bindService(
         intent,
         connection, BIND_AUTO_CREATE
       )
       if(binded != null){
         log("doBindService ====================== mIsBound  =$binded")
         mBound=binded

       }
     }
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
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      val binder = service as LocalBinder
      //mService = binder.getService()
      mBound = true
      log("ViewService ====================== onServiceConnected  $mBound")

    }

    override fun onServiceDisconnected(arg0: ComponentName) {
      mBound = false
      log("ViewService ====================== onServiceDisconnected  $mBound")

    }
  }
  inner class LocalBinder : Binder() {
    // Return this instance of LocalService so clients can call public methods
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
   //tryStopService()
    return true
  }
    // overridable func ----------------------------------------------------------------------------



     @RequiresApi(Build.VERSION_CODES.O)
     fun startViewForeground() {
        val channelId =
            createNotificationChannel(Const.INCOMING_CALL_CHANNEL_ID,Const.INCOMING_CALL_CHANNEL_NAME)

        mNotification = setupNotificationBuilder()
      log( "startViewForeground")

      startForeground(Const.NOTIFICATION_ID, mNotification)
       this.isStarted = true

    }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    /*  if (intent.getAction().equals("StopService")) {
      stopForeground(true);
      Log.d("callMyService", "StopService onStartCommand");
      stopSelf();
      return START_NOT_STICKY;
    }*/

    if (intent != null) {
      log("onStartCommand action=" + intent.action)
      val action = intent.action
      mIntent = intent
      mStartId = startId
      extras = intent.extras
      log("using an intent with action $action")
      if (action === Actions.STOP.name) {
       // stopService()
        stopSelf()
        stopSelfResult(startId)
        //tryStopService()
      //  val intents = Intent("com.iriscrm.ACTION_STOP")
      //  LocalBroadcastManager.getInstance(this).sendBroadcast(intents);
      //  log("sent intent with action -> $action")

      } else {
        if (isDrawOverlaysPermissionGranted()) {

          setupViewAppearance()

          if (isHigherThanAndroid8()) {
            if(this.isStarted == false){
              startViewForeground()
            }
          }

        } else throw PermissionDeniedException()

        startService()

      }
    }
    return START_STICKY;
  }

  override fun setupCallView(action: CallView.Action): CallView.Builder? {
    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout = inflater.inflate(R.layout.call_view, null)
    log("setupCallView ======================   service isStarted ="+isStarted)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startViewForeground()
    }
    val textView: TextView = layout.findViewById(R.id.callerNameV)
    if (extras != null) {
      textView.text = extras!!.getString(Const.CALLER_NAME)
    }
    val imgDeclineBtn: ImageButton = layout.findViewById(R.id.imgDecline)
    val imgAnswerBtn: ImageButton = layout.findViewById(R.id.imgAnswer)
    imgAnswerBtn.setOnClickListener { v: View? ->
      extras?.let {
       // action.popCallView()
        handleIntent(
          it,
          Const.ANSWER
        )
      }
    }

    imgDeclineBtn.setOnClickListener { v: View? ->
      extras?.let {
        //action.popCallView()
        handleIntent(
          it,
          Const.REJECT
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
          isServiceStarted=true
        }
      })
  }


   fun setupNotificationBuilder(): Notification {
    val notificationIntent = Intent(this, Class.forName("com.iriscrm.MainActivity")::class.java)
     val pendingFlags: Int
     pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
       PendingIntent.FLAG_IMMUTABLE
     } else {
       PendingIntent.FLAG_UPDATE_CURRENT
     }
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags )
    mNotification = NotificationCompat.Builder(this, Const.INCOMING_CALL_CHANNEL_ID)
      .setOngoing(true)
      .setSmallIcon(R.drawable.logo_round)
      .setContentTitle("Incomming Call")
      .setTicker("Call_STATUS")
      .setPriority(PRIORITY_MIN)
      .setCategory(Notification.CATEGORY_SERVICE)
      .setContentIntent(pendingIntent)
      .build()

    return mNotification as Notification

  }

  override fun onDestroy() {
    log("====================== onDestroy  ViewService")
     stopService()
  }
  override fun onTaskRemoved(rootIntent: Intent?) {
    stopService()
    log("======================== onTaskRemoved =====================")
    super.onTaskRemoved(rootIntent)
  }


  private  fun startService() {
    if (isServiceStarted) {
      log("Starting the foreground service task")
      return
    }
    // Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show();
    isServiceStarted = true
    setServiceState(this, ServiceState.STARTED)
  }

  private  fun stopService() {
      try {

        log("================= ON Stopping the foreground service =================")

        doUnbindService()
        log("================= ON Stopping goo ================= doUnbindService")
        Thread.currentThread().interrupt();
        log("================= ON Stopping goo ================= interrupt")
        mNotificationManager!!.cancel(Const.NOTIFICATION_ID)
        log("================= ON Stopping goo ================= cancel mNotificationManager")
        stopForeground(STOP_FOREGROUND_REMOVE)
        log("================= ON Stopping goo ================= STOP_FOREGROUND_REMOVE")
        stopForeground(true)
        log("================= ON Stopping goo ================= stopForeground")

        stopSelfResult(mStartId);
        log("================= ON Stopping goo ================= stopSelfResult")

        tryStopService();
        log("================= ON Stopping goo ================= tryStopService")
        val closeIntent = Intent(this, this::class.java)
        closeIntent.action = Actions.STOP.name
        stopService(closeIntent)
        log("================= ON Stopping goo ================= stopService")

        //val cancelPendingIntent = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        // cancelPendingIntent.send()
      } catch (e: Exception) {
        tryStopService()
        log("========= ON Service stopping Exception ===========\n mStartId:$mStartId\n$e")
      }
      isServiceStarted = false
      setServiceState(this, ServiceState.STOPPED)

  }

   fun handleIntent(extras: Bundle, type: String?) {
    Log.d("callMyService", "callExtra not null")
    try {
      val pendingFlags: Int
      pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_IMMUTABLE
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }
      //  if(!isAppRunning()){
      val appIntent = Intent(this, Class.forName("com.iriscrm.MainActivity"))
      appIntent.action = Actions.STOP.name
      val contentIntent = PendingIntent.getActivity(
        this,
        0,
        appIntent,
        pendingFlags
      )
      contentIntent.send()
      //  }
      val headlessIntent = Intent(
        this,
        NotificationsHeadlessReceiver::class.java
      )
      extras.putString(Const.ACTION, type)
      headlessIntent.putExtra(Const.EXTRA_NOTIFIER, extras)
      val name = startService(headlessIntent)
      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(this)
      }
      log("finish service A")
      stopService()
      // viewUtils.actionOnService(Actions.STOP,this,"");
      // android.os.Process.killProcess(android.os.Process.myPid());
    } catch (e: java.lang.Exception) {
      log("Exception =$e")
      stopService(Intent(this, ViewService::class.java))
      //viewUtils.actionOnService(Actions.STOP,this,"");
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

}

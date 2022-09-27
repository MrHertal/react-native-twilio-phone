package com.reactnativetwiliophone.callView

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.log

abstract class ViewServiceConfig : Service() {

    private var callView: CallView? = null

    // lifecycle -----------------------------------------------------------------------------------

    override fun onDestroy() {
      log("====================== onDestroy  ViewServiceConfig")

        tryRemoveAllView()
        super.onDestroy()
    }

    // override ------------------------------------------------------------------------------------

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

      return START_STICKY
  }

    open fun setupCallView(action: CallView.Action): CallView.Builder? = null

    // public func ---------------------------------------------------------------------------------
    protected fun setupViewAppearance() {

        callView = setupCallView(customCallViewListener)
            ?.build()



       // onMainThread {
          tryShowCallView()

     // }
    }

  protected fun isAppRunning(): Boolean {
    val activityManager: ActivityManager =
     this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val procInfos: List<ActivityManager.RunningAppProcessInfo> =
      activityManager.getRunningAppProcesses()
    if (procInfos != null) {
      for (processInfo in procInfos) {
        if (processInfo.processName.equals(this.packageName)) {
          log( "app ViewServiceConfig running = true");
          return true
        }
      }
    }
    log( "app ViewServiceConfig NOT running = false");
    return false
  }
    // private func --------------------------------------------------------------------------------

    private val customCallViewListener = object : CallView.Action {

        override fun popCallView() {
            tryShowCallView()
        }
    }

    private fun tryNavigateToCallView() {

        tryShowCallView()
            .onComplete {
            }.onError {
                throw NullViewException("you DID NOT override expandable view")
            }
    }

    public fun tryStopService() {
        //ViewService().doUnbindService()
        tryRemoveAllView()
        stopForeground(true)
        stopSelf()
    }

    public fun tryRemoveAllView() {
        tryRemoveCallView()
    }


    // shorten -------------------------------------------------------------------------------------

    private fun tryRemoveCallView() = logIfError {
      callView?.remove()
    }

    private fun tryShowCallView() = logIfError {
        callView!!.show()
    }



}


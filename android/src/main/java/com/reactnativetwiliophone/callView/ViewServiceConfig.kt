package com.reactnativetwiliophone.callView

import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

abstract class ViewServiceConfig : Service() {

    private var callView: CallView? = null

    // lifecycle -----------------------------------------------------------------------------------

    override fun onDestroy() {
        tryRemoveAllView()
        super.onDestroy()
    }

    // override ------------------------------------------------------------------------------------


    open fun setupCallView(action: CallView.Action): CallView.Builder? = null

    // public func ---------------------------------------------------------------------------------
    protected fun setupViewAppearance() {

        callView = setupCallView(customCallViewListener)
            ?.build()

        onMainThread {
          tryShowCallView()

      }
    }

  protected fun isAppRunning(): Boolean {
    val activityManager: ActivityManager =
     this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val procInfos: List<ActivityManager.RunningAppProcessInfo> =
      activityManager.getRunningAppProcesses()
    if (procInfos != null) {
      for (processInfo in procInfos) {
        if (processInfo.processName.equals(this.packageName)) {
          Log.v("callMyService", "app ViewServiceConfig running = true");
          return true
        }
      }
    }
    Log.v("callMyService", "app ViewServiceConfig NOT running = false");
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

        tryRemoveAllView()
        stopSelf()
    }

    public fun tryRemoveAllView() {
        tryRemoveCallView()
    }


    // shorten -------------------------------------------------------------------------------------

    private fun tryRemoveCallView() = logIfError {
        callView!!.remove()
    }

    private fun tryShowCallView() = logIfError {
        callView!!.show()
    }


}


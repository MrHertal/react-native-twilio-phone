package com.reactnativetwiliophone.callView

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.reactnativetwiliophone.log


open class BaseView(
  context: Context
) : Logger by LoggerImpl() {

  var windowManager: WindowManager? = null
  var windowParams: WindowManager.LayoutParams? = null
  var mContext: Context? = null

  init {
    windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    windowParams = WindowManager.LayoutParams()
    mContext = context
  }

  // public --------------------------------------------------------------------------------------

  protected fun show(view: View) {
    logIfError {

      if (view.getParent() != null) {
        Log.v("callMyService", "REMOVE! view in BaseView view.getParent not null");
        windowManager!!.removeView(view)
      }
      windowManager!!.addView(view, windowParams)

    }
  }

  fun isAppRunning(): Boolean {
    val activityManager: ActivityManager =
      mContext?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val procInfos: List<ActivityManager.RunningAppProcessInfo> =
      activityManager.getRunningAppProcesses()
    if (procInfos != null) {
      for (processInfo in procInfos) {
        if (processInfo.processName.equals(mContext?.packageName)) {
          log("app BaseView running = true")

          return true
        }
      }
    }
    log("app BaseView NOT running = false")

    return false
  }

  protected fun remove(view: View) {
    tryOnly {
      if (view.visibility == View.VISIBLE) {
        View.INVISIBLE
        log("remove  on View.GONE")

      }
      windowManager!!.removeView(view)
      log("remove  on removeView")

    }
  }

  protected fun update(view: View) {
    logIfError {
      windowManager!!.updateViewLayout(view, windowParams)
    }
  }


  // override ------------------------------------------------------------------------------------

  open fun setupLayoutParams() {

    logIfError {

      windowParams!!.apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        gravity = Gravity.CENTER
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
          // for android version lower than 8
          WindowManager.LayoutParams.TYPE_PHONE
        }
      }

    }
  }

}

package com.reactnativetwiliophone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.content.Context.BIND_IMPORTANT
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReadableMap
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.callView.ServiceState
import com.reactnativetwiliophone.callView.ViewService
import com.reactnativetwiliophone.callView.getServiceState
import com.reactnativetwiliophone.log


object ViewUtils {

  @SuppressLint("SuspiciousIndentation")
  fun showCallView(context: Context, data: ReadableMap) {
    val callerName = data.getString(Const.CALLER_NAME)

    if (checkFloatingWindowPermission(context)) {
      /* context.packageManager?.setComponentEnabledSetting(
         cmp, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
         PackageManager.DONT_KILL_APP
       )*/
      if (callerName != null) {
        val intent = Intent(context, ViewService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
        intent.putExtra(Const.CALLER_NAME, callerName)
        intent.action = Actions.START.name
        //context.startService(intent);
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, ViewService().connection, 0);
       // ViewService().doBindService(intent)
      }
    }

  }



  public fun actionOnService(action: Actions, context: Context, callerName: String) {

    //if(checkServiceRunning(CallViewService::class.java,context)){
     // log("============================ service was connected will closed =================");
    //  context.stopService(Intent(context.applicationContext, CallViewService::class.java))
   // }
 //  if (ApplicationLifecycleHandler.get().getNumStarted() > 0) {
     // log("Starting RUNNING")

      if (getServiceState(context) == ServiceState.STOPPED && action == Actions.STOP) return
     // log("Starting isAppRunning")
      val intent = Intent(context, ViewService::class.java)
      intent.putExtra(Const.CALLER_NAME, callerName)
      intent.action = action.name
    //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
     //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
     //intent.setFlags(DriveFile.MODE_READ_ONLY);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
        //context.bindService(intent, callview.mConnection, 0);
      } else {
        context.startService(intent)
       // context.bindService(intent, callview.mConnection, 0);
      }
  /*} else {

      log("Starting KILLED")

      val intent2 = Intent(context, CallViewInKilled::class.java)
      intent2.putExtra(Const.CALLER_NAME, callerName)
     intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
     intent2.addFlags(FLAG_ACTIVITY_NO_HISTORY)

      context.startService(intent2)

    }*/

  }
  fun checkServiceRunning(serviceClass: Class<*>,context: Context) : Boolean  {
    log("======================= call check ServiceIfRunning 222=================");
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
      if (serviceClass.name == service.service.className) {
        return true
      }
    }
    return false
  }
  fun isServiceRunning(serviceClassName: String?,context: Context): Boolean {
    val activityManager =
      context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val services: List<ActivityManager.RunningServiceInfo> = activityManager.getRunningServices(Int.MAX_VALUE)
    for (runningServiceInfo in services) {
      if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
        return true
      }
    }
    return false
  }
  private fun checkFloatingWindowPermission(context: Context): Boolean {
    //val foregroud: Boolean = ForegroundCheckTask()!.execute(context).get()

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (Settings.canDrawOverlays(context)) {
        true
      } else {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        } else {
          TODO("VERSION.SDK_INT < M")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
        // showPermissionDialog(context)
        false
      }
    } else {
      true
    }
  }

  public fun checkWindowsDrawWithDialogPermission(activity: Activity, context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (Settings.canDrawOverlays(context)) {
        true
      } else {
        showPermissionDialog(activity, context)
        false
      }
    } else {
      true
    }
  }


  private fun showPermissionDialog(activity: Activity, context: Context) {
    val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
    builder.setTitle("SYSTEM ALERT WINDOW Permission Required")
    builder.setMessage(
      "To see incoming calls when the app close, please Enable action to manage overly permission now?"
    )
    builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
      override fun onClick(dialogInterface: DialogInterface, i: Int) {
        Toast.makeText(activity, R.string.permission_floating_window, Toast.LENGTH_SHORT)
          .show()
        dialogInterface.dismiss()
      }
    })
    builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
      override fun onClick(dialogInterface: DialogInterface?, i: Int) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        } else {
          TODO("VERSION.SDK_INT < M")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent)
      }
    })
    val alertDialog: android.app.AlertDialog? = builder.create()
    alertDialog!!.setCancelable(false)
    alertDialog.show()
  }
}
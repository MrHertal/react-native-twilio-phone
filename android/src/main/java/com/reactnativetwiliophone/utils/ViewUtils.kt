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
    val callSid = data.getString(Const.CALL_SID)

    if (checkFloatingWindowPermission(context)) {
      if (callerName != null) {
        val intent = Intent(context, ViewService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
        intent.putExtra(Const.CALLER_NAME, callerName)
        intent.putExtra(Const.CALL_SID, callSid)
        intent.action = Actions.START.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(intent)
        } else {
          context.startService(intent)
        }
        context.bindService(intent, ViewService().connection, 0);
      }
    }
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

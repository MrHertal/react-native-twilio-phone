package com.reactnativetwiliophone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.facebook.react.bridge.ReadableMap
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.callView.ViewService
import com.reactnativetwiliophone.log


object ViewUtils {
 var serviceIntent: Intent? = null

  @SuppressLint("SuspiciousIndentation")
  fun showCallView(context: Context, data: ReadableMap) {
    val callerName = data.getString(Const.CALLER_NAME)
    val callSid = data.getString(Const.CALL_SID)
    log("---------------------- showCallView start ------------------------")

    if (checkFloatingWindowPermission(context)) {
      if (callerName != null) {
        serviceIntent= Intent(context, ViewService::class.java)
        serviceIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        serviceIntent!!.addFlags(FLAG_ACTIVITY_NO_HISTORY)
        serviceIntent!!.putExtra(Const.CALLER_NAME, callerName)
        serviceIntent!!.putExtra(Const.CALL_SID, callSid)
        serviceIntent!!.action = Actions.START.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          context.startForegroundService(serviceIntent)
        } else {
          context.startService(serviceIntent)
        }
        context.bindService(serviceIntent, ViewService().connection, 0);
      }
    }
  }

  fun stopService(context: Context) {
    context.stopService(serviceIntent)
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

package com.reactnativetwiliophone.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.facebook.react.bridge.ReadableMap
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.callView.CallViewService


object ViewUtils {


  fun showCallView(context: Context, data: ReadableMap) {
    val callerName = data.getString(Const.CALLER_NAME)

    if (checkFloatingWindowPermission(context)) {
      val intent = Intent(context, CallViewService::class.java)
      intent.putExtra(Const.CALLER_NAME,callerName)
     // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startService(intent)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }
      //activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION)

  }
  private fun checkFloatingWindowPermission(context: Context): Boolean {
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
  public fun checkWindowsDrawWithDialogPermission(activity: Activity,context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (Settings.canDrawOverlays(context)) {
        true
      } else {
        showPermissionDialog(activity,context)
        false
      }
    } else {
      true
    }
  }

  private fun showPermissionDialog(activity: Activity,context: Context) {
    val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
    builder.setTitle("Permission Required")
    builder.setMessage(
      "To enable call view to when app close, on the home device screen and over apps, please Enable action to manage overly permission now?"
    )
    builder.setNegativeButton("No", object : DialogInterface.OnClickListener {
      override  fun onClick(dialogInterface: DialogInterface, i: Int) {
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

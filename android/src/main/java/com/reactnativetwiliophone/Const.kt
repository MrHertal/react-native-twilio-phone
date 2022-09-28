package com.reactnativetwiliophone

import android.os.Build
import android.util.Log

fun log(msg: String) {
  Log.d("App_Log VIEW-SERVICE", msg)
}

class StaticConst {
  companion object {
    var IS_RUNNING = false
  }
}
object Const {
  const val ACTION_START_LISTEN = "action_start_listen"
  const val ACTION_STOP_LISTEN = "action_stop_listen"

  const val INCOMING_CALL_CHANNEL_NAME = "incoming_call_channel_name"
  const val SYSTEM_OVERLAY_WINDOW = "android.permission.SYSTEM_OVERLAY_WINDOW"
  const val INCOMING_CALL_CHANNEL_ID = "incoming_call_channel_id"
  const val NOTIFICATION_ID = 58764854
  const val EXTRA_NOTIFIER = "com.reactnativetwiliophone.notifier"
  const val ACTIVITY_NAME = "activityName"
  const val ACTION = "action"
  const val REJECT = "reject"
  const val ANSWER = "answer"
  const val CALLER_NAME = "callerName"
  const val CALL_SID = "callSid"
  val REQ_CODE_PERMISSION_SYSTEM_OVERLAY_WINDOW=11

  const val KEY_STRING_VIDEO_URL = "video_url"
  const val KEY_INT_CLOSE_BUTTON_COLOR = "close_button_color"
  const val KEY_INT_CLOSE_BUTTON_BG_COLOR = "close_button_background"
  const val KEY_STRING_NOTIFICATION_TITLE = "notification_title"
  const val KEY_STRING_NOTIFICATION_DESCRIPTION = "notification_description"
  const val KEY_INT_NOTIFICATION_ICON = "notification_icon"
  var IS_LOGGER_ENABLED = true

  val ANDROID_11 = Build.VERSION_CODES.R
  val ANDROID_10 = Build.VERSION_CODES.Q
  val ANDROID_9 = Build.VERSION_CODES.P
  val ANDROID_8 = Build.VERSION_CODES.O
  val ANDROID_7 = 24
  val ANDROID_6 = 23
  val ANDROID_5 = 21
}

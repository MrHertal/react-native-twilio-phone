package com.reactnativetwiliophone

import android.os.Build

object Const {
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
  val REQ_CODE_PERMISSION_SYSTEM_OVERLAY_WINDOW=11
  var IS_LOGGER_ENABLED = true

  val ANDROID_11 = Build.VERSION_CODES.R
  val ANDROID_10 = Build.VERSION_CODES.Q
  val ANDROID_9 = Build.VERSION_CODES.P
  val ANDROID_8 = Build.VERSION_CODES.O
  val ANDROID_7 = 24
  val ANDROID_6 = 23
  val ANDROID_5 = 21
}

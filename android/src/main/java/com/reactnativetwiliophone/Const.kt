package com.reactnativetwiliophone

import android.util.Log

fun log(msg: String) {
  Log.d("TwilioPhone_log", msg)
}

class StaticConst {
  companion object {
    var IS_RUNNING = false
  }
}

object Const {

  //general---------------------------------------
  const val MODULE_NAME = "TwilioPhone"
  const val INCOMING_CALL_CHANNEL_NAME = "incoming_call_channel_name"
  const val INCOMING_CALL_CHANNEL_ID = "incoming_call_channel_id"
  const val NOTIFICATION_ID = 58764854
  const val EXTRA_NOTIFIER = "com.reactnativetwiliophone.notifier"

  //prefs---------------------------------------

  const val PREFS_NAME = "call_prefs"
  const val BAKAGE_NAME = "backage_name"

  //extras---------------------------------------
  const val EXTRA_ACTION = "action"
  const val EXTRA_REJECT = "reject"
  const val EXTRA_ANSWER = "answer"
  const val EXTRA_CALLER_NAME = "callerName"
  const val EXTRA_CALL_SID = "callSid"
  const val EXTRA_CALLER_IMAGE = "callerImage"
  const val EXTRA_TXT_MESSAGE = "callerMsg"

  //events--------------------------------------
  const val REGISTER_SUCCESS = "RegistrationSuccess"
  const val REGISTER_FAILURE = "RegistrationFailure"
  const val CALL_INVITE = "CallInvite"
  const val UNREGISTER_SUCCESS = "UnregistrationSuccess"
  const val UNREGISTER_FAILURE = "UnregistrationFailure"
  const val CANCELLED_CALL_INVITE = "CancelledCallInvite"
  const val CALL_RINGING = "CallRinging"
  const val CALL_CONNECT_FAILURE = "CallConnectFailure"
  const val CALL_CONNECTED = "CallConnected"
  const val CALL_CONNECTING = "CallReconnecting"
  const val CALL_CONNECTED_ERROR = "CallDisconnectedError"
  const val CALL_DISCONNECTED = "CallDisconnected"
  const val FROM = "from"
  const val CLIENT = "client:"
  const val ERROR_CODE = "errorCode"
  const val ERROR_MESSAGE = "errorMessage"
  var IS_LOGGER_ENABLED = true

}

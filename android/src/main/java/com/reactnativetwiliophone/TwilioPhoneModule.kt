package com.reactnativetwiliophone

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.twilio.voice.*

class TwilioPhoneModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val tag = "TwilioPhone"

  private var activeCallInvites = mutableMapOf<String, CallInvite>()
  private var activeCalls = mutableMapOf<String, Call>()

  private var callListener = callListener()

  private var audioManager: AudioManager =
    reactContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

  override fun getName(): String {
    return "TwilioPhone"
  }

  @ReactMethod
  fun register(accessToken: String, deviceToken: String) {
    Log.i(tag, "Registering")

    Voice.register(
      accessToken,
      Voice.RegistrationChannel.FCM,
      deviceToken,
      object : RegistrationListener {
        override fun onRegistered(accessToken: String, fcmToken: String) {
          Log.d(tag, "Successfully registered FCM token")

          sendEvent(reactApplicationContext, "RegistrationSuccess", null)
        }

        override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) {
          Log.e(tag, "Registration error: ${error.errorCode}, ${error.message}")

          val params = Arguments.createMap()
          params.putInt("errorCode", error.errorCode)
          params.putString("errorMessage", error.message)

          sendEvent(reactApplicationContext, "RegistrationFailure", params)
        }
      })
  }

  @ReactMethod
  fun handleMessage(payload: ReadableMap) {
    Log.i(tag, "Handling message")

    val data = Bundle()

    for (entry in payload.entryIterator) {
      data.putString(entry.key, entry.value as String)
    }

    val valid = Voice.handleMessage(reactApplicationContext, data, object : MessageListener {
      override fun onCallInvite(callInvite: CallInvite) {
        Log.d(tag, "Call invite received")

        activeCallInvites[callInvite.callSid] = callInvite

        val from = callInvite.from ?: ""

        val params = Arguments.createMap()
        params.putString("callSid", callInvite.callSid)
        params.putString("from", from.replace("client:", ""))

        sendEvent(reactApplicationContext, "CallInvite", params)
      }

      override fun onCancelledCallInvite(
        cancelledCallInvite: CancelledCallInvite,
        callException: CallException?
      ) {
        Log.d(tag, "Cancelled call invite received")

        activeCallInvites.remove(cancelledCallInvite.callSid)

        val params = Arguments.createMap()
        params.putString("callSid", cancelledCallInvite.callSid)

        sendEvent(reactApplicationContext, "CancelledCallInvite", params)
      }
    })

    if (!valid) {
      Log.e(tag, "The message was not a valid Twilio Voice SDK payload")
    }
  }

  @ReactMethod
  fun acceptCallInvite(callSid: String) {
    Log.i(tag, "Accepting call invite")

    if (activeCallInvites[callSid] == null) {
      Log.e(tag, "No call invite to be accepted")
      return
    }

    val call = activeCallInvites[callSid]!!.accept(reactApplicationContext, callListener)

    activeCalls[callSid] = call
    activeCallInvites.remove(callSid)
  }

  @ReactMethod
  fun rejectCallInvite(callSid: String) {
    Log.i(tag, "Rejecting call invite")

    if (activeCallInvites[callSid] == null) {
      Log.e(tag, "No call invite to be rejected")
      return
    }

    activeCallInvites[callSid]!!.reject(reactApplicationContext)

    activeCallInvites.remove(callSid)
  }

  @ReactMethod
  fun disconnectCall(callSid: String) {
    Log.i(tag, "Disconnecting call")

    if (activeCalls[callSid] == null) {
      Log.e(tag, "No call to be disconnected")
      return
    }

    activeCalls[callSid]!!.disconnect()
  }

  @ReactMethod
  fun endCall(callSid: String) {
    Log.i(tag, "Ending call")

    if (activeCallInvites[callSid] != null) {
      activeCallInvites[callSid]!!.reject(reactApplicationContext)
      activeCallInvites.remove(callSid)
      return
    }

    if (activeCalls[callSid] != null) {
      activeCalls[callSid]!!.disconnect()
      return
    }

    Log.e(tag, "Unknown sid to perform end-call action with")
  }

  @ReactMethod
  fun toggleMuteCall(callSid: String, mute: Boolean) {
    Log.i(tag, "Toggling mute call")

    val activeCall = activeCalls[callSid] ?: return

    activeCall.mute(mute)
  }

  @ReactMethod
  fun toggleHoldCall(callSid: String, hold: Boolean) {
    Log.i(tag, "Toggling hold call")

    val activeCall = activeCalls[callSid] ?: return

    activeCall.hold(hold)
  }

  @ReactMethod
  fun toggleSpeaker(speakerOn: Boolean) {
    Log.i(tag, "Toggling speaker")
    audioManager.isSpeakerphoneOn = speakerOn
  }

  @ReactMethod
  fun sendDigits(callSid: String, digits: String) {
    Log.i(tag, "Sending digits")

    val activeCall = activeCalls[callSid] ?: return

    activeCall.sendDigits(digits)
  }

  @ReactMethod
  fun startCall(accessToken: String, params: ReadableMap) {
    Log.i(tag, "Starting call")

    val connectParams = mutableMapOf<String, String>()

    for (entry in params.entryIterator) {
      connectParams[entry.key] = entry.value as String
    }

    val connectOptions = ConnectOptions.Builder(accessToken)
      .params(connectParams)
      .build()

    Voice.connect(reactApplicationContext, connectOptions, callListener)
  }

  @ReactMethod
  fun unregister(accessToken: String, deviceToken: String) {
    Log.i(tag, "Unregistering")

    Voice.unregister(
      accessToken,
      Voice.RegistrationChannel.FCM,
      deviceToken,
      object : UnregistrationListener {
        override fun onUnregistered(accessToken: String, fcmToken: String) {
          Log.d(tag, "Successfully unregistered FCM token")

          sendEvent(reactApplicationContext, "UnregistrationSuccess", null)
        }

        override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) {
          Log.e(tag, "Unregistration error: ${error.errorCode}, ${error.message}")

          val params = Arguments.createMap()
          params.putInt("errorCode", error.errorCode)
          params.putString("errorMessage", error.message)

          sendEvent(reactApplicationContext, "UnregistrationFailure", params)
        }
      })
  }

  @ReactMethod
  fun checkPermissions(callback: Callback) {
    Log.i(tag, "Checking permissions")

    val permissionsToRequest = mutableListOf<String>()

    val recordAudio = checkPermission(android.Manifest.permission.RECORD_AUDIO)
    if (recordAudio != "GRANTED") {
      permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
    }

    val callPhone = checkPermission(android.Manifest.permission.CALL_PHONE)
    if (callPhone != "GRANTED") {
      permissionsToRequest.add(android.Manifest.permission.CALL_PHONE)
    }

    if (permissionsToRequest.isNotEmpty()) {
      currentActivity?.let {
        ActivityCompat.requestPermissions(
          it,
          permissionsToRequest.toTypedArray(),
          1
        )
      }
    }

    val permissions = Arguments.createMap()
    permissions.putString("RECORD_AUDIO", recordAudio)
    permissions.putString("CALL_PHONE", callPhone)

    callback(permissions)
  }

  private fun sendEvent(
    reactContext: ReactContext,
    eventName: String,
    params: WritableMap?
  ) {
    reactContext
      .getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private fun callListener(): Call.Listener {
    return object : Call.Listener {
      /*
       * This callback is emitted once before the Call.Listener.onConnected() callback when
       * the callee is being alerted of a Call. The behavior of this callback is determined by
       * the answerOnBridge flag provided in the Dial verb of your TwiML application
       * associated with this client. If the answerOnBridge flag is false, which is the
       * default, the Call.Listener.onConnected() callback will be emitted immediately after
       * Call.Listener.onRinging(). If the answerOnBridge flag is true, this will cause the
       * call to emit the onConnected callback only after the call is answered.
       * See answeronbridge for more details on how to use it with the Dial TwiML verb. If the
       * twiML response contains a Say verb, then the call will emit the
       * Call.Listener.onConnected callback immediately after Call.Listener.onRinging() is
       * raised, irrespective of the value of answerOnBridge being set to true or false
       */
      override fun onRinging(call: Call) {
        Log.d(tag, "Call did start ringing")
        /*
         * When [answerOnBridge](https://www.twilio.com/docs/voice/twiml/dial#answeronbridge)
         * is enabled in the <Dial> TwiML verb, the caller will not hear the ringback while
         * the call is ringing and awaiting to be accepted on the callee's side. The application
         * can use the `SoundPoolManager` to play custom audio files between the
         * `Call.Listener.onRinging()` and the `Call.Listener.onConnected()` callbacks.
         */

        activeCalls[call.sid!!] = call

        val params = Arguments.createMap()
        params.putString("callSid", call.sid)

        sendEvent(reactApplicationContext, "CallRinging", params)
      }

      override fun onConnectFailure(call: Call, error: CallException) {
        Log.e(tag, "Call failed to connect: ${error.errorCode}, ${error.message}")

        val params = Arguments.createMap()
        params.putString("callSid", call.sid)
        params.putInt("errorCode", error.errorCode)
        params.putString("errorMessage", error.message)

        sendEvent(reactApplicationContext, "CallConnectFailure", params)
      }

      override fun onConnected(call: Call) {
        Log.d(tag, "Call did connect")

        val params = Arguments.createMap()
        params.putString("callSid", call.sid)

        sendEvent(reactApplicationContext, "CallConnected", params)
      }

      override fun onReconnecting(call: Call, error: CallException) {
        Log.e(tag, "Call is reconnecting with error: ${error.errorCode}, ${error.message}")

        val params = Arguments.createMap()
        params.putString("callSid", call.sid)
        params.putInt("errorCode", error.errorCode)
        params.putString("errorMessage", error.message)

        sendEvent(reactApplicationContext, "CallReconnecting", params)
      }

      override fun onReconnected(call: Call) {
        Log.d(tag, "Call did reconnect")

        val params = Arguments.createMap()
        params.putString("callSid", call.sid)

        sendEvent(reactApplicationContext, "CallReconnected", params)
      }

      override fun onDisconnected(call: Call, error: CallException?) {
        val params = Arguments.createMap()
        params.putString("callSid", call.sid)

        if (error != null) {
          Log.e(tag, "Call disconnected with error: ${error.errorCode}, ${error.message}")

          params.putInt("errorCode", error.errorCode)
          params.putString("errorMessage", error.message)

          sendEvent(reactApplicationContext, "CallDisconnectedError", params)
        } else {
          Log.d(tag, "Call disconnected")

          sendEvent(reactApplicationContext, "CallDisconnected", params)
        }
      }
    }
  }

  private fun checkPermission(permission: String): String {
    val activity = currentActivity ?: return "UNKNOWN"

    return when (ContextCompat.checkSelfPermission(activity, permission)) {
      PackageManager.PERMISSION_GRANTED -> "GRANTED"
      PackageManager.PERMISSION_DENIED -> "DENIED"
      else -> {
        "UNKNOWN"
      }
    }
  }
}

package com.reactnativetwiliophone.boradcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.reactnativetwiliophone.Actions
import com.reactnativetwiliophone.callView.ServiceState
import com.reactnativetwiliophone.callView.ViewService
import com.reactnativetwiliophone.callView.getServiceState
import com.reactnativetwiliophone.log

class StopReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
      Intent(context, ViewService::class.java).also {
        if(it.action == Actions.START.name){
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("Starting the service in >=26 Mode from a StartReceiver")
            context.startForegroundService(it)
            return
          }
          log("Starting the service in < 26 Mode from a StartReceiver")
          context.startService(it)
        }else{
          log("Stop the service  from a StartReceiver")
          context.stopService(it)
        }
      }
    }
  }
}


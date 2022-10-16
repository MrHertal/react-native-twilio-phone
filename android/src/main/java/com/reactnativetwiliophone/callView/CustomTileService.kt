package com.reactnativetwiliophone.callView

import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class CustomTileService: TileService(){

  override fun onClick() {
    super.onClick()
  }

  override fun onTileRemoved() {
    super.onTileRemoved()
  }

  override fun onTileAdded() {
    super.onTileAdded()
  }

  override fun onStartListening() {
    super.onStartListening()
  }

  override fun onStopListening() {
    super.onStopListening()
  }
}

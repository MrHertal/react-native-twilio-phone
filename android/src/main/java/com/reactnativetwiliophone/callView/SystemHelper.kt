package com.reactnativetwiliophone.callView

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.view.View

// exclude view gesture on home screen -------------------------------------------------------------
private var exclusionRects: MutableList<Rect> = ArrayList()

internal fun View.updateGestureExclusion(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return


    val screenSize = ScreenInfo.getScreenSize(context.applicationContext)

    exclusionRects.clear()

    val rect = Rect(0, 0, this.width, screenSize.height)
    exclusionRects.add(rect)


    this.systemGestureExclusionRects = exclusionRects
}

fun Context.isDrawOverlaysPermissionGranted(): Boolean {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }

    return Settings.canDrawOverlays(this)
}

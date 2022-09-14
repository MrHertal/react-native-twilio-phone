package com.reactnativetwiliophone.overlyView

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.view.View
import com.reactnativetwiliophone.Const.ANDROID_10


// exclude view gesture on home screen -------------------------------------------------------------
private var exclusionRects: MutableList<Rect> = ArrayList()

internal fun View.updateGestureExclusion(context: Context) {
    if (Build.VERSION.SDK_INT < ANDROID_10) return


    val screenSize = ScreenInfo.getScreenSize(context.applicationContext)

    exclusionRects.clear()

    val rect = Rect(0, 0, this.width, screenSize.height)
    exclusionRects.add(rect)


    this.systemGestureExclusionRects = exclusionRects
}

// permission --------------------------------------------------------------------------------------

/**
 * by default, display over other app permission will be granted automatically if minor than android M
 *
 * - some MIUI devices may not work properly
 *
 * */
fun Context.isDrawOverlaysPermissionGranted(): Boolean {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }

    return Settings.canDrawOverlays(this)
}

package com.reactnativetwiliophone.callView

import android.os.Handler
import android.os.Looper


internal inline fun onMainThread(crossinline doWork: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        doWork()
    }
}

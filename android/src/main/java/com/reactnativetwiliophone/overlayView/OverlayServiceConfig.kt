package com.reactnativetwiliophone.overlyView

import android.app.Service

abstract class OverlayServiceConfig : Service() {

    private var overLyView: OverLyView? = null

    // lifecycle -----------------------------------------------------------------------------------

    override fun onDestroy() {
        tryRemoveAllView()
        super.onDestroy()
    }

    // override ------------------------------------------------------------------------------------


    open fun setupOverLyView(action: OverLyView.Action): OverLyView.Builder? = null

    // public func ---------------------------------------------------------------------------------
    protected fun setupViewAppearance() {

        overLyView = setupOverLyView(customOverLyViewListener)
            ?.build()


        onMainThread {
            tryShowOverLyView()
        }
    }


    // private func --------------------------------------------------------------------------------

    private val customOverLyViewListener = object : OverLyView.Action {

        override fun popOverLyView() {
            tryShowOverLyView()
        }
    }

    private fun tryNavigateToOverLyView() {

        tryShowOverLyView()
            .onComplete {
            }.onError {
                throw NullViewException("you DID NOT override expandable view")
            }
    }


    public fun tryStopService() {

        tryRemoveAllView()
        stopSelf()
    }

    private fun tryRemoveAllView() {
        tryRemoveOverLyView()
    }

    // shorten -------------------------------------------------------------------------------------

    private fun tryRemoveOverLyView() = logIfError {
        overLyView!!.remove()
    }

    private fun tryShowOverLyView() = logIfError {
        overLyView!!.show()
    }


}


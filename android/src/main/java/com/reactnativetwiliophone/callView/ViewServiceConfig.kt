package com.reactnativetwiliophone.callView

import android.app.Service

abstract class ViewServiceConfig : Service() {

    private var callView: CallView? = null

    // lifecycle -----------------------------------------------------------------------------------

    override fun onDestroy() {
        tryRemoveAllView()
        super.onDestroy()
    }

    // override ------------------------------------------------------------------------------------


    open fun setupCallView(action: CallView.Action): CallView.Builder? = null

    // public func ---------------------------------------------------------------------------------
    protected fun setupViewAppearance() {

        callView = setupCallView(customCallViewListener)
            ?.build()


        onMainThread {
            tryShowCallView()
        }
    }


    // private func --------------------------------------------------------------------------------

    private val customCallViewListener = object : CallView.Action {

        override fun popCallView() {
            tryShowCallView()
        }
    }

    private fun tryNavigateToCallView() {

        tryShowCallView()
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
        tryRemoveCallView()
    }

    // shorten -------------------------------------------------------------------------------------

    private fun tryRemoveCallView() = logIfError {
        callView!!.remove()
    }

    private fun tryShowCallView() = logIfError {
        callView!!.show()
    }


}


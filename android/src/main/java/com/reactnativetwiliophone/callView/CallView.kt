package com.reactnativetwiliophone.callView

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class CallView(
    private val builder: CallView.Builder
) : BaseView(builder.context) {

    init {
        setupLayoutParams()
    }

    // interface -----------------------------------------------------------------------------------

    interface Action {

        fun popCallView() {}
        fun onOpenCallView() {}
       // fun onCloseCallView() {}

    }

    // public --------------------------------------------------------------------------------------

    fun show() = logIfError {
        super.show(builder.rootView!!)
    }.onComplete {
        builder.listener.onOpenCallView()
    }


    fun remove() = logIfError {
        super.remove(builder.rootView!!)
    }.onComplete {
     //   builder.listener.onCloseCallView()
    }


    // private -------------------------------------------------------------------------------------

    override fun setupLayoutParams() {
        super.setupLayoutParams()

        logIfError {


            windowParams!!.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP
                flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = builder.dim         // default = 0.5f
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
           //  windowAnimations = R.style.TransViewStyle
            }

        }

    }

    // builder class -------------------------------------------------------------------------------

    class Builder : ICallViewBuilder {

        lateinit var context: Context

        var rootView: View? = null
        var listener = object : CallView.Action {}

        var dim = 0.5f

        override fun with(context: Context): Builder {
            this.context = context
            return this
        }

        override fun setCallView(view: View): Builder {
            this.rootView = view
            return this
        }

        override fun addCallViewListener(action: CallView.Action): Builder {
            this.listener = action
            return this
        }

        override fun setDimAmount(dimAmount: Float): Builder {
            this.dim = dimAmount
            return this
        }


        override fun build(): CallView {
            return CallView(this)
        }

    }
}

private interface ICallViewBuilder {

    fun with(context: Context): ICallViewBuilder

    fun setCallView(view: View): ICallViewBuilder

    fun addCallViewListener(action: CallView.Action): ICallViewBuilder

    fun setDimAmount(dimAmount: Float): ICallViewBuilder

    fun build(): CallView

}

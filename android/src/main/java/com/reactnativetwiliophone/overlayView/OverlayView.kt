package com.reactnativetwiliophone.overlyView

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class OverLyView(
    private val builder: OverLyView.Builder
) : BaseOverlyView(builder.context) {

    init {
        setupLayoutParams()
    }

    // interface -----------------------------------------------------------------------------------

    interface Action {

        fun popOverLyView() {}
        fun onOpenOverLyView() {}
        fun onCloseOverLyView() {}

    }

    // public --------------------------------------------------------------------------------------

    fun show() = logIfError {
        super.show(builder.rootView!!)
    }.onComplete {
        builder.listener.onOpenOverLyView()
    }


    fun remove() = logIfError {
        super.remove(builder.rootView!!)
    }.onComplete {
        builder.listener.onCloseOverLyView()
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
//            windowAnimations = R.style.TransViewStyle
            }

        }

    }

    // builder class -------------------------------------------------------------------------------

    class Builder : IOverLyViewBuilder {

        lateinit var context: Context

        var rootView: View? = null
        var listener = object : OverLyView.Action {}

        var dim = 0.5f

        override fun with(context: Context): Builder {
            this.context = context
            return this
        }

        override fun setOverLyView(view: View): Builder {
            this.rootView = view
            return this
        }

        override fun addOverLyViewListener(action: OverLyView.Action): Builder {
            this.listener = action
            return this
        }

        override fun setDimAmount(dimAmount: Float): Builder {
            this.dim = dimAmount
            return this
        }


        override fun build(): OverLyView {
            return OverLyView(this)
        }

    }
}

private interface IOverLyViewBuilder {

    fun with(context: Context): IOverLyViewBuilder

    fun setOverLyView(view: View): IOverLyViewBuilder

    fun addOverLyViewListener(action: OverLyView.Action): IOverLyViewBuilder

    fun setDimAmount(dimAmount: Float): IOverLyViewBuilder

    fun build(): OverLyView

}

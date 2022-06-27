package com.zyl.bubblelibrary

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

open class BubbleGLSurfaceView : GLSurfaceView {

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mOnTouch!!.onTouchChild(event)
        return true
    }

    interface OnTouch {
        fun onTouchChild(event: MotionEvent)
    }

    var mOnTouch: OnTouch? = null
    fun setOnTouch(onTouch: OnTouch) {
        this.mOnTouch = onTouch
    }

    fun destroy() {
        super.onDetachedFromWindow()
    }
}
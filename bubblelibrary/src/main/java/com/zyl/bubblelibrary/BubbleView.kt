package com.zyl.bubblelibrary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Author: zhaoyl
 * Date: 2022/06/29 10:53
 * remark:BubbleView
 **/
open class BubbleView : FrameLayout, GLSurfaceView.Renderer {

    private var mAccumulator = 0.0f
    private var mLastTicks: Long = -1
    private var mBubbleGame: BubbleGame? = null
    private lateinit var mSurfaceView: GLSurfaceView
    private var mImageReSmallId = mutableListOf<BodyBitmapInfo>()
    private var mImageReBigId = mutableListOf<BodyBitmapInfo>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mSurfaceView = createSurfaceView(context)
    }

    private fun createSurfaceView(context: Context): GLSurfaceView {
        val mSurfaceView = GLSurfaceView(context)
        val params =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mSurfaceView.layoutParams = params
        return mSurfaceView
    }

    fun addSmallBit(mImageReSmallId: MutableList<BodyBitmapInfo>): BubbleView {
        this.mImageReSmallId = mImageReSmallId
        return this
    }

    fun addBigBit(mImageReBigId: MutableList<BodyBitmapInfo>): BubbleView {
        this.mImageReBigId = mImageReBigId
        return this
    }

    fun addSetting(): BubbleView {
        mSurfaceView.run {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            setEGLContextClientVersion(2)
            holder.setFormat(PixelFormat.TRANSLUCENT)
            setZOrderOnTop(true)
            setRenderer(this@BubbleView)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        addView(mSurfaceView)
        return this
    }

    fun addBubbleType(onBackView: (bubbleView: BubbleGame) -> Unit): BubbleView {
        mBubbleGame = BubbleGame(this@BubbleView.context)
        onBackView(mBubbleGame!!)
        return this
    }

    fun addBubbleClick(onClick: (id: Int) -> Unit) {
        mBubbleGame?.setOnChildClick(object : BubbleGame.OnChildClick {
            override fun onChildClick(id: Int) {
                onClick(id)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mBubbleGame?.onTouchEvent(event)
        return true
    }

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        try {
            if (mImageReSmallId.isNotEmpty() && mImageReBigId.isNotEmpty()) {
                mBubbleGame?.bubbleInit(mImageReSmallId, mImageReBigId)
            }
            //创建的时候设置背景色
//            GLES20.glClearColor(235f / 255.0f, 235f / 255.0f, 255f / 255.0f, 255f / 255.0f)
            GLES20.glClearColor(0f, 0f, 0f, 0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceChanged(gl10: GL10?, i: Int, i1: Int) {
        try {
            gl10?.glViewport(0, 0, i, i1)
            if (mBubbleGame?.virtualHeight == 0f) mBubbleGame?.setSize(i, i1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDrawFrame(gl10: GL10) {
        try {
            if (mLastTicks == -1L) mLastTicks = Calendar.getInstance().time.time
            val minTimeStep = 1.0f / 100.0f
            val nowTicks = Calendar.getInstance().time.time
            mAccumulator += (nowTicks - mLastTicks).toFloat() / 1000.0f
            mLastTicks = nowTicks
            while (mAccumulator > minTimeStep) {
                mBubbleGame?.upDate(minTimeStep)
                mAccumulator -= minTimeStep
            }
            mBubbleGame?.draw()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onResume() {
        mBubbleGame?.let {
            mSurfaceView.onResume()
        }
    }

    fun onPause() {
        mBubbleGame?.let {
            mSurfaceView.onPause()
        }
    }

    fun destroy() {
        mBubbleGame?.destroy()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        //显示的时候启动刷新  隐藏的时候也停止刷新  节省资源
        if (visibility == View.VISIBLE) mSurfaceView.onResume() else mSurfaceView.onPause()
    }

}
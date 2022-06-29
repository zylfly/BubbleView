package com.zyl.bubbleview

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.zyl.bubblelibrary.BodyBitmapInfo
import com.zyl.bubblelibrary.BubbleGLSurfaceView
import com.zyl.bubblelibrary.BubbleGame
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Author: zhaoyl
 * Date: 2022/06/27 15:53
 * remark:我只做了小球从小变大的动画，大变小没写，也可以自定义一个view  去实现这些方法 调用更简单
 *
 **/
class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    private var mAccumulator = 0.0f
    private var mLastTicks: Long = -1
    private var mBubbleGame: BubbleGame? = null
    private val ballSize = 7
    lateinit var bubbleGLSurfaceView: BubbleGLSurfaceView
    private val mImageReSmallId = mutableListOf<BodyBitmapInfo>()
    private val mImageReBigId = mutableListOf<BodyBitmapInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bubbleGLSurfaceView = findViewById(R.id.bubbleGLSurfaceView)

        // Read in the resource
        for (i in 0 until ballSize) {
            val smallBit = BitmapFactory.decodeResource(resources, R.mipmap.ball_small)
            mImageReSmallId.add(BodyBitmapInfo(i, i, "我是+$i", smallBit))
            val bigBit = BitmapFactory.decodeResource(resources, R.mipmap.ball_big)
            mImageReBigId.add(BodyBitmapInfo(i, i, "我是+$i", bigBit))
        }
        initBubble()
    }


    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        try {
            Log.e("http", "------>onSurfaceCreated");
            if (mImageReSmallId.isNotEmpty() && mImageReBigId.isNotEmpty()) {
                mBubbleGame?.bubbleInit(mImageReSmallId, mImageReBigId)
            }
            //创建的时候设置背景色
            GLES20.glClearColor(235f / 255.0f, 235f / 255.0f, 255f / 255.0f, 255f / 255.0f)
//            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        try {
            Log.e("http", "------>onSurfaceChanged");
            gl?.glViewport(0, 0, width, height)
            if (mBubbleGame?.virtualHeight == 0f) mBubbleGame?.setSize(width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDrawFrame(p0: GL10?) {
        try {
            if (mLastTicks == -1L) mLastTicks = Calendar.getInstance().time.time
            val min_timestep = 1.0f / 100.0f
            val nowticks = Calendar.getInstance().time.time
            mAccumulator += (nowticks - mLastTicks).toFloat() / 1000.0f
            mLastTicks = nowticks
            while (mAccumulator > min_timestep) {
                mBubbleGame?.upDate(min_timestep)
                mAccumulator -= min_timestep
            }
            mBubbleGame?.draw()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initBubble() {
        try {
            bubbleGLSurfaceView.run {
                setEGLConfigChooser(8, 8, 8, 8, 16, 0)
                setEGLContextClientVersion(2)
                holder.setFormat(PixelFormat.TRANSLUCENT)
                setZOrderOnTop(true)
                setRenderer(this@MainActivity)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                setOnTouch(object : BubbleGLSurfaceView.OnTouch {
                    override fun onTouchChild(event: MotionEvent) {
                        try {
                            mBubbleGame?.onTouchEvent(event)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })

                mBubbleGame = BubbleGame(this@MainActivity).apply {
                    applyType = 0
                    bigWhPx = 184.0f
                    smallWhPx = 114.0f
                    maxBallRadius = 2.0499995f
                    smallChangeBigValue = 2f
//                    animTime = 0.10f
                    setOnChildClick(object : BubbleGame.OnChildClick {

                        override fun onChildClick(id: Int) {
                            Toast.makeText(this@MainActivity, "第${id}个", Toast.LENGTH_SHORT).show()
                            //选中的id
//                        binding.kkFriendNotesSelectorTag.text = mImageReSmallId[id].notesExplain
                        }

                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        mBubbleGame?.let {
            bubbleGLSurfaceView.onResume()
        }
//        binding.kkFriendNotesTopVideo.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBubbleGame?.let {
            bubbleGLSurfaceView.onPause()
        }
//        binding.kkFriendNotesTopVideo.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBubbleGame?.destroy()
    }

    fun bubble(view: View) {
        startActivity(Intent(this, BubbleActivity::class.java))
    }
}
package com.zyl.bubbleview

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.zyl.bubblelibrary.BodyBitmapInfo
import com.zyl.bubblelibrary.BubbleView

class BubbleActivity : AppCompatActivity() {

    private val ballSize = 7
    private var isShow = false
    lateinit var bubbleView: BubbleView
    private val mImageReSmallId = mutableListOf<BodyBitmapInfo>()
    private val mImageReBigId = mutableListOf<BodyBitmapInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubble)
        bubbleView = findViewById(R.id.bubbleView)
        for (i in 0 until ballSize) {
            val smallBit = BitmapFactory.decodeResource(resources, R.mipmap.ball_small)
            mImageReSmallId.add(BodyBitmapInfo(i, i, "我是第${i}个", smallBit))
            val bigBit = BitmapFactory.decodeResource(resources, R.mipmap.ball_big)
            mImageReBigId.add(BodyBitmapInfo(i, i, "我是第${i}个", bigBit))
        }
        //调用顺序最好别改
        bubbleView
            .addSmallBit(mImageReSmallId)
            .addBigBit(mImageReBigId)
            .addSetting()
            .addBubbleType {
                it.applyType = 0 //两种受力度模式（0或者1）
                it.bigWhPx = 184.0f //大图的分辨率
                it.smallWhPx = 114.0f//小图的分辨率
                it.maxBallRadius = 2.0499995f //大球的半径
                it.smallChangeBigValue = 2f //动画范围值
                it.animTime = 0.05f  //动画的快慢
            }.addBubbleClick {
                Toast.makeText(this, mImageReSmallId[it].notesExplain, Toast.LENGTH_SHORT).show()
            }
    }

    fun gone(view: View) {
        isShow = !isShow
        bubbleView.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        bubbleView.onResume()
    }

    override fun onPause() {
        super.onPause()
        bubbleView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbleView.destroy()
    }

}
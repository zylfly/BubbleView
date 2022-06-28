package com.zyl.bubblelibrary

import android.content.Context
import android.opengl.GLES20
import android.view.MotionEvent
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import org.joml.Matrix4f
import org.joml.Vector4f
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

open class BubbleGame(parent: Context?) {

    private var mParentActivity: Context? = null
    private var mView = Matrix4f()
    private val mSpriteList = mutableListOf<BubbleSprite>()
    private val mSpriteBigList = mutableListOf<BubbleSprite>()
    private val mB2World: World
    private val mDrawWhite = Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
    private val HORIZONTAL_WALL_LENGTH = 125.0f
    private val VERTICAL_WALL_LENGTH = 25.0f
    private val mCirclesMap: MutableMap<Int, Body> = HashMap()
    private var mLastTouchX = 0.0
    private var mLastTouchY = 0.0
    private var sysTime = 0L
    private var moveX = 0.0
    private var moveY = 0.0
    private var ballNum: Int = 0
    var minBallRadius = 1.5f
    var maxBallRadius = 2.999999f
    var smallChangeBigValue = 2.9f
    var applyType = 0 //默认是稳定加速的力
    var bigWhPx = 660.0f //这个是根据你图片像素的宽高
    var smallWhPx = 255.0f
    var virtualHeight = 0f
    var changeId = 0
    var animTime = 0.05f //小球动画速度
    private val mVec2List = arrayListOf(
        Vec2(6.5f, 3.6f),
        Vec2(3.0f, 5.8f),
        Vec2(5.3f, 7.6f),
        Vec2(8.1f, 7.38f),
        Vec2(10.0f, 5.6f),
        Vec2(10.5f, 2.5f),
        Vec2(2.5f, 2.5f),
        Vec2(2.5f, 8.8f),
        Vec2(5.0f, 10.5f),
        Vec2(8.0f, 10.5f)
    )

    private fun reset() {
        var b = mB2World.bodyList
        while (b != null) {
            mB2World.destroyBody(b)
            b = b.next
        }
        createHorizontalWall(Vec2(0f, 0f))
        createHorizontalWall(Vec2(0f, virtualHeight))
        createVerticalWall(Vec2(0f, 0f))
        createVerticalWall(Vec2(baseUnits, 0f))
        for (i in 0 until if (ballNum != 0) ballNum else mVec2List.size) {
            createBall(mVec2List[i], i)
        }
    }

    private fun createBall(position: Vec2, id: Int) {
        val bodyDef = BodyDef()
        bodyDef.position = position
        bodyDef.userData = BubbleBean(id, position, BubbleWorldType.Ball, id == changeId)
        bodyDef.type = BodyType.DYNAMIC
        val shape = CircleShape()
        shape.radius = if (id == changeId) maxBallRadius else minBallRadius
        val fixtureDef = FixtureDef()
        fixtureDef.shape = shape
        fixtureDef.userData = null
        fixtureDef.friction = 0.0f
        fixtureDef.restitution = 0.0f
        fixtureDef.density = 1f
        val body = mB2World.createBody(bodyDef)
        body.createFixture(fixtureDef)
        mCirclesMap[id] = body
    }

    private fun createHorizontalWall(position: Vec2) {
        val bodyDef = BodyDef()
        bodyDef.position = position
        bodyDef.userData = BubbleBean(null, position, BubbleWorldType.Wall, false)
        bodyDef.type = BodyType.KINEMATIC
        val shape = PolygonShape()
        shape.setAsBox(HORIZONTAL_WALL_LENGTH, 0.05f)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = shape
        val body = mB2World.createBody(bodyDef)
        body.createFixture(fixtureDef)
    }

    private fun createVerticalWall(position: Vec2) {
        val bodyDef = BodyDef()
        bodyDef.position = position
        bodyDef.userData = BubbleBean(null, position, BubbleWorldType.Wall, false)
        bodyDef.type = BodyType.KINEMATIC
        val shape = PolygonShape()
        shape.setAsBox(0.05f, VERTICAL_WALL_LENGTH)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = shape
        val body = mB2World.createBody(bodyDef)
        body.createFixture(fixtureDef)
    }

    fun bubbleInit(
        mImageReSmallId: MutableList<BodyBitmapInfo>,
        mImageReBigId: MutableList<BodyBitmapInfo>
    ) {
        BubbleSquare.initSquare()
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//        GLES20.glClearColor(235f / 255.0f, 235f / 255.0f, 255f / 255.0f, 255f / 255.0f)
        ballNum = mImageReSmallId.size
        bubbleInitData(mImageReSmallId, mImageReBigId)
    }

    private fun bubbleInitData(
        mImageReSmallId: MutableList<BodyBitmapInfo>,
        mImageReBigId: MutableList<BodyBitmapInfo>
    ) {
        for (i in 0 until mImageReSmallId.size) {
            mSpriteList.add(
                BubbleSprite(
                    BubbleTexture(
                        i,
                        mImageReSmallId[i].bitmap,
                        mImageReSmallId.size
                    )
                )
            )
        }
        for (i in 0 until mImageReBigId.size) {
            mSpriteBigList.add(
                BubbleSprite(
                    BubbleTexture(
                        i,
                        mImageReBigId[i].bitmap,
                        mImageReSmallId.size
                    )
                )
            )
        }
    }

    fun upDate(delta: Float) {
        mB2World.step(delta, 20, 20)
        mB2World.clearForces()
        for ((_, value) in mCirclesMap) {
            val circlePosition = value.worldCenter
            val centerPosition = Vec2(6.5f, virtualHeight / 2)
            val centertDistance = Vec2(0f, 0f)
            centertDistance.addLocal(circlePosition)
            centertDistance.subLocal(centerPosition)
            val finalDistance = centertDistance.length()
            centertDistance.negateLocal()
            centertDistance.mulLocal((1.0 / (finalDistance * 0.030)).toFloat())
            val distanceFromCenter = distanceBetweenPoints(centerPosition, value.position)
            val linearDamping =
                (if (distanceFromCenter > 5) 2 else 2 + (5 - distanceFromCenter)).toFloat()
            value.linearDamping = linearDamping
            if (applyType == 0) {
                value.applyForce(centertDistance, value.worldCenter)
            } else {
                centertDistance.mulLocal(finalDistance * 0.05f)
                value.applyLinearImpulse(centertDistance, value.worldCenter)
            }

//            value.applyForce(centertDistance, value.worldCenter)
        }
    }

    private fun distanceBetweenPoints(point1: Vec2, point2: Vec2): Double {
        return Math.hypot((point2.x - point1.x).toDouble(), (point2.y - point1.y).toDouble())
    }

    fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        val num_objects = mB2World.bodyCount
        if (num_objects >= 0) {
            var body = mB2World.bodyList
            for (i in 0 until num_objects) {
                BubbleSquare.mColor = mDrawWhite
                if (body.userData is BubbleBean) {
                    val bubbleBean = body.userData as BubbleBean
                    if (bubbleBean.type == BubbleWorldType.Ball) {
                        val position = body.worldCenter
                        bubbleBean.id?.let {
                            if (bubbleBean.isSelector) {
                                if (bubbleBean.isAnim) {
                                    if (body.fixtureList.shape.radius < smallChangeBigValue) {
                                        body.fixtureList.shape.radius =
                                            body.fixtureList.shape.radius + animTime
//                                        Log.e("http","------>radius="+body.fixtureList.shape.radius);
                                    } else {
//                                        Log.e("http","------>radius=end="+body.fixtureList.shape.radius);
                                        bubbleBean.isAnim = false
                                    }
                                }
                                mSpriteBigList[it].draw(
                                    it,
                                    position,
                                    body.angle,
                                    (body.fixtureList.shape.radius * 2) / bigWhPx,
                                    mView
                                )
                            } else {
                                body.fixtureList.shape.radius = minBallRadius
                                mSpriteList[it].draw(
                                    it,
                                    position,
                                    body.angle,
                                    (body.fixtureList.shape.radius * 2) / smallWhPx,
                                    mView
                                )
                            }
                        }
                    }
                }
                body = body.next
            }
        }
    }

    fun onTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastTouchX = event.x.toDouble()
                mLastTouchY = event.y.toDouble()
                sysTime = System.currentTimeMillis()
                moveX = 0.0
                moveY = 0.0
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x.toDouble()
                val y = event.y.toDouble()
                var dx = x - mLastTouchX
                var dy = y - mLastTouchY
                val b = sqrt(x.pow(2.0) + y.pow(2.0))
                dx = if (b == 0.0) 0.0 else dx / b
                dy = if (b == 0.0) 0.0 else dy / b
                if (dx == 0.0 && dy == 0.0) {
                    return
                }

                for ((_, value) in mCirclesMap) {
                    val direction =
                        Vec2((PUSH_STRENGTH * dx).toFloat(), (-PUSH_STRENGTH * dy).toFloat())
                    value.applyForce(direction, value.worldCenter)
                }
                moveX += abs(event.x - mLastTouchX)//X轴距离
                moveY += abs(event.y - mLastTouchY)//y轴距离
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP -> {
                val moveTime = System.currentTimeMillis() - sysTime
                if (moveTime < 200 && (moveX == 0.0 && moveY == 0.0)) {
                    val worldX = mLastTouchX * 13f / widths
                    val worldY = (heights - mLastTouchY) * 13f / widths
                    for ((_, value) in mCirclesMap) {
                        val b = Math.sqrt(
                            Math.pow(
                                value.position.x - worldX,
                                2.0
                            ) + Math.pow(value.position.y - worldY, 2.0)
                        ) <= minBallRadius
                        val bubbleBean = value.userData as BubbleBean
                        if (b) {
                            bubbleBean.id?.let {
                                changeId = it
                                mOnChildClick?.onChildClick(it)
                                if (bubbleBean.isSelector) {//是大的变小的
//                                    bubbleBean.isSelector = false
                                } else {// 小的变大
                                    bubbleBean.isSelector = !bubbleBean.isSelector
                                    bubbleBean.isAnim = true
                                    for ((_, mBody) in mCirclesMap) {
                                        val mBubbleBean = mBody.userData as BubbleBean
                                        if (bubbleBean.id != mBubbleBean.id) {
                                            if (mBubbleBean.isSelector) {
                                                mBubbleBean.isAnim = true
                                            }
                                            mBubbleBean.isSelector = false
                                        }
                                    }
                                }
                                return
                            }
                        }
                    }
                    return
                }
            }
        }
    }

    var widths = 0.0
    var heights = 0.0
    fun setSize(width: Int, height: Int) {
        widths = width.toDouble()
        heights = height.toDouble()
        val heightRatio = height.toFloat() / width.toFloat()
        virtualHeight = baseUnits * heightRatio
        mView = Matrix4f().ortho(0f, baseUnits, 0f, virtualHeight, 1f, -1f)
        reset()
    }

    var mOnChildClick: OnChildClick? = null

    interface OnChildClick {
        fun onChildClick(id: Int)
    }

    fun setOnChildClick(onChildClick: OnChildClick) {
        this.mOnChildClick = onChildClick
    }

    fun destroy() {
        mParentActivity = null
    }

    companion object {
        private const val PUSH_STRENGTH = 10000
        const val baseUnits = 13f //
        val maxBall = 10//max生成的球10个  这个要改变 连同mVec2List位置一直更新不然报错
    }

    init {
        mParentActivity = parent
        mB2World = World(Vec2(0.0f, 0.0f))
    }
}
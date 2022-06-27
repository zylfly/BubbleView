package com.zyl.bubblelibrary

import org.jbox2d.common.Vec2
import org.joml.Matrix4f

class BubbleSprite(var mDrawTexture: BubbleTexture) {
    fun draw(bindId: Int, position: Vec2, rotation: Float, scale: Float, view: Matrix4f?) {
        mDrawTexture.bindTexture(bindId)
        val mtranslate = Matrix4f().translate(position.x, position.y, 0.0f)
        val mscale = Matrix4f().scale(
            mDrawTexture.width.toFloat() * scale,
            mDrawTexture.height.toFloat() * scale,
            1.0f
        )
        val mrotate = Matrix4f().rotate(rotation * (180.0 / Math.PI).toFloat(), 0.0f, 0.0f, -1.0f)
        val mvp = Matrix4f().mul(view).mul(mtranslate).mul(mrotate).mul(mscale)
        BubbleSquare.sDrawSquare!!.draw(mvp)
    }
}
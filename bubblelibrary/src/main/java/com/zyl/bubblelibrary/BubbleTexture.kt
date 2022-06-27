package com.zyl.bubblelibrary

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.zyl.bubblelibrary.BubbleGame.Companion.maxBall

class BubbleTexture(id: Int, resourcesId: Bitmap, size: Int) {
    //    private val mTextureID = intArrayOf(0, 1, 2, 3, 4)
    private val mTextureID = IntArray(maxBall)
    var width = 0
        private set
    var height = 0
        private set

    fun bindTexture(id: Int) {
        GLES20.glActiveTexture(id)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID[id])
    }

    private fun loadTexture(id: Int, resourcesId: Bitmap) {
        GLES20.glGenTextures(mTextureID.size, mTextureID, 0)
        try {
            width = resourcesId.width
            height = resourcesId.height
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID[id])
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, resourcesId, 0)
        } catch (e: Exception) {
            GLES20.glGenTextures(mTextureID.size, mTextureID, 0)
            mTextureID[0] = id
            e.printStackTrace()
        }
    }

    init {
        for (i in 0 until size) {
            mTextureID[i] = i
        }
        loadTexture(id, resourcesId)
    }
}
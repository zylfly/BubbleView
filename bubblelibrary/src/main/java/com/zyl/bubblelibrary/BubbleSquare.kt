package com.zyl.bubblelibrary

import android.opengl.GLES20
import org.joml.Matrix4f
import org.joml.Vector4f
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class BubbleSquare {

    private val mVertexBuffer: FloatBuffer
    private val mDrawListBuffer: ShortBuffer
    private val mDrawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
    private val mVertexShaderCode = "attribute vec2 vPosition;" +
            "varying vec2 TexCoord;" +
            "uniform mat4 vMatrix;" +
            "void main() {" +
            "  gl_Position = vMatrix * vec4(vPosition,0,1);" +
            "  TexCoord = vPosition.st + 0.5;" +
            "  TexCoord.t = 1.0 - TexCoord.t;" +
            "}"
    private val mFragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D vTexture;" +
            "varying vec2 TexCoord;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = texture2D(vTexture,TexCoord.st).rgba * vColor;" +
            "  gl_FragColor *= gl_FragColor.a;" +
            "}"
    var mProgram: Int
    var mPositionHandle: Int
    var mColorHandle: Int
    var mMatrixHandle: Int
    var mTextureHandle: Int

    fun draw(matrix: Matrix4f) {
        GLES20.glUseProgram(mProgram)
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            mVertexStride, mVertexBuffer
        )
        val color = floatArrayOf(mColor.x, mColor.y, mColor.z, mColor.w)
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        val f = FloatArray(16)
        matrix[f, 0]
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, f, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mVertexCount)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        @JvmField
        var sDrawSquare: BubbleSquare? = null
        fun initSquare() {
            sDrawSquare = BubbleSquare()
        }

        const val COORDS_PER_VERTEX = 2
        var squareCoords = floatArrayOf(
            -0.5f, -0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
            -0.5f, 0.5f
        )
        var mColor = Vector4f()
        const val mVertexStride = COORDS_PER_VERTEX * 4
        const val mVertexCount = 4
        fun loadShader(type: Int, shaderCode: String?): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }
    }

    init {
        val bb =
            ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer.put(squareCoords)
        mVertexBuffer.position(0)

        val dlb =
            ByteBuffer.allocateDirect(mDrawOrder.size * 2) // (# of coordinate values * 2 bytes per short)
        dlb.order(ByteOrder.nativeOrder())
        mDrawListBuffer = dlb.asShortBuffer()
        mDrawListBuffer.put(mDrawOrder)
        mDrawListBuffer.position(0)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode)
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture")
        GLES20.glUniform1i(mTextureHandle, 0)
    }
}
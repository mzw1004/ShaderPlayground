package com.maviwz.demo.shader.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES31
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.maviwz.demo.shader.App
import com.maviwz.demo.shader.R
import com.maviwz.demo.shader.gles.EglCore
import com.maviwz.demo.shader.gles.GlUtil
import com.maviwz.demo.shader.gles.OffscreenSurface
import com.maviwz.demo.shader.utils.AssetsUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Compute shader demo
 *
 * Author: mazhiwei
 * Date: 2021/3/25
 * E-mail: mazhiwei1004@gmail.com
 */
class ComputeShaderActivity : AppCompatActivity() {

    private var ivPreview: ImageView? = null
    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ivPreview = ImageView(this).apply {
            addContentView(
                this, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.pic_compute_test)
        Log.d(GlUtil.TAG, "bmp size: width=${bmp.width}, height=${bmp.height}")
        ivPreview?.setImageBitmap(bmp)
        ComputeThread(bmp).start()
    }

    private inner class ComputeThread(val bmp: Bitmap) : Thread("ComputeThread") {

        private var acBuffer = 0
        private val width = 100
        private val height = 100

        override fun run() {
            super.run()
            var eglCore: EglCore? = null
            var surface: OffscreenSurface? = null
            try {
                eglCore = EglCore(null, 0)
                surface = OffscreenSurface(
                    eglCore,
                    width,
                    height
                )
                surface.makeCurrent()
                GlUtil.logVersionInfo()
                createSrcTexture()
                createAtomicCounterBuffer()
                runComputeTest()
                runRenderTest()
                // read result
                readResult()
            } catch (e: Exception) {
                Log.e(GlUtil.TAG, "run", e)
            } finally {
                surface?.release()
                eglCore?.release()
            }
        }

        private fun runComputeTest() {
            val limits = intArrayOf(1)
            GLES31.glGetIntegerv(GLES31.GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, limits, 0)
            Log.d(GlUtil.TAG, "runComputeTest: ${limits[0]}")
            // create program
            val program = GLES31.glCreateProgram()
            // compile shader
            val shader = GlUtil.loadShader(GLES31.GL_COMPUTE_SHADER, AssetsUtil.loadShaderString(App.context, "compute/main.glsl"))
            // attach shader
            GLES31.glAttachShader(program, shader)
            // link
            GLES31.glLinkProgram(program)
            // use program
            GLES31.glUseProgram(program)
            GLES31.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 1, acBuffer)
            GLES31.glDispatchCompute(5, 5, 1)
            // end program
            GLES31.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 1, 0)
            GLES31.glUseProgram(0)
        }

        private fun runRenderTest() {
            GLES31.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
            // create program
            val program = GLES31.glCreateProgram()
            // compile shader
            val v = GlUtil.loadShader(GLES31.GL_VERTEX_SHADER, AssetsUtil.loadShaderString(App.context, "compute/v.glsl"))
            GlUtil.checkGlError("compile vertex shader")
            val f = GlUtil.loadShader(GLES31.GL_FRAGMENT_SHADER, AssetsUtil.loadShaderString(App.context, "compute/f.glsl"))
            GlUtil.checkGlError("compile fragment shader")
            // attach shader
            GLES31.glAttachShader(program, v)
            GLES31.glAttachShader(program, f)
            // link
            GLES31.glLinkProgram(program)
            // use program
            GLES31.glUseProgram(program)
            // aPosition
            val aPositionHandle = GLES31.glGetAttribLocation(program, "aPosition")
            val vCoords = floatArrayOf(
                -1.0f, 1.0f, 0.0f,  // top left
                -1.0f, -1.0f, 0.0f,  // bottom left
                1.0f, -1.0f, 0.0f,  // bottom right
                1.0f, 1.0f, 0.0f // top right
            )
            val vertexBuffer = GlUtil.createFloatBuffer(vCoords)
            GLES31.glEnableVertexAttribArray(aPositionHandle)
            GLES31.glVertexAttribPointer(
                aPositionHandle, 3,
                GLES31.GL_FLOAT, false, 3 * 4, vertexBuffer
            )
            // bind atomic counter
            GLES31.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 0, acBuffer)
            // draw the rect
            val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
            val drawOrderBuf = GlUtil.createShortBuffer(drawOrder)
            GLES31.glDrawElements(
                GLES31.GL_TRIANGLES, drawOrder.size,
                GLES31.GL_UNSIGNED_SHORT, drawOrderBuf
            );
            // end program
            GLES31.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 0, 0)
            GLES31.glDisableVertexAttribArray(aPositionHandle)
            GLES31.glUseProgram(0)
        }

        private fun createSrcTexture() {

        }

        private fun createAtomicCounterBuffer() {
            val buffer = intArrayOf(1)
            GLES31.glGenBuffers(1, buffer, 0)
            acBuffer = buffer[0]
            GLES31.glBindBuffer(GLES31.GL_ATOMIC_COUNTER_BUFFER, acBuffer)
            GLES31.glBufferData(GLES31.GL_ATOMIC_COUNTER_BUFFER, 4, null, GLES31.GL_DYNAMIC_DRAW)
            GLES31.glBindBuffer(GLES31.GL_ATOMIC_COUNTER_BUFFER, 0)
        }

        private fun readResult() {
            val buf = ByteBuffer.allocateDirect(width * height * 4).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
            GLES31.glReadPixels(0, 0, width, height, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, buf)
            GlUtil.checkGlError("glReadPixels")
            buf.rewind()
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                copyPixelsFromBuffer(buf)
            }
//            uiHandler.post {
//                ivPreview?.setImageBitmap(bmp)
//            }
        }
    }
}
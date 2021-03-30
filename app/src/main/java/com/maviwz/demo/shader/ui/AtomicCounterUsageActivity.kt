package com.maviwz.demo.shader.ui

import android.graphics.Bitmap
import android.opengl.GLES31
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.maviwz.demo.shader.gles.EglCore
import com.maviwz.demo.shader.gles.GlUtil
import com.maviwz.demo.shader.gles.OffscreenSurface
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *
 * Author: mazhiwei
 * Date: 2021/3/30
 * E-mail: mazhiwei1004@gmail.com
 */
class AtomicCounterUsageActivity : AppCompatActivity() {

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
        ivPreview?.scaleY = -1f
        RenderThread().start()
    }

    private inner class RenderThread : Thread() {

        private var width = 200
        private var height = 200

        private val VERTEC_SHADER =
            "#version 310 es\n" +
            "in vec4 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "}"
        private val FRAGMENT_SHADER =
            "#version 310 es\n" +
            "precision highp float;\n" +
            "out vec4 gl_FragColor;\n" +
            "layout(binding=0, offset=0) uniform atomic_uint ac;\n" +
            "void main() {\n" +
            "    vec3 col = vec3(0.);\n" +
            "    uint counter = atomicCounterIncrement(ac);\n" +
            "    float r = float(counter)/float(${width * height});\n" +
            "    col.r += r;\n" +
            "    gl_FragColor = vec4(col, 1.);\n" +
            "}"
        private var acBuffer = 0
        private var program = 0

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

        private fun runRenderTest() {
            createAtomicCounterBuffer()
            // create program
            program = GLES31.glCreateProgram()
            // compile shader
            val v = GlUtil.loadShader(GLES31.GL_VERTEX_SHADER, VERTEC_SHADER)
            GlUtil.checkGlError("compile vertex shader")
            val f = GlUtil.loadShader(GLES31.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
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
            uiHandler.post {
                ivPreview?.setImageBitmap(bmp)
            }
        }

        private fun createAtomicCounterBuffer() {
            val buffer = intArrayOf(1)
            GLES31.glGenBuffers(1, buffer, 0)
            acBuffer = buffer[0]
            GLES31.glBindBuffer(GLES31.GL_ATOMIC_COUNTER_BUFFER, acBuffer)
            GLES31.glBufferData(GLES31.GL_ATOMIC_COUNTER_BUFFER, 4, null, GLES31.GL_DYNAMIC_DRAW)
            GLES31.glBindBuffer(GLES31.GL_ATOMIC_COUNTER_BUFFER, 0)
        }
    }
}
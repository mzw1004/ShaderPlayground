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
        ivPreview?.scaleY = -1f
        ComputeThread().start()
    }

    private inner class ComputeThread : Thread("ComputeThread") {

        private val COMPUTE_SHADER =
            "#version 310 es\n" +
            "layout (local_size_x = 20, local_size_y = 20, local_size_z = 1) in;\n" +
//                "layout(binding = 0, rgba32f) readonly uniform highp image2D input_image;\n" +
            "layout(binding = 0, rgba32f) writeonly uniform highp image2D output_image;\n" +
            "layout(binding = 1, offset = 0) uniform atomic_uint ac;\n" +
            "void main(void) {\n" +
            "    ivec2 pos = ivec2(gl_GlobalInvocationID.xy);\n" +
            "    vec4 col = vec4(0.0, 0.0, 0.0, 1.);\n" +
            "    col.r += float(pos.x) / 100.;\n" +
            "    col.g += float(pos.y) / 100.;\n" +
            "    uint previous = atomicCounterIncrement(ac);\n" +
            "    imageStore(output_image, pos.xy, col);\n" +
            "}"
        private val VERTEC_SHADER =
            "attribute vec4 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "}"
        private val FRAGMENT_SHADER =
            "void main() {\n" +
            "    vec3 col = vec3(0.);\n" +
            "    col.r += 1.;\n" +
            "    gl_FragColor = vec4(col, 1.);\n" +
            "}"
        private var outputFbo = 0
        private var outputTexture = 0
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
                createOutputFBO()
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
            val shader = compileShader(GLES31.GL_COMPUTE_SHADER, COMPUTE_SHADER)
            // attach shader
            GLES31.glAttachShader(program, shader)
            // link
            GLES31.glLinkProgram(program)
            // use program
            GLES31.glUseProgram(program)
            GLES31.glBindBufferBase(GLES31.GL_ATOMIC_COUNTER_BUFFER, 1, acBuffer)
            GLES31.glBindImageTexture(
                0,
                outputTexture,
                0,
                false,
                0,
                GLES31.GL_WRITE_ONLY,
                GLES31.GL_RGBA32F
            )
            GLES31.glDispatchCompute(5, 5, 1)
            // end program
            GLES31.glUseProgram(0)
        }

        private fun runRenderTest() {
            GLES31.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
            // create program
            val program = GLES31.glCreateProgram()
            // compile shader
            val v = compileShader(GLES31.GL_VERTEX_SHADER, VERTEC_SHADER)
            val f = compileShader(GLES31.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
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
            // draw the rect
            val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)
            val drawOrderBuf = GlUtil.createShortBuffer(drawOrder)
            GLES31.glDrawElements(
                GLES31.GL_TRIANGLES, drawOrder.size,
                GLES31.GL_UNSIGNED_SHORT, drawOrderBuf
            );
            // end program
            GLES31.glDisableVertexAttribArray(aPositionHandle)
            GLES31.glUseProgram(0)
        }

        private fun createOutputFBO() {
            val fbo = intArrayOf(1)
            val texture = intArrayOf(1)
            // create frame buffer
            GLES31.glGenFramebuffers(1, fbo, 0)
            GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, fbo[0])
            outputFbo = fbo[0]
            // create texture
            GLES31.glGenTextures(1, texture, 0)
            GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, texture[0])
            GLES31.glTexStorage2D(GLES31.GL_TEXTURE_2D, 1, GLES31.GL_RGBA32F, width, height)
            GLES31.glTexParameteri(
                GLES31.GL_TEXTURE_2D,
                GLES31.GL_TEXTURE_MIN_FILTER,
                GLES31.GL_LINEAR
            )
            GLES31.glTexParameteri(
                GLES31.GL_TEXTURE_2D,
                GLES31.GL_TEXTURE_MAG_FILTER,
                GLES31.GL_LINEAR
            )
            GLES31.glTexParameteri(
                GLES31.GL_TEXTURE_2D,
                GLES31.GL_TEXTURE_WRAP_S,
                GLES31.GL_CLAMP_TO_EDGE
            )
            GLES31.glTexParameteri(
                GLES31.GL_TEXTURE_2D,
                GLES31.GL_TEXTURE_WRAP_T,
                GLES31.GL_CLAMP_TO_EDGE
            )
            GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, 0)
            outputTexture = texture[0]
            // bind texture
            GLES31.glFramebufferTexture2D(
                GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0,
                GLES31.GL_TEXTURE_2D, texture[0], 0
            )
            GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
        }

        private fun createAtomicCounterBuffer() {
            val buffer = intArrayOf(1)
            GLES31.glGenBuffers(1, buffer, 0)
            acBuffer = buffer[0]
        }

        private fun compileShader(type: Int, source: String): Int {
            var shader = GLES31.glCreateShader(type)
            GLES31.glShaderSource(shader, source)
            GLES31.glCompileShader(shader)
            val compiled = intArrayOf(1)
            GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e(GlUtil.TAG, "Could not compile shader: ")
                Log.e(GlUtil.TAG, " " + GLES31.glGetShaderInfoLog(shader))
                GLES31.glDeleteShader(shader)
                shader = 0
            }
            return shader
        }

        private fun readResult() {
            val buf = ByteBuffer.allocateDirect(width * height * 4).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
            GLES31.glReadPixels(
                0, 0, width, height,
                GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, buf
            )
            GlUtil.checkGlError("glReadPixels")
            buf.rewind()
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                copyPixelsFromBuffer(buf)
            }
            uiHandler.post {
                ivPreview?.setImageBitmap(bmp)
            }
        }
    }
}
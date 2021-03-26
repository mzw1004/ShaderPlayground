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
import java.nio.FloatBuffer
import java.nio.IntBuffer

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
        ComputeThread().start()
    }

    private inner class ComputeThread : Thread("ComputeThread") {

        private val COMPUTE_SHADER = "\n" +
                "#version 310 es\n" +
                "layout (local_size_x = 20, local_size_y = 20, local_size_z = 1) in;\n" +
//                "layout(binding = 0, rgba32f) readonly uniform highp image2D input_image;\n" +
                "layout(binding = 0, rgba32f) writeonly uniform highp image2D output_image;\n" +
                "void main(void)\n" +
                "{\n" +
                "ivec2 pos = ivec2(gl_GlobalInvocationID.xy);\n" +
                "vec4 col = vec4(0.0, 0.0, 0.0, 1.);\n" +
                "col.r += float(pos.x) / 100.;\n" +
                "col.g += float(pos.y) / 100.;\n" +
                "imageStore(output_image, pos.xy, col);\n" +
                "}"
        private var outputFbo = 0
        private var outputTexture = 0
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
                GLES31.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
                GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT)
                runComputeTest(surface)
                // read result
                readResult()
            } catch (e: Exception) {
                Log.e(GlUtil.TAG, "run", e)
            } finally {
                surface?.release()
                eglCore?.release()
            }
        }

        private fun runComputeTest(surface: OffscreenSurface) {
            GlUtil.logVersionInfo()
            val limits = intArrayOf(1)
            GLES31.glGetIntegerv(GLES31.GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, limits, 0)
            Log.d(GlUtil.TAG, "runComputeTest: ${limits[0]}")
            Log.d(GlUtil.TAG, "run: $COMPUTE_SHADER")
            createOutputFBO()
            // create program
            val program = GLES31.glCreateProgram()
            // compile shader
            val shader = compileShader()
            // attach shader
            GLES31.glAttachShader(program, shader)
            // link
            GLES31.glLinkProgram(program)
            // use program
            GLES31.glUseProgram(program)
            GLES31.glBindImageTexture(
                0,
                outputTexture,
                0,
                false,
                0,
                GLES31.GL_WRITE_ONLY,
                GLES31.GL_RGBA32F
            );
            GLES31.glDispatchCompute(5, 5, 1)
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
        }

        private fun compileShader(): Int {
            var shader = GLES31.glCreateShader(GLES31.GL_COMPUTE_SHADER)
            GLES31.glShaderSource(shader, COMPUTE_SHADER)
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
            val a = FloatBuffer.allocate(4)
            GLES31.glReadBuffer(GLES31.GL_COLOR_ATTACHMENT0)
            GLES31.glReadPixels(60, 60, 1, 1, GLES31.GL_RGBA, GLES31.GL_FLOAT, a)
            GlUtil.checkGlError("glReadPixels/a")
            a.rewind()
            Log.d(GlUtil.TAG, "readResult: ${a[0]}, ${a[1]}, ${a[2]}, ${a[3]}")
        }
    }
}
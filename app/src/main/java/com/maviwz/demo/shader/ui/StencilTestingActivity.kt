package com.maviwz.demo.shader.ui

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.maviwz.demo.shader.App
import com.maviwz.demo.shader.gles.GlUtil
import com.maviwz.demo.shader.render.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * Author: mazhiwei
 * Date: 2021/4/7
 * E-mail: mazhiwei1004@gmail.com
 */
class StencilTestingActivity : AppCompatActivity() {

    private lateinit var glView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glView = GLSurfaceView(this).apply {
            setContentView(this)
        }
        glView.setEGLContextClientVersion(3)
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 8)
        glView.setRenderer(MyRenderer())
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    private inner class MyRenderer : GLSurfaceView.Renderer {

        private val blockVertex = floatArrayOf(
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
        )
        private val normal = floatArrayOf(
            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,
        )
        private val blockIndex = intArrayOf(
            0, 1, 3,
            2, 3, 1,
        )
        private val eyePosition = floatArrayOf(0f, 0.2f, 0f)
        private val RED = floatArrayOf(1f, 0f, 0f)
        private val GREEN = floatArrayOf(0f, 1f, 0f)
        private val BLUE = floatArrayOf(0f, 0f, 1f)
        private val YELLOW = floatArrayOf(1f, 1f, 0f)
        private val BLACK = floatArrayOf(0f, 0f, 0f)
        private val WHITE = floatArrayOf(1f, 1f, 1f)
        private val modelMatrix = FloatArray(16)
        private val mvMatrix = FloatArray(16)
        private val mvpMatrix = FloatArray(16)
        private val camera = Camera()
        private var blockMesh: Mesh? = null
        private var shader: Shader? = null
        private var tsCreated: Long = 0

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            tsCreated = SystemClock.uptimeMillis()
            val indexBuffer = GlUtil.createIntBuffer(blockIndex)
            val vertexBuffer = GlUtil.createFloatBuffer(blockVertex)
            val normalBuffer = GlUtil.createFloatBuffer(normal)
            blockMesh = Mesh(Mesh.PrimitiveMode.TRIANGLES, IndexBuffer(indexBuffer), arrayOf(
                VertexBuffer(3, vertexBuffer),
                VertexBuffer(3, normalBuffer)
            ))
            shader = Shader.createFromAssets(App.context, "stencil/v.glsl", "stencil/f.glsl", null)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            camera.viewport(width.toFloat(), height.toFloat())
        }

        override fun onDrawFrame(gl: GL10?) {
            val time = SystemClock.uptimeMillis() - tsCreated
            GLES30.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_STENCIL_BUFFER_BIT)
            eyePosition[0] = 2 * sin(time/1000f)
            eyePosition[2] = -2 * cos(time/1000f)
            camera.lookAt(eyePosition[0], eyePosition[1], eyePosition[2], 0f, 0f, 0f, 0f, 1f, 0f)
            GLES30.glEnable(GLES30.GL_STENCIL_TEST)
            drawCube()
            GLES30.glDisable(GLES30.GL_STENCIL_TEST)
        }

        private fun drawCube() {
            drawFront()
            drawLeft()
            drawRight()
            drawBack()
            drawBottom()
            drawTop()
        }

        private fun drawBlock(color: FloatArray) {
            Matrix.multiplyMM(mvMatrix, 0, camera.viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, camera.projectionMatrix, 0, mvMatrix, 0)
            shader?.let {
                it.setMatrix4("u_MVMatrix", mvMatrix)
                    .setMatrix4("u_MVPMatrix", mvpMatrix)
//                    .set3("u_PosEye", eyePosition)
                    .set3("u_Color", color)
                    .use()
                blockMesh?.draw()
            }
        }

        private fun drawFront() {
            Matrix.setIdentityM(modelMatrix, 0)
            drawBlock(RED)
        }

        private fun drawLeft() {
            Matrix.setRotateM(modelMatrix, 0, -90f, 0f, 1f, 0f)
            drawBlock(GREEN)
        }

        private fun drawRight() {
            Matrix.setRotateM(modelMatrix, 0, 90f, 0f, 1f, 0f)
            drawBlock(BLUE)
        }

        private fun drawBack() {
            Matrix.setRotateM(modelMatrix, 0, 180f, 0f, 1f, 0f)
            drawBlock(YELLOW)
        }

        private fun drawBottom() {
            Matrix.setRotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
            drawBlock(BLACK)
        }

        private fun drawTop() {
            Matrix.setRotateM(modelMatrix, 0, 90f, 1f, 0f, 0f)
            drawBlock(WHITE)
        }
    }
}
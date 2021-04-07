package com.maviwz.demo.shader.ui

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.maviwz.demo.shader.App
import com.maviwz.demo.shader.render.Camera
import com.maviwz.demo.shader.render.Mesh
import com.maviwz.demo.shader.render.Shader
import com.maviwz.demo.shader.render.Texture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * Author: mazhiwei
 * Date: 2021/4/6
 * E-mail: mazhiwei1004@gmail.com
 */
class ObjViewerActivity : AppCompatActivity() {

    private lateinit var glView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glView = GLSurfaceView(this).apply {
            setContentView(this)
        }
        glView.setEGLContextClientVersion(3)
        glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glView.setRenderer(MyRenderer())
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    private inner class MyRenderer : GLSurfaceView.Renderer {

        private var shader: Shader? = null
        private var mesh: Mesh? = null
        private val camera = Camera()
        private val modelMatrix = FloatArray(16)
        private val mvMatrix = FloatArray(16)
        private val mvpMatrix = FloatArray(16)
        private var tsCreate: Long = 0
        private val light = floatArrayOf(2f, 2f, -2f)

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            tsCreate = SystemClock.uptimeMillis()
            mesh = Mesh.createFromAsset(App.context, "objs/icosphere.obj")
            shader = Shader.createFromAssets(App.context, "objviewer/v.glsl", "objviewer/f.glsl", null)
                    .set3("u_Light", light)
            Matrix.setIdentityM(modelMatrix, 0)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            camera.viewport(width.toFloat(), height.toFloat())
        }

        override fun onDrawFrame(gl: GL10?) {
            val time = SystemClock.uptimeMillis() - tsCreate
            GLES30.glClearColor(0.5f, 0.5f, .5f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

            camera.lookAt(3 * sin(time/1000f), 0.5f, -3 * cos(time/1000f), 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(mvMatrix, 0, camera.viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, camera.projectionMatrix, 0, mvMatrix, 0)
            shader?.let {
                it.setMatrix4("u_MVMatrix", mvMatrix)
                    .setMatrix4("u_MVPMatrix", mvpMatrix)
                    .use()
                mesh?.draw()
            }
        }
    }
}
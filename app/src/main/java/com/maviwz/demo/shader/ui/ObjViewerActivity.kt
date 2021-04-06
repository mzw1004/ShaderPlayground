package com.maviwz.demo.shader.ui

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maviwz.demo.shader.App
import com.maviwz.demo.shader.render.Mesh
import com.maviwz.demo.shader.render.Shader
import com.maviwz.demo.shader.render.Texture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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
        private var material: Texture? = null
        private val modelMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)
        private val mvMatrix = FloatArray(16)
        private val mvpMatrix = FloatArray(16)

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            mesh = Mesh.createFromAsset(App.context, "objs/andy.obj")
            material = Texture.createFromAsset(App.context, "objs/andy.png", Texture.WrapMode.CLAMP_TO_EDGE)
            shader = Shader.createFromAssets(App.context, "objviewer/v.glsl", "objviewer/f.glsl", null)
                .setTexture("u_Material", material)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.setIdentityM(viewMatrix, 0)
            Matrix.setIdentityM(projectionMatrix, 0)

//            Matrix.scaleM(modelMatrix, 0, 2f, 2f, 2f)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0.5f, -1.5f, 0f, 0f, 0f, 0f, 1f, 0f)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height
            val left = -ratio
            val right = ratio
            val bottom = -1.0f
            val top = 1.0f
            val near = 1f
            val far = 100.0f
            Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES30.glClearColor(0.5f, 0.5f, .5f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
            shader?.let {
                it.setMatrix4("u_MVMatrix", mvMatrix)
                    .setMatrix4("u_MVPMatrix", mvpMatrix)
                    .use()
                mesh?.draw()
            }
        }
    }
}
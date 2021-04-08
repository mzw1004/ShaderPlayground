package com.maviwz.demo.shader.ui

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *
 * Author: mazhiwei
 * Date: 2021/4/8
 * E-mail: mazhiwei1004@gmail.com
 */
class ParticleActivity : AppCompatActivity() {

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

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES30.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES30.glClearColor(0.5f, 0.5f, .5f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        }
    }
}

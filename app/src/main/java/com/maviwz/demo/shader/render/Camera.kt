package com.maviwz.demo.shader.render

import android.opengl.Matrix

/**
 *
 * Author: mazhiwei
 * Date: 2021/4/7
 * E-mail: mazhiwei1004@gmail.com
 */
class Camera {
    private val lookAtMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)
    private val translation = FloatArray(16)
    val viewMatrix = FloatArray(16)
    get() {
        Matrix.multiplyMM(field, 0, translation, 0, lookAtMatrix, 0)
        return field
    }

    init {
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(translation, 0)
    }

    fun lookAt(
        eyeX: Float, eyeY: Float, eyeZ: Float,
        centerX: Float, centerY: Float, centerZ: Float,
        upX: Float, upY: Float, upZ: Float
    ) {
        Matrix.setLookAtM(lookAtMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
    }

    fun viewport(width: Float, height: Float) {
        val ratio = width / height
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1f
        val far = 100.0f
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
    }

    fun setRotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.setRotateM(translation, 0, angle, x, y, z)
    }
}
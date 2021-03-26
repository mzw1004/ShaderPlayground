package com.maviwz.demo.shader.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.maviwz.demo.shader.R

/**
 *
 * Author: mazhiwei
 * Date: 2021/3/25
 * E-mail: mazhiwei1004@gmail.com
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoComputeShader(view: View) {
        Intent(this, ComputeShaderActivity::class.java).apply {
            startActivity(this)
        }
    }
}
package com.maviwz.demo.shader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 *
 * Author: mazhiwei
 * Date: 2021/3/30
 * E-mail: mazhiwei1004@gmail.com
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        context = base
    }
}
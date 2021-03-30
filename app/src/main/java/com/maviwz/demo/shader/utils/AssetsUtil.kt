package com.maviwz.demo.shader.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 *
 * Author: mazhiwei
 * Date: 2021/3/30
 * E-mail: mazhiwei1004@gmail.com
 */
object AssetsUtil {

    fun loadShaderString(context: Context, fileName: String): String {
        val sb = StringBuilder()
        val ins = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(ins, StandardCharsets.UTF_8))
        reader.lineSequence().forEach {
            sb.append(it)
            sb.append("\n")
        }
        reader.closeQuietly()
        ins.closeQuietly()
        return sb.toString()
    }
}
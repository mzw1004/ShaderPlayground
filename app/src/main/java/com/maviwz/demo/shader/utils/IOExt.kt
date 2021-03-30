package com.maviwz.demo.shader.utils

import java.io.Closeable

/**
 * ${DESC}
 *
 * Author mazhiwei
 * Email mazhiwei1004@gmail.com
 * Date 2018/12/20.
 */

fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (_: Throwable) {
    }
}
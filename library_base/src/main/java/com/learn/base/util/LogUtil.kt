package com.learn.base.util

import android.util.Log

/**
 * Create by lxw on 2020/12/15
 * Note：LogUtil
 */
object LogUtil {
    private const val TAG = "LogUtil"

    fun debug(msg: String) {
        Log.d(TAG, msg)
    }
}
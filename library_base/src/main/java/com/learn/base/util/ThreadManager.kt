package com.learn.base.util

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Create by lxw on 2021/1/8
 * Note：Thread Manager
 */
object ThreadManager {
    @Volatile
    private lateinit var singleThreadPool: ThreadPoolExecutor


    fun getSingleThreadPool(): ThreadPoolExecutor {
        singleThreadPool =
            ThreadPoolExecutor(
                1, 1, 0,
                TimeUnit.MILLISECONDS, LinkedBlockingQueue()
            )
        return singleThreadPool
    }
}
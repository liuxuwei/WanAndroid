package com.learn.camera.global

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

/**
 * Create by lxw on 2021/1/5
 * Note：App
 */
class CameraApp: Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}
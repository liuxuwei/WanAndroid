package com.learn.player

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

/**
 * Create by lxw on 2020/12/11
 * Note：application
 */
class App: Application() {


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
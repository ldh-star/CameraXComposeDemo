package com.liangguo.cameraxcomposedemo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context


/**
 * @author ldh
 * 时间: 17:04
 * 邮箱: 2637614077@qq.com
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

}

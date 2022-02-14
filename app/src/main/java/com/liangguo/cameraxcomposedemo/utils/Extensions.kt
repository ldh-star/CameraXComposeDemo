package com.liangguo.cameraxcomposedemo.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.liangguo.cameraxcomposedemo.logic.Media
import kotlinx.coroutines.flow.MutableStateFlow


/**
 * @author ldh
 * 时间: 2022/1/27 17:00
 * 邮箱: 2637614077@qq.com
 */

/**
 * 用这种方法可以避免LiveData在设置相同的值时依旧通知
 */
inline var <T> MutableLiveData<T?>.smartNotifyValue: T?
    get() = this.value
    set(value) = kotlin.run {
        if (this.value != value) {
            this.value = value
        }
    }


/**
 * 用这种方法可以避免LMutableStateFlow在设置相同的值时依旧通知
 */
inline var <T>  MutableStateFlow<T>.smartNotifyValue: T
    get() = this.value
    set(value) = kotlin.run {
        if (this.value != value) {
            this.value = value
        }
    }

/**
 * 为ImageView加载媒体
 */
@SuppressLint("CheckResult")
fun ImageView.loadMedia(media: Media) {
    Glide.with(this)
        .load(Uri.fromFile(media.file))
        .into(this)
}



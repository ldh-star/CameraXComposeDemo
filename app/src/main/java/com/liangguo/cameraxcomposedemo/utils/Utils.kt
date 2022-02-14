package com.liangguo.cameraxcomposedemo.utils

import android.content.ContentValues
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.liangguo.cameraxcomposedemo.App.Companion.context
import com.liangguo.cameraxcomposedemo.logic.Media
import java.io.File


/**
 * @author ldh
 * 时间: 2022/2/14 18:22
 * 邮箱: 2637614077@qq.com
 */

fun Media.notifyInsert() {
    if (isImage) {
        notifyImageInsert(file = file)
    } else {
        notifyVideoInsert(file = file, duration = getVideoDuration(file.absolutePath))
    }
}

/**
 * 通知系统媒体库插入图片
 */
private fun notifyImageInsert(file: File) {
    val values = ContentValues()
    //媒体文件的标题
    values.put(MediaStore.Images.ImageColumns.DATA, file.absolutePath)
    //把文件插入到媒体库ContentProvider中
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}

/**
 * 通知系统媒体库插入视频
 */
private fun notifyVideoInsert(file: File, duration: Long? = null) {
    val values = ContentValues()
    //媒体文件的标题
    values.put(MediaStore.Video.VideoColumns.DATA, file.absolutePath)
    values.put(MediaStore.Video.VideoColumns.DURATION, duration)
    //把文件插入到媒体库ContentProvider中
    context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
}

/**
 * 获取视频时长
 */
private fun getVideoDuration(videoPath: String): Long {
    var duration = 0L
    try {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(videoPath)
        duration =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: 0
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return duration
}

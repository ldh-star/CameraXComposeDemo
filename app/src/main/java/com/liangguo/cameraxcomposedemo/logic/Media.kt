package com.liangguo.cameraxcomposedemo.logic

import java.io.File


/**
 * @author ldh
 * 时间: 2022/2/14 15:19
 * 邮箱: 2637614077@qq.com
 *
 * 媒体的实体类
 * @param isImage true：图片， false：视频
 */
data class Media(val isImage: Boolean, val file: File)

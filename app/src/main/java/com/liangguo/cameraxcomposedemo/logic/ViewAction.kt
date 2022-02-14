package com.liangguo.cameraxcomposedemo.logic

import androidx.camera.core.ZoomState
import androidx.compose.ui.geometry.Offset
import java.io.File

sealed class ViewAction {
        /**
         * 进行拍照的事件
         */
        object TakePicture : ViewAction()

        /**
         * 拍照执行完成过后触发的事件
         */
        class OnPictureTaken(
            val success: Boolean,
            val file: File,
            val exception: Exception? = null
        ) : ViewAction()

        /**
         * 反转相机，朝前或朝后
         */
        object ReverseCamera : ViewAction()

        /**
         * 改变网格是否可见
         */
        object ChangeGridViewVisibility : ViewAction()

        /**
         * 改变录制视频时的闪光灯
         */
        object ChangeRecordVideoTorch : ViewAction()

        /**
         * 打开浏览刚刚拍摄的媒体界面
         */
        object OpenGallery : ViewAction()

        /**
         * 退出相机
         */
        object Exit : ViewAction()

        /**
         * 改变拍照的闪光灯模式
         */
        object ChangeImageCaptureFlashMode : ViewAction()

        /**
         * 监听用户手势事件
         */
        class OnGesture(
            val centroid: Offset,
            val pan: Offset,
            val zoom: Float,
            val rotation: Float
        ) : ViewAction()

        /**
         * 配置[ZoomState]
         */
        class ChangeZoomState(val zoomState: ZoomState?) : ViewAction()

        /**
         * 当视频录像完成后
         */
        class OnVideoRecorded(
            val success: Boolean,
            val file: File,
            val exception: Exception? = null
        ) : ViewAction()

        /**
         * 改变相机模式
         * @param captureImageMode 是否是拍照模式，true：拍照  false：视频
         */
        class ChangeCameraMode(val captureImageMode: Boolean) : ViewAction()

        /**
         * 停止录像时调用
         */
        object OnRecordStop : ViewAction()
    }

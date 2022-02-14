package com.liangguo.cameraxcomposedemo.logic

import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ZoomState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow


/**
 * @author ldh
 *
 * Compose界面的数据，就要这样分开写，一定不要弄到一个对象里去，否则其中某个属性更新会引起整个更新
 */
class ViewState {
        /**
         * 使用的是哪一个摄像头。 true为CameraSelector.DEFAULT_FRONT_CAMERA， false为CameraSelector.DEFAULT_BACK_CAMERA
         */
        val frontCamera = MutableStateFlow(false)

        /**
         * 是否显示分割线
         */
        val showClipGrid = MutableStateFlow(false)

        /**
         * 进行拍照的信号量，只要这个量>0，就会执行拍照，每次拍照消耗一次信号量，直到减为0为止
         */
        val takePictureSemaphore = MutableStateFlow(0)

        /**
         * 最近拍摄的媒体，最前面的是最刚刚的，最后面的是最早拍的
         */
        val recentMedias = mutableStateListOf<Media>()

        /**
         * 拍照的闪光模式
         * [androidx.camera.core.ImageCapture.FLASH_MODE_AUTO]
         * [androidx.camera.core.ImageCapture.FLASH_MODE_ON]
         * [androidx.camera.core.ImageCapture.FLASH_MODE_OFF]
         */
        val imageCaptureFlashMode = MutableStateFlow(FLASH_MODE_OFF)

        /**
         * 拍照时的相机缩放
         */
        val zoomRatio = MutableLiveData(1f)

        /**
         * 相机当前的zoom状态
         */
        var zoomState: ZoomState? = null

        /**
         * 当前是否把zoom信息显示出来
         */
        val showZoomInfo = MutableStateFlow(false)

        /**
         * 当前摄像模式是否为拍照
         * true:  拍照
         * false: 录像
         */
        val captureImageMode = MutableStateFlow(true)

        /**
         * 是否正在录像
         * 这里要用3个状态来表示，因为每次切换状态涉及到视频的开始录制和停止录制，如果视频还没有开始录制就让它停止录制就要出问题
         */
        var recordingVideo = MutableStateFlow<Boolean?>(null)


    }

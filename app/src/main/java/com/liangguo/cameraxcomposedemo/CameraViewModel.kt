package com.liangguo.cameraxcomposedemo

import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture.*
import androidx.lifecycle.ViewModel
import com.liangguo.cameraxcomposedemo.App.Companion.context
import com.liangguo.cameraxcomposedemo.logic.ActivityState
import com.liangguo.cameraxcomposedemo.logic.Media
import com.liangguo.cameraxcomposedemo.logic.ViewAction
import com.liangguo.cameraxcomposedemo.logic.ViewState
import com.liangguo.cameraxcomposedemo.utils.notifyInsert
import com.liangguo.cameraxcomposedemo.utils.smartNotifyValue
import com.liangguo.timingexecutor.TimingExecutor
import java.io.File
import kotlin.math.abs


/**
 * @author ldh
 * 时间: 2022/2/10 11:48
 * 邮箱: 2637614077@qq.com
 */
class CameraViewModel : ViewModel() {

    val viewState = ViewState()

    val unComposableUiState = ActivityState()


    fun onAction(action: ViewAction) {
        action.apply {
            when (this) {

                is ViewAction.ReverseCamera ->
                    viewState.frontCamera.value =
                        !viewState.frontCamera.value

                is ViewAction.ChangeGridViewVisibility -> viewState.showClipGrid.value =
                    !viewState.showClipGrid.value

                is ViewAction.TakePicture -> {
                    if (viewState.captureImageMode.value) {
                        viewState.takePictureSemaphore.value++
                    } else {
                        viewState.recordingVideo.value?.let {
                            viewState.recordingVideo.value = !it
                        } ?: let { viewState.recordingVideo.value = true }
                    }
                }

                is ViewAction.OnRecordStop ->
                    //录像完了过后一定要把状态恢复到没有录制时的状态
                    if (viewState.recordingVideo.value == false) {
                        viewState.recordingVideo.value = null
                    }

                is ViewAction.ChangeImageCaptureFlashMode -> {
                    viewState.imageCaptureFlashMode.value =
                        when (viewState.imageCaptureFlashMode.value) {
                            FLASH_MODE_OFF -> FLASH_MODE_AUTO
                            FLASH_MODE_AUTO -> FLASH_MODE_ON
                            else -> FLASH_MODE_OFF
                        }
                }

                is ViewAction.Exit -> unComposableUiState.finish.value = true

                is ViewAction.OnVideoRecorded -> {
                    if (success) {
                        Media(isImage = false, file = file).apply {
                            viewState.recentMedias.add(0, this)
                            notifyInsert()
                        }
                    } else {
                        Toast.makeText(context, "异常:$exception", Toast.LENGTH_LONG).show()
                        Log.e("OnVideoRecorded异常", exception.toString())
                    }
                }

                is ViewAction.OnPictureTaken -> {
                    viewState.takePictureSemaphore.value--
                    if (success) {
                        Media(isImage = true, file = file).apply {
                            viewState.recentMedias.add(0, this)
                            notifyInsert()
                        }
                    } else {
                        Toast.makeText(context, "异常:$exception", Toast.LENGTH_LONG).show()
                        Log.e("OnPictureTaken异常", exception.toString())
                    }
                }

                is ViewAction.ChangeCameraMode -> viewState.captureImageMode.smartNotifyValue =
                    captureImageMode

                is ViewAction.ChangeRecordVideoTorch ->
                    unComposableUiState.recordVideoTorch.smartNotifyValue =
                        !unComposableUiState.recordVideoTorch.value!!


                is ViewAction.OnGesture -> //这是进行zoom判断的逻辑
                    viewState.zoomState?.let {
                        //这里加一个判断，防止进行平移操作时也认为是放大
                        if (viewState.zoomRatio.value != zoom) {
                            viewState.zoomRatio.smartNotifyValue =
                                (viewState.zoomRatio.value!! * zoom).coerceIn(
                                    it.minZoomRatio,
                                    it.maxZoomRatio
                                )
                            //每当zoom改变时，就会让屏幕上的zoom信息显示出来，2秒后又关闭
                            viewState.showZoomInfo.smartNotifyValue = true
                            TimingExecutor.delayExecute(2000, viewState.showZoomInfo) {
                                viewState.showZoomInfo.smartNotifyValue = false
                            }
                        }

                        //切换阈值，左右滑的速度只要超过这个就算是切换状态了
                        val switchingThreshold = 65
                        if (abs(pan.x) > switchingThreshold) {
                            onAction(ViewAction.ChangeCameraMode(pan.x > 0))
                        }
                    }

                is ViewAction.ChangeZoomState -> {
                    viewState.zoomState = zoomState
                }

                is ViewAction.OpenGallery -> {
                    //TODO 浏览刚拍的照片
                }
            }

        }
    }

    /**
     * 拍照或录像需要有一个文件来保存，这个文件生成的逻辑放在ViewModel这一层
     */
    fun getTargetFile(isImage: Boolean) = File(
        unComposableUiState.currentFileDir,
        "IMG_${System.nanoTime()}.${if (isImage) "jpg" else "mp4"}"
    )

}
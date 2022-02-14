package com.liangguo.cameraxcomposedemo.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.getMainExecutor
import androidx.lifecycle.LifecycleOwner
import com.liangguo.cameraxcomposedemo.CameraViewModel
import com.liangguo.cameraxcomposedemo.logic.ViewAction
import kotlin.math.abs


/**
 * @author ldh
 * 时间: 2022/2/11 10:17
 * 邮箱: 2637614077@qq.com
 */


/**
 * 相机组件，包括了相机的配置和PreviewView的绘制
 */
@SuppressLint("UnrememberedMutableState", "RestrictedApi")
@Composable
fun CameraX(viewModel: CameraViewModel) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context)
    }
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    val videoCapture = remember {
        VideoCapture.Builder
            .fromConfig(VideoCapture.DEFAULT_CONFIG.config)
            .build()
    }

    val captureImageMode by viewModel.viewState.captureImageMode.collectAsState()

    //相机配置
    CameraXLauncher(
        viewModel = viewModel,
        previewView = previewView,
        imageCapture = imageCapture,
        videoCapture = videoCapture,
        captureImageMode = captureImageMode
    )

    //拍照时的闪光灯配置
    ImageCaptureFlashOption(viewModel, imageCapture)

    //画面预览
    CameraPreviewView(viewModel = viewModel, previewView = previewView)

    //进行拍照，采集图片
    TakePicture(viewModel, context, imageCapture)

    //进行录像
    VideoRecord(viewModel = viewModel, videoCapture = videoCapture)


}


/**
 * 录制视频
 */
@SuppressLint("MissingPermission", "RestrictedApi")
@Composable
fun VideoRecord(viewModel: CameraViewModel, videoCapture: VideoCapture) {
    val isRecording by viewModel.viewState.recordingVideo.collectAsState()
    isRecording?.let {
        val context = LocalContext.current
        val file = viewModel.getTargetFile(false)
        file.parentFile?.mkdirs()
        val outputOptions = VideoCapture.OutputFileOptions.Builder(file).build()

        if (it) {
            videoCapture.startRecording(
                outputOptions,
                getMainExecutor(context),
                object : VideoCapture.OnVideoSavedCallback {
                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        viewModel.onAction(
                            ViewAction.OnVideoRecorded(
                                file = file,
                                success = true
                            )
                        )
                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        viewModel.onAction(
                            ViewAction.OnVideoRecorded(
                                file = file,
                                success = false,
                                exception = java.lang.Exception(message, cause)
                            )
                        )
                    }
                })
        } else {
            videoCapture.stopRecording()
            viewModel.onAction(ViewAction.OnRecordStop)
        }
    }
}

/**
 * 这个函数是用来显示相机画面的
 * 其中包含了动画效果
 */
@Composable
fun CameraPreviewView(viewModel: CameraViewModel, previewView: PreviewView) {
    val frontCamera by viewModel.viewState.frontCamera.collectAsState()

    //当切换前后相机时，会执行这个翻转动画
    val rotationY by animateFloatAsState(
        targetValue = if (frontCamera) 0f else 180f, animationSpec = spring(
            stiffness = Spring.StiffnessLow
        )
    )
    //当旋转动画进行的同时还伴随着缩小，最低缩小到0.55
    val rotationScale = 1f - (1f - (abs(rotationY - 90f) / 90f)) * 0.45f

    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures(
                    panZoomLock = true, // 平移或放大时是否可以旋转
                    onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float ->
                        viewModel.onAction(
                            ViewAction.OnGesture(
                                centroid,
                                pan,
                                zoom,
                                rotation
                            )
                        )
                    }
                )
                detectTapGestures {
                    //todo 对焦
                }
            }
            .fillMaxSize()
            .graphicsLayer(
                //这则if表达式可以避免翻转完了过后出现镜像，我真是大聪明[/doge][/doge]
                rotationY = if (rotationY == 180f) 0f else rotationY,
                scaleX = rotationScale,
                scaleY = rotationScale
            )
    )
}

/**
 * 这个函数不起绘制作用，是专门用来给相机调节配置和启动相机的
 * 相机视图不能太频繁的被修改刷新，所以单独放在一个函数里，专门作一些配置工作
 */
@Composable
fun CameraXLauncher(
    viewModel: CameraViewModel,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    videoCapture: VideoCapture,
    captureImageMode: Boolean
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val frontCamera by viewModel.viewState.frontCamera.collectAsState()

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val cameraSelector =
        if (frontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                if (captureImageMode) imageCapture
                else videoCapture
            )
            configCamera(viewModel, lifecycleOwner, camera)
        } catch (e: Exception) {
            Log.e("CameraCore", e.toString())
        }
    }, getMainExecutor(context))
}

/**
 * 为Camera作配置
 */
fun configCamera(viewModel: CameraViewModel, lifecycleOwner: LifecycleOwner, camera: Camera) {
    camera.cameraInfo.zoomState.observe(lifecycleOwner) {
        viewModel.onAction(ViewAction.ChangeZoomState(it))
    }
    //闪光灯
    viewModel.unComposableUiState.recordVideoTorch.observe(lifecycleOwner) {
        camera.cameraControl.enableTorch(it)
    }
    //为Camera设置Zoom
    viewModel.viewState.zoomRatio.observe(lifecycleOwner) {
        camera.cameraControl.setZoomRatio(it)
    }
    //刚开始的时候zoom就统一都设为1吧
    viewModel.viewState.zoomRatio.value = 1f
//    camera.cameraInfo.zoomState.value?.let {
//        viewModel.viewState.zoomRatio.value = it.zoomRatio
//    }

}


/**
 * 拍照时的闪光灯配置
 */
@Composable
fun ImageCaptureFlashOption(viewModel: CameraViewModel, imageCapture: ImageCapture) {
    val flashMode by viewModel.viewState.imageCaptureFlashMode.collectAsState()
    imageCapture.flashMode = flashMode
}

/**
 * 进行拍照的逻辑
 */
@Composable
fun TakePicture(viewModel: CameraViewModel, context: Context, imageCapture: ImageCapture) {
    val takePictureSemaphore by viewModel.viewState.takePictureSemaphore.collectAsState()
    if (takePictureSemaphore > 0) {
        val file = viewModel.getTargetFile(true)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(outputOptions,
            getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewModel.onAction(ViewAction.OnPictureTaken(true, file))
                }

                override fun onError(exception: ImageCaptureException) {
                    viewModel.onAction(
                        ViewAction.OnPictureTaken(
                            false,
                            file,
                            exception
                        )
                    )
                }
            })
    }
}
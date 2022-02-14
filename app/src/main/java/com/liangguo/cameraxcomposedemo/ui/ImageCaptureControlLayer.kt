package com.liangguo.privatalbums.ui.camera.ui

import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import androidx.camera.core.ImageCapture
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.liangguo.cameraxcomposedemo.CameraViewModel
import com.liangguo.cameraxcomposedemo.R
import com.liangguo.cameraxcomposedemo.logic.ViewAction
import com.liangguo.cameraxcomposedemo.ui.RecentMediaThumb
import com.liangguo.cameraxcomposedemo.ui.ReverseCameraButton
import com.liangguo.cameraxcomposedemo.ui.TakePictureButton


/**
 * @author ldh
 * 时间: 2022/2/12 10:15
 * 邮箱: 2637614077@qq.com
 *
 * 这个里面的组件是在照相机之上的一些控制按钮层
 */


/**
 * 用来装一些按钮的控制面，在相机画面之上
 */
@Composable
fun CameraControlLayer(viewModel: CameraViewModel) {
    val context = LocalContext.current
    val chronometer = remember {
        Chronometer(context).apply {
            setTextColor(android.graphics.Color.WHITE)
        }
    }
    ChronometerLogic(viewModel, chronometer)

    CommonControlLayer(viewModel)
    BottomControlLayer(viewModel = viewModel, chronometer = chronometer)
}


/**
 * 通用控制层
 */
@Composable
fun CommonControlLayer(viewModel: CameraViewModel) {
    val context = LocalContext.current
    val videoRecording by viewModel.viewState.recordingVideo.collectAsState()
    //由于目前不知道compose怎么绘制xml中gradient类型的drawable，所以才出此下策让View来装
    val topShadowView = remember {
        View(context).apply {
            setBackgroundResource(R.drawable.gradient_black_top)
        }
    }
    val bottomShadowView = remember {
        View(context).apply {
            setBackgroundResource(R.drawable.gradient_black_bottom)
        }
    }
    AnimatedVisibility(visible = videoRecording != true, enter = fadeIn(), exit = fadeOut()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (
                topShadow,
                bottomShadow,
                cameraModeSwitchPanel,
                clipGridButton,
                flashModeButton,

            ) = createRefs()


            //顶部的控制栏
            createHorizontalChain(
                elements = arrayOf(
                    clipGridButton,
                    flashModeButton,
                ),
                chainStyle = ChainStyle.Packed
            )


            //距离底部24%处有一条线，显示拍照信息什么的就在这条线这个位置显示
            val guidelineInfo = createGuidelineFromBottom(fraction = 0.30f)

            //顶部的阴影栏
            AndroidView(factory = { topShadowView }, modifier = Modifier
                .fillMaxWidth()
                .height(125.dp)
                .alpha(0.8f)
                .constrainAs(topShadow) {
                    top.linkTo(parent.top)
                })

            //底部的阴影栏
            AndroidView(factory = { bottomShadowView }, modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .alpha(0.8f)
                .constrainAs(bottomShadow) {
                    bottom.linkTo(parent.bottom)
                })

            //顶部控制台的按钮栏的分割线
            val guidelineTopPanel = createGuidelineFromTop(50.dp)


            //控制网格显示的按钮
            ClipGridButton(
                modifier = Modifier
                    .constrainAs(clipGridButton) {
                        top.linkTo(guidelineTopPanel)
                    },
                viewModel = viewModel
            )

            //控制闪光灯的按钮
            FlashModeButton(
                modifier = Modifier
                    .constrainAs(flashModeButton) {
                        top.linkTo(guidelineTopPanel)
                    },
                viewModel = viewModel
            )



            //切换相机模式的控制层
            CameraModeSwitchPanel(viewModel = viewModel, modifier = Modifier
                .constrainAs(cameraModeSwitchPanel) {
                    linkTo(
                        start = parent.start,
                        end = parent.end
                    )
                    top.linkTo(guidelineInfo)
                })

        }
    }
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (
            closeButton,
            videoTorchButton,
            cameraZoomText,
        ) = createRefs()
        //顶部控制台的按钮栏的分割线
        val guidelineTopPanel = createGuidelineFromTop(50.dp)
        //距离底部24%处有一条线，显示拍照信息什么的就在这条线这个位置显示
        val guidelineInfo = createGuidelineFromBottom(fraction = 0.30f)
        //关闭相机的按钮
        AnimatedVisibility(
            visible = videoRecording != true,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
            modifier = Modifier
                .constrainAs(closeButton) {
                    top.linkTo(guidelineTopPanel)
                    start.linkTo(parent.start)
                }
        ) {
            CloseButton(
                viewModel = viewModel
            )
        }

        //相机缩放显示的文字
        CameraZoomText(viewModel = viewModel, modifier = Modifier
            .constrainAs(cameraZoomText) {
                linkTo(start = parent.start, end = parent.end)
                bottom.linkTo(guidelineInfo)
            })

        VideoTorchButton(viewModel = viewModel, modifier = Modifier
            .constrainAs(videoTorchButton) {
                top.linkTo(guidelineTopPanel)
                start.linkTo(closeButton.end)
            })

    }
}


//把这些常数拿到compose函数外边来，可以减少在更新时频繁创建对象
private const val CAMERA_MODE_SWITCH_WIDGET_SELECTED_SCALE = 1f
private const val CAMERA_MODE_SWITCH_WIDGET_UNSELECTED_SCALE = 0.8f
private const val CAMERA_MODE_SWITCH_WIDGET_SELECTED_ALPHA = 0.9f
private const val CAMERA_MODE_SWITCH_WIDGET_UNSELECTED_ALPHA = 0.65f
private const val CAMERA_MODE_SWITCH_WIDGET_BACKGROUND_SELECTED_SCALE = 1f
private const val CAMERA_MODE_SWITCH_WIDGET_BACKGROUND_UNSELECTED_SCALE = 0f
private const val CAMERA_MODE_SWITCH_WIDGET_DP_SIZE = 35

/**
 * 切换相机模式的控制层
 */
@Composable
fun CameraModeSwitchPanel(viewModel: CameraViewModel, modifier: Modifier = Modifier) {
    val imageMode by viewModel.viewState.captureImageMode.collectAsState()
    val offsetX by animateDpAsState(targetValue = if (!imageMode) (-12).dp else 12.dp)
    val imageButtonScale by animateFloatAsState(targetValue = if (imageMode) CAMERA_MODE_SWITCH_WIDGET_SELECTED_SCALE else CAMERA_MODE_SWITCH_WIDGET_UNSELECTED_SCALE)
    val videoButtonScale by animateFloatAsState(targetValue = if (!imageMode) CAMERA_MODE_SWITCH_WIDGET_SELECTED_SCALE else CAMERA_MODE_SWITCH_WIDGET_UNSELECTED_SCALE)
    val imageButtonBackgroundScale by animateFloatAsState(targetValue = if (imageMode) CAMERA_MODE_SWITCH_WIDGET_BACKGROUND_SELECTED_SCALE else CAMERA_MODE_SWITCH_WIDGET_BACKGROUND_UNSELECTED_SCALE)
    val videoButtonBackgroundScale by animateFloatAsState(targetValue = if (!imageMode) CAMERA_MODE_SWITCH_WIDGET_BACKGROUND_SELECTED_SCALE else CAMERA_MODE_SWITCH_WIDGET_BACKGROUND_UNSELECTED_SCALE)

    Row(
        modifier = modifier
            .wrapContentSize()
            .offset(x = offsetX)
    ) {
        Box(
            modifier = Modifier
                .size(CAMERA_MODE_SWITCH_WIDGET_DP_SIZE.dp)
                .alpha(if (imageMode) CAMERA_MODE_SWITCH_WIDGET_SELECTED_ALPHA else CAMERA_MODE_SWITCH_WIDGET_UNSELECTED_ALPHA)
                .scale(imageButtonScale), contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(imageButtonBackgroundScale)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {}
            //设置录像模式的按钮
            IconButton(
                onClick = { viewModel.onAction(ViewAction.ChangeCameraMode(true)) }) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = if (imageMode) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        Box(
            modifier = Modifier
                .size(CAMERA_MODE_SWITCH_WIDGET_DP_SIZE.dp)
                .alpha(if (!imageMode) CAMERA_MODE_SWITCH_WIDGET_SELECTED_ALPHA else CAMERA_MODE_SWITCH_WIDGET_UNSELECTED_ALPHA)
                .scale(videoButtonScale), contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(videoButtonBackgroundScale)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {}
            //设置录像模式的按钮
            IconButton(
                onClick = { viewModel.onAction(ViewAction.ChangeCameraMode(false)) }) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = if (!imageMode) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}


/**
 * 底部控制层
 */
@Composable
fun BottomControlLayer(viewModel: CameraViewModel, chronometer: Chronometer) {
    val videoRecording by viewModel.viewState.recordingVideo.collectAsState()
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (
            takePictureButton,
            reverseCameraButton,
            galleryImageView,
            recordTimeInfoWidget,
        ) = createRefs()

        //点击拍照按钮
        TakePictureButton(
            modifier = Modifier
                .constrainAs(takePictureButton) {
                    bottom.linkTo(parent.bottom, margin = 50.dp)
                    centerHorizontallyTo(parent)
                }, viewModel = viewModel
        )

        AnimatedVisibility(visible = videoRecording != true,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .constrainAs(reverseCameraButton) {
                    linkTo(start = parent.start, end = takePictureButton.start)
                    linkTo(top = takePictureButton.top, bottom = takePictureButton.bottom)
                }) {
            //反转相机按钮
            ReverseCameraButton(frontCamera = viewModel.viewState.frontCamera.collectAsState().value) {
                viewModel.onAction(ViewAction.ReverseCamera)
            }
        }

        AnimatedVisibility(visible = videoRecording != true,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .constrainAs(galleryImageView) {
                    linkTo(start = takePictureButton.end, end = parent.end)
                    linkTo(top = takePictureButton.top, bottom = takePictureButton.bottom)
                }) {
            //用来显示最近刚刚拍摄的媒体的缩略图组件
            RecentMediaThumb(viewModel = viewModel)
        }

        AnimatedVisibility(visible = videoRecording == true,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .constrainAs(recordTimeInfoWidget) {
                    linkTo(start = parent.start, end = parent.end)
                    bottom.linkTo(takePictureButton.top, margin = 30.dp)
                }) {
            //用来显示当前录像的时长的控件
            RecordTimeInfoWidget(viewModel = viewModel, chronometer = chronometer)
        }

    }

}

/**
 * 用来显示当前录像的时长的控件
 */
@Composable
fun RecordTimeInfoWidget(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier,
    chronometer: Chronometer
) {
    val animAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 555),
            RepeatMode.Reverse
        )
    )
    Row(
        modifier = modifier
            .height(30.dp)
            .wrapContentWidth()
            .alpha(0.7f)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(percent = 50)
            ), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .alpha(animAlpha)
                .padding(5.dp)
                .background(
                    color = Color.Red,
                    shape = CircleShape
                )
                .size(15.dp)
        )
        AndroidView(factory = { chronometer }, modifier = Modifier.padding(5.dp))
    }
}


/**
 * 这个Compose函数不绘制，而是对Chronometer进行一些设置操作
 */
@Composable
fun ChronometerLogic(viewModel: CameraViewModel, chronometer: Chronometer) {
    val isRecording by viewModel.viewState.recordingVideo.collectAsState()
    isRecording?.let {
        if (it) {
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
        } else {
            chronometer.stop()
        }
    }
}

/**
 * 关闭的按钮
 */
@Composable
fun CloseButton(modifier: Modifier = Modifier, viewModel: CameraViewModel) {
    BaseTopControlButton(
        modifier = modifier,
        onClick = { viewModel.onAction(ViewAction.Exit) },
        imageVector = Icons.Default.Close,
        contentDescription = null
    )
}


/**
 * 控制闪光灯模式的按钮
 */
@Composable
fun FlashModeButton(modifier: Modifier = Modifier, viewModel: CameraViewModel) {
    val imageMode by viewModel.viewState.captureImageMode.collectAsState()
    val frontCamera by viewModel.viewState.frontCamera.collectAsState()
    val onClick = { viewModel.onAction(ViewAction.ChangeImageCaptureFlashMode) }
    val flashMode by viewModel.viewState.imageCaptureFlashMode.collectAsState()
    val iconSize = 24.dp
    val animDpOffsetY by animateDpAsState(
        targetValue = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> -iconSize
            ImageCapture.FLASH_MODE_ON -> iconSize
            else -> 0.dp
        }
    )

    AnimatedVisibility(
        modifier = modifier,
        visible = imageMode && !frontCamera,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally()
    ) {
        IconButton(modifier = Modifier, onClick = onClick) {
            Surface(color = Color.Transparent) {
                Icon(
                    modifier = Modifier.offset(y = animDpOffsetY + iconSize),
                    imageVector = Icons.Default.FlashOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface
                )
                Icon(
                    modifier = Modifier.offset(y = animDpOffsetY),
                    imageVector = Icons.Default.FlashAuto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface
                )
                Icon(
                    modifier = Modifier.offset(y = animDpOffsetY - iconSize),
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        }

    }
}

/**
 * 录制视频时是否启用闪光灯的按钮
 */
@Composable
fun VideoTorchButton(modifier: Modifier = Modifier, viewModel: CameraViewModel) {
    val captureImageMode by viewModel.viewState.captureImageMode.collectAsState()
    val frontCamera by viewModel.viewState.frontCamera.collectAsState()
    AnimatedVisibility(
        modifier = modifier,
        visible = !captureImageMode && !frontCamera,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
    ) {
        VideoTorchButtonContent(viewModel = viewModel)
    }
}


/**
 * 录制视频时是否启用闪光灯的按钮里面显示的内容，图标这些
 */
@Composable
fun VideoTorchButtonContent(modifier: Modifier = Modifier, viewModel: CameraViewModel) {
    val recordVideoTorch by viewModel.unComposableUiState.recordVideoTorch.observeAsState()
    val rotationY by animateFloatAsState(
        targetValue = if (recordVideoTorch == true) 180f else 0f, animationSpec = spring(
            stiffness = Spring.StiffnessLow
        )
    )
    BaseTopControlButton(
        modifier = modifier,
        innerIconModifier = Modifier.graphicsLayer(rotationY = rotationY),
        onClick = {
            viewModel.onAction(ViewAction.ChangeRecordVideoTorch)
        },
        imageVector = if (rotationY > 90) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
        contentDescription = null,
    )
}

/**
 * 控制是否显示网格的按钮
 */
@Composable
fun ClipGridButton(modifier: Modifier = Modifier, viewModel: CameraViewModel) {
    val showClipGrid by viewModel.viewState.showClipGrid.collectAsState()
    //当切换前后相机时，会执行这个翻转动画
    val rotationY by animateFloatAsState(
        targetValue = if (showClipGrid) 180f else 0f, animationSpec = spring(
            stiffness = Spring.StiffnessLow
        )
    )
    BaseTopControlButton(
        modifier = modifier,
        innerIconModifier = Modifier.graphicsLayer(rotationY = rotationY),
        onClick = {
            viewModel.onAction(ViewAction.ChangeGridViewVisibility)
        },
        imageVector = if (rotationY > 90) Icons.Default.GridOn else Icons.Default.GridOff,
        contentDescription = null,
    )
}

/**
 * 在屏幕上显示相机缩放的文字
 */
@Composable
fun CameraZoomText(viewModel: CameraViewModel, modifier: Modifier) {
    val zoom by viewModel.viewState.zoomRatio.observeAsState()
    val showZoomInfo by viewModel.viewState.showZoomInfo.collectAsState()
    AnimatedVisibility(visible = showZoomInfo, modifier = modifier) {
        Text(
            modifier = Modifier.alpha(0.8f),
            fontSize = 38.sp,
            text = "× ${String.format("%.1f", zoom)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.surface
        )

    }
}


/**
 * 显示在相机上的分割线
 */
@Composable
fun ClipGridView(viewModel: CameraViewModel) {
    val showClipGrid by viewModel.viewState.showClipGrid.collectAsState()
    //线的宽度。动画显示和隐藏分割线
    val lineWidth: Dp by animateDpAsState(targetValue = if (showClipGrid) 1.15f.dp else 0.dp)
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.55f)
    ) {
        val oneThread = 1 / 3f
        val (vertical1, vertical2, horizontal1, horizontal2) = createRefs()
        val guidelineV1 = createGuidelineFromTop(fraction = oneThread)
        val guidelineV2 = createGuidelineFromBottom(fraction = oneThread)
        val guidelineH1 = createGuidelineFromStart(fraction = oneThread)
        val guidelineH2 = createGuidelineFromEnd(fraction = oneThread)
        ClipLine(
            Modifier
                .constrainAs(vertical1) {
                    top.linkTo(guidelineV1)
                }
                .fillMaxWidth()
                .height(lineWidth)
        )
        ClipLine(
            Modifier
                .constrainAs(vertical2) {
                    top.linkTo(guidelineV2)
                }
                .fillMaxWidth()
                .height(lineWidth)
        )
        ClipLine(
            Modifier
                .constrainAs(horizontal1) {
                    start.linkTo(guidelineH1)
                }
                .fillMaxHeight()
                .width(lineWidth)
        )
        ClipLine(
            Modifier
                .constrainAs(horizontal2) {
                    start.linkTo(guidelineH2)
                }
                .fillMaxHeight()
                .width(lineWidth)
        )
    }
}

/**
 * 顶部的控制按钮
 */
@Composable
fun BaseTopControlButton(
    modifier: Modifier = Modifier,
    innerIconModifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String? = null
) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            modifier = innerIconModifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * 分割线
 */
@Composable
fun ClipLine(modifier: Modifier) {
    Surface(
        color = Color.White,
        modifier = modifier
    ) {}
}
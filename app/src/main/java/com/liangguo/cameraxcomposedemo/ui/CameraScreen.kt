package com.liangguo.cameraxcomposedemo.ui

import android.widget.ImageView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraFront
import androidx.compose.material.icons.outlined.CameraRear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.liangguo.cameraxcomposedemo.CameraViewModel
import com.liangguo.cameraxcomposedemo.logic.ViewAction
import com.liangguo.cameraxcomposedemo.utils.loadMedia
import com.liangguo.privatalbums.ui.camera.ui.CameraControlLayer
import com.liangguo.privatalbums.ui.camera.ui.ClipGridView


/**
 * @author ldh
 * 时间: 2022/2/9 11:45
 * 邮箱: 2637614077@qq.com
 */


/**
 * 相机界面的入口Composable函数
 */
@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    Surface(color = Color.Transparent) {
        //相机画面预览
        CameraX(viewModel)

        //分割线
        ClipGridView(viewModel)

        //按钮组件层
        CameraControlLayer(viewModel)

    }
}

/**
 * 最近拍摄的媒体用这个来显示，包括了View和媒体加载器
 */
@Composable
fun RecentMediaThumb(viewModel: CameraViewModel, modifier: Modifier = Modifier) {
    //用于需要涉及到Glide加载，所以这里用安卓的ImageView来实现
    val context = LocalContext.current
    val imageView = remember {
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }
    //View部分
    MediaThumbImageView(viewModel = viewModel, imageView = imageView, modifier = modifier)

    //加载图片部分
    ImageViewSetMedia(viewModel, imageView)
}

/**
 * 用来显示最近刚刚拍摄的媒体的缩略图组件
 */
@Composable
fun MediaThumbImageView(
    viewModel: CameraViewModel,
    modifier: Modifier,
    imageView: ImageView
) {
    val animSize by animateDpAsState(targetValue = if (viewModel.viewState.recentMedias.isEmpty()) 0.dp else 50.dp)
    IconButton(
        modifier = modifier
            .size(animSize)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(2.dp),
        onClick = { viewModel.onAction(ViewAction.OpenGallery) }
    ) {
        AndroidView(
            factory = { imageView }, modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

/**
 * 这个组件专门用来加载媒体缩略图
 */
@Composable
fun ImageViewSetMedia(viewModel: CameraViewModel, imageView: ImageView) {
    viewModel.viewState.recentMedias.firstOrNull()?.let {
        imageView.loadMedia(it)
    }
}

/**
 * 点击拍照的按钮
 */
@Composable
fun TakePictureButton(modifier: Modifier = Modifier, viewModel: CameraViewModel) {
    val imageRecording by viewModel.viewState.recordingVideo.collectAsState()
    val captureImageMode by viewModel.viewState.captureImageMode.collectAsState()
    val animColor by animateColorAsState(targetValue = if (captureImageMode) MaterialTheme.colorScheme.surface else Color.Red)
    val animPadding by animateDpAsState(targetValue = if (imageRecording == true && !captureImageMode) 20.dp else if (!captureImageMode) 15.dp else 10.dp)
    IconButton(
        modifier = modifier
            .size(70.dp)
            .alpha(0.85f)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        onClick = { viewModel.onAction(ViewAction.TakePicture) },
    ) {
        Surface(
            color = animColor,
            shape = if (imageRecording == true && !captureImageMode) RoundedCornerShape(4.dp) else CircleShape,
            modifier = Modifier
                .padding(animPadding)
                .fillMaxSize()
        ) {}
    }
}

/**
 * 反转相机的按钮，控制相机拍摄哪一面
 * @param frontCamera 使用的是哪一个摄像头。 true为CameraSelector.DEFAULT_FRONT_CAMERA， false为CameraSelector.DEFAULT_BACK_CAMERA
 */
@Composable
fun ReverseCameraButton(
    modifier: Modifier = Modifier,
    frontCamera: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .size(40.dp)
            .alpha(0.82f)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        onClick = onClick,
    ) {
        Icon(
            modifier = modifier.wrapContentSize(),
            imageVector = if (frontCamera) Icons.Outlined.CameraFront else Icons.Outlined.CameraRear,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}


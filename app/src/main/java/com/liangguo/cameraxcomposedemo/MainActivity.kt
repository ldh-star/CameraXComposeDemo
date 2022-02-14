package com.liangguo.cameraxcomposedemo

import android.Manifest.permission.*
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.liangguo.cameraxcomposedemo.logic.ViewAction
import com.liangguo.cameraxcomposedemo.ui.CameraScreen
import com.liangguo.claritypermission.requestPermissions
import com.zackratos.ultimatebarx.ultimatebarx.statusBar


class MainActivity : AppCompatActivity() {

    private val mViewModel by lazy {
        ViewModelProvider(this)[CameraViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBar {
            fitWindow = false
            light = false
        }
        requestPermissions(
            CAMERA,
            RECORD_AUDIO,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
        ).granted {
            setContent {
                CameraScreen(viewModel = mViewModel)
            }
        }
        initDataListener()
    }


    private fun initDataListener() {
        mViewModel.unComposableUiState.finish.observe(this) {
            if (it) finish()
        }
    }


    override fun onBackPressed() {
        if (mViewModel.viewState.recordingVideo.value == true) {
            mViewModel.onAction(ViewAction.TakePicture)
            return
        }
        super.onBackPressed()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                //进行拍照
                mViewModel.onAction(ViewAction.TakePicture)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                //进行拍照
                mViewModel.onAction(ViewAction.TakePicture)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}
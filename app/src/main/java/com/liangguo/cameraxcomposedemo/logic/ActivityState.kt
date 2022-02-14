package com.liangguo.cameraxcomposedemo.logic

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import java.io.File

/**
 * @author ldh
 *
 * 这里面的数据一般不用来传入compose，但也是属于界面的数据
 */
class ActivityState {
    /**
     * 当前拍摄的照片储存的文件夹是什么
     */
    val currentFileDir by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "CameraXComposeDemo"
        ).apply {
            mkdirs()
        }
    }

    /**
     * 当这个值变为true以后，activity应该终结它自己
     */
    var finish = MutableLiveData(false)

    /**
     * 录像时后置摄像头是否开启
     */
    val recordVideoTorch = MutableLiveData(false)

}

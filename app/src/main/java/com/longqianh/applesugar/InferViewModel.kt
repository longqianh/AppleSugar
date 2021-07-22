package com.longqianh.applesugar

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.longqianh.applesugar.extensions.toBitmap
import com.longqianh.applesugar.extensions.toGray
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class InferViewModel : ViewModel() {


    var features = DoubleArray(8) { _ -> 0.0 }
    var bk = doubleArrayOf(
        0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
    )




//    var inferButtonSelect= BooleanArray(7){_->false}
//    var bkButtonSelect= BooleanArray(7){_->false}
//    var lightControlSelect = BooleanArray(7){_->false}

}
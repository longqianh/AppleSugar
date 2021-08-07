package com.longqianh.applesugar

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import com.longqianh.applesugar.extensions.toBitmap
import com.longqianh.applesugar.extensions.toGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class Utils {


    companion object
    {
        private fun loadModelFile(modelPath: String, am: AssetManager): MappedByteBuffer? {

            val fileDescriptor: AssetFileDescriptor = am.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel: FileChannel = inputStream.channel
            val startOffset: Long = fileDescriptor.startOffset
            val declaredLength: Long = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }

        suspend fun calSugar(sequence: DoubleArray,modelSwitch:Boolean,am:AssetManager): Float =
        withContext(Dispatchers.Default)
        {

            val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
//            if(modelSwitch)
//            {
//                val modelPath="model_reg.tflite"
//                val interpreter = Interpreter(loadModelFile(modelPath,am)!!)
//                val output: Array<FloatArray> = arrayOf(FloatArray(1))
//                interpreter.run(inputs, output)
//                return@withContext output[0][0]
//            }
            if(modelSwitch)
            {
                val modelPath="model_yellow-all.tflite"
                val interpreter = Interpreter(loadModelFile(modelPath,am)!!)
                val output: Array<FloatArray> = arrayOf(FloatArray(91))
                interpreter.run(inputs, output)
                val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
                return@withContext (9.0+maxIdx*0.1).toFloat()
            }
            else{
                val modelPath="model_red_shuffle_96.tflite"
//                val modelPath2="model_cla1.tflite"
                val interpreter = Interpreter(loadModelFile(modelPath,am)!!)
//                val interpreter2 = Interpreter(loadModelFile(modelPath2,am)!!)
                val output: Array<FloatArray> = arrayOf(FloatArray(91))
//            Toast.makeText(requireContext(),"Result: $maxIdx,${output[0][maxIdx]}",Toast.LENGTH_SHORT).show()
                interpreter.run(inputs, output)
                val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: 0
//                interpreter2.run(inputs, output)
//                val maxIdx2 = output[0].indices.maxByOrNull { output[0][it] } ?: 0
//            Toast.makeText(requireContext(),"model1: ${8.0+maxIdx1*0.1}, model2: ${8.0+maxIdx2*0.1}",
//                Toast.LENGTH_LONG).show()
//                return@withContext (8.0 + (maxIdx1+maxIdx2)*0.5 * 0.1).toFloat()
                return@withContext (9.0+maxIdx*0.1).toFloat()
            }

        }


        @RequiresApi(Build.VERSION_CODES.P)
        suspend fun getIntensityFromUri(uri: Uri, contentResolver: ContentResolver): Double {
//        val source = ImageDecoder.createSource(contentResolver, uri)
            val bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
            return calIntensity(bitmap)
        }


        suspend fun getIntensityFromPath(path: String, am:AssetManager): Double {
            val istm: InputStream = am.open(path)
            val bitmap = BitmapFactory.decodeStream(istm)
            return calIntensity(bitmap)
        }

        fun getIntensityFromImageProxy(image:ImageProxy)
        {
            //
        }

        fun measureTimeMillis(block: () -> Unit): Long {
            val start = System.currentTimeMillis()
            block()
            return System.currentTimeMillis() - start
        }


        private suspend fun calIntensity(bitmap: Bitmap):Double = withContext(Dispatchers.Default)
        {
            val mat = Mat()
            mat.toGray(bitmap)
            val sz: Size = Size(640.0, 480.0)
            Imgproc.resize(mat, mat, sz)
//            image_origin.setImageBitmap(mat.toBitmap())
//            val binary = Mat()
//            Imgproc.threshold(mat, mat, 200.0, 255.0, Imgproc.THRESH_TOZERO_INV)
//            Imgproc.threshold(mat, binary, 0.0, 255.0, Imgproc.THRESH_OTSU)
//            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0))
//            Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernel)
//            Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_CLOSE, kernel)
//
//            val contour = MatOfPoint()
//            Core.findNonZero(binary, contour)
//            val y_index = mutableListOf<Double>()
//            val x_index = mutableListOf<Double>()
//
//            contour.toArray().forEach {
//                y_index.add(it.x) // col index
//                x_index.add(it.y) // row index
//            }

//            val c_y = (y_index.maxOrNull()!! + y_index.minOrNull()!!) / 2 // center
//            val c_x = (x_index.maxOrNull()!! + x_index.minOrNull()!!) / 2
            val c_y=320.0
            val c_x=240.0

//        val r_y = (y_index.maxOrNull()!!-y_index.minOrNull()!!) / 2 // radius
//        val r_x = (x_index.maxOrNull()!!-x_index.minOrNull()!!) / 2
//        val r_max = if(r_y>r_x) r_y else r_x
            val r_max = 200.0
            val in_bound = 0.85
            val out_bound = 0.95

            val thres = 20.0
            for (y in 0..mat.cols()) {
                for (x in 0..mat.rows()) {
                    val dist = sqrt((x - c_x) * (x - c_x) + (y - c_y) * (y - c_y))
                    if (dist >= out_bound * r_max || dist <= in_bound * r_max)
                        mat.put(x, y, 0.0)
                }
            }
            val mask = Mat()
            Imgproc.threshold(mat, mask, thres, 255.0, Imgproc.THRESH_BINARY)
            mat.mul(mask)
//        image_processed.setImageBitmap(mat.toBitmap())
            val mean_val = MatOfDouble()
            val std_val = MatOfDouble()
            Core.meanStdDev(mat, mean_val, std_val, mask)

//            binary.release()
            mask.release()
            std_val.release()
            mat.release()
            return@withContext mean_val.toArray()[0]
        }

//        image_origin.setImageBitmap(procbitmap)
        }
    private fun drawContour(bitmap: Bitmap) {
        val mat = Mat()
        mat.toGray(bitmap)
        val binary = Mat()
        Imgproc.threshold(mat, binary, 0.0, 255.0, Imgproc.THRESH_OTSU)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            binary,
            contours,
            hierarchy,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        val color = Scalar(120.0, 155.0, 0.0)
        Log.d("drawContour", "${contours.get(0).size()}")
        Imgproc.drawContours(mat, contours, -1, color)
        val procbitmap = mat.toBitmap()          // Dispatchers.IO (main-safety block)

    }



}

package com.longqianh.applesugar

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.longqianh.applesugar.extensions.toBitmap
import com.longqianh.applesugar.extensions.toGray
import kotlinx.coroutines.delay
import org.opencv.core.*
import org.opencv.core.Core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import org.tensorflow.lite.Interpreter
import org.w3c.dom.Text
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class inferFragment: Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() = inferFragment()
    }

    private lateinit var getContent: ActivityResultLauncher<String>
    private var intensity: Double = 0.0
    private var features = DoubleArray(7) { _ -> 0.0 }
    private lateinit var viewModel: InferViewModel
    private lateinit var image_origin: ImageView
    private lateinit var image_processed: ImageView
    private lateinit var contentResolver: ContentResolver
    private lateinit var am: AssetManager
    private lateinit var sugar_text: TextView
    private lateinit var show_sugar_text: TextView
//    private var buttonPressed= BooleanArray(7){_->false}
    private var bk = doubleArrayOf(
        76.50252212,
        81.3847853,
        61.46050539,
        58.0978088,
        59.74946987,
        59.76914235,
        61.46982301
    )

    private var is_reg=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.infer_fragment, container, false)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri ->
            intensity = getIntensity(uri)
        }
        val back_button: Button = view.findViewById(R.id.infer_back_button)

        val button_680: Button = view.findViewById(R.id.pick680_button)
        val button_700: Button = view.findViewById(R.id.pick700_button)
        val button_720: Button = view.findViewById(R.id.pick720_button)
        val button_760: Button = view.findViewById(R.id.pick760_button)
        val button_780: Button = view.findViewById(R.id.pick780_button)
        val button_800: Button = view.findViewById(R.id.pick800_button)
        val button_810: Button = view.findViewById(R.id.pick810_button)
        val button_fresh: Button = view.findViewById(R.id.pick_button)
        val get_sugar_button: Button = view.findViewById(R.id.get_sugar_button)
        val model_switch: Switch = view.findViewById(R.id.model_switch)
        sugar_text = view.findViewById(R.id.sugar_text)
        show_sugar_text = view.findViewById(R.id.show_sugar_text)
        sugar_text.visibility = View.GONE
        image_origin = view.findViewById(R.id.image_origin)
        image_processed = view.findViewById(R.id.image_processed)
        am = requireContext().assets

//        val testPath="680.jpg"
//        val testIntensity=testIntensity(testPath)
//        Toast.makeText(requireContext(),"Test: $testIntensity",Toast.LENGTH_LONG).show()

        back_button.setOnClickListener(this)
        button_680.setOnClickListener(this) // click 后会调用 onClick
        button_700.setOnClickListener(this)
        button_720.setOnClickListener(this)
        button_760.setOnClickListener(this)
        button_780.setOnClickListener(this)
        button_800.setOnClickListener(this)
        button_810.setOnClickListener(this)
        button_fresh.setOnClickListener(this)
        get_sugar_button.setOnClickListener(this)
        model_switch.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(v: View?) {

        when (v!!.id) {

            R.id.pick680_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(0, intensity / bk[0])
                show_sugar_text.text = "${features[0]}"
            }

            R.id.pick700_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(1, intensity / bk[1])
                show_sugar_text.text = "${features[1]}"
            }

            R.id.pick720_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(2, intensity / bk[2])
                show_sugar_text.text = "${features[2]}"
            }

            R.id.pick760_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(3, intensity / bk[3])
                show_sugar_text.text = "${features[3]}"
            }

            R.id.pick780_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(4, intensity / bk[4])
                show_sugar_text.text = "${features[4]}"
            }

            R.id.pick800_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(5, intensity / bk[5])
                show_sugar_text.text = "${features[5]}"
            }

            R.id.pick810_button -> {
                val btn = v.findViewById<Button>(v.id)
                btn.isSelected=!btn.isSelected
                features.set(6, intensity / bk[6])
                show_sugar_text.text = "${features[6]}"
            }

            R.id.get_sugar_button -> {
                Toast.makeText(
                    requireContext(),
                    "input features: [${features[0]},${features[1]}," +
                            "${features[2]},${features[3]},${features[4]},${features[5]},${features[6]}]",
                    Toast.LENGTH_LONG
                ).show()
                val sugar = calSugar(features)
                sugar_text.visibility = View.VISIBLE
                sugar_text.setText("Apple sugar: $sugar Brix")

            }

            R.id.infer_back_button -> {
//                resetButton(v)
                Navigation.findNavController(v).navigateUp()
            }
            R.id.pick_button -> {
                getContent.launch("image/*")
            }

            R.id.model_switch -> {
                is_reg=!is_reg
                if(is_reg==true)
                {
                    Toast.makeText(requireContext(),"Change to Regresion Model.",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(),"Change to Classification Model.",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                Navigation.findNavController(requireView()).navigate(R.id.action_inferFragment_to_aboutFragment)
                true
            }
            R.id.menu_help ->{
                Navigation.findNavController(requireView()).navigate(R.id.action_inferFragment_to_helpFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun loadModelFile(modelPath: String): MappedByteBuffer? {

        val fileDescriptor: AssetFileDescriptor = am.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun calSugar(sequence: DoubleArray): Float {

        val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
        if(is_reg==true)
        {
            val modelPath="model_reg6.tflite"
            val interpreter = Interpreter(loadModelFile(modelPath)!!)
            val output: Array<FloatArray> = arrayOf(FloatArray(1))
            interpreter.run(inputs, output)
            return output[0][0]
        }
        else{
            val modelPath1="model_cla_test.tflite"
            val modelPath2="model_cla1.tflite"
            val interpreter1 = Interpreter(loadModelFile(modelPath1)!!)
            val interpreter2 = Interpreter(loadModelFile(modelPath2)!!)
            val output: Array<FloatArray> = arrayOf(FloatArray(101))
//            Toast.makeText(requireContext(),"Result: $maxIdx,${output[0][maxIdx]}",Toast.LENGTH_SHORT).show()
            interpreter1.run(inputs, output)
            val maxIdx1 = output[0].indices.maxByOrNull { output[0][it] } ?: 0
            interpreter2.run(inputs, output)
            val maxIdx2 = output[0].indices.maxByOrNull { output[0][it] } ?: 0
            Toast.makeText(requireContext(),"model1: ${8.0+maxIdx1*0.1}, model2: ${8.0+maxIdx2*0.1}",Toast.LENGTH_LONG).show()
            return (8.0 + (maxIdx1+maxIdx2)*0.5 * 0.1).toFloat()
        }

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getIntensity(uri: Uri): Double {
        contentResolver = requireContext().getContentResolver()
//        val source = ImageDecoder.createSource(contentResolver, uri)
        val bitmap: Bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))

        val mat = Mat()
        mat.toGray(bitmap)
        val sz: Size = Size(640.0, 480.0)
        resize(mat, mat, sz)
        image_origin.setImageBitmap(mat.toBitmap())
        val binary = Mat()
        threshold(mat, mat, 200.0, 255.0, THRESH_TOZERO_INV)
        threshold(mat, binary, 0.0, 255.0, THRESH_OTSU)
        val kernel = Imgproc.getStructuringElement(MORPH_RECT, Size(2.0, 2.0))
        morphologyEx(binary, binary, MORPH_OPEN, kernel)
        morphologyEx(binary, binary, MORPH_CLOSE, kernel)

        val contour = MatOfPoint()
        findNonZero(binary, contour)
        val y_index = mutableListOf<Double>()
        val x_index = mutableListOf<Double>()

        contour.toArray().forEach {
            y_index.add(it.x) // col index
            x_index.add(it.y) // row index
        }

        val c_y = (y_index.maxOrNull()!! + y_index.minOrNull()!!) / 2 // center
        val c_x = (x_index.maxOrNull()!! + x_index.minOrNull()!!) / 2
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
        threshold(mat, mask, thres, 255.0, THRESH_BINARY)
        mat.mul(mask)
        image_processed.setImageBitmap(mat.toBitmap())
        val mean_val = MatOfDouble()
        val std_val = MatOfDouble()
        meanStdDev(mat, mean_val, std_val, mask)

        binary.release()
        mask.release()
        std_val.release()
        mat.release()

        Log.d("getIntensity", "$mean_val.toArray()[0]")

        return mean_val.toArray()[0]

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun testIntensity(path: String): Double {
        val istm: InputStream = am.open(path)
        val bitmap = BitmapFactory.decodeStream(istm)
        image_origin.setImageBitmap(bitmap)
        val mat = Mat()
        mat.toGray(bitmap)
//        val resizeimage:Mat = Mat()
        val sz: Size = Size(640.0, 480.0)
        resize(mat, mat, sz)
//        val procBitmap:Bitmap=mat.toBitmap()
        val binary = Mat()
        threshold(mat, binary, 200.0, 255.0, THRESH_TOZERO_INV)
        threshold(mat, binary, 0.0, 255.0, THRESH_OTSU)
        val kernel = getStructuringElement(MORPH_RECT, Size(2.0, 2.0))
        morphologyEx(binary, binary, MORPH_OPEN, kernel)
        morphologyEx(binary, binary, MORPH_CLOSE, kernel)

        val contour = MatOfPoint()
        findNonZero(binary, contour)
        val y_index = mutableListOf<Double>()
        val x_index = mutableListOf<Double>()

        contour.toArray().forEach {
            y_index.add(it.x) // col index
            x_index.add(it.y) // row index
        }

        val c_y = (y_index.maxOrNull()!! + y_index.minOrNull()!!) / 2 // center
        val c_x = (x_index.maxOrNull()!! + x_index.minOrNull()!!) / 2
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
        threshold(mat, mask, thres, 255.0, THRESH_BINARY)
        mat.mul(mask)
        image_processed.setImageBitmap(mat.toBitmap())
        val mean_val = MatOfDouble()
        val std_val = MatOfDouble()
        meanStdDev(mat, mean_val, std_val, mask)
//        val res=mean(mat,mask)
//        println("${res::class.qualifiedName}")
//        Log.d("res","${res[0]}, ${res[1]}, ${res[2]}, ${res[3]}")

        binary.release()
        mask.release()
        std_val.release()
        mat.release()

        return mean_val.toArray()[0]

    }


    private fun drawContour(bitmap: Bitmap) {
        val mat = Mat()
        mat.toGray(bitmap)
        val binary = Mat()
        threshold(mat, binary, 0.0, 255.0, THRESH_OTSU)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        findContours(binary, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE)
        val color = Scalar(120.0, 155.0, 0.0)
        Log.d("drawContour", "${contours.get(0).size()}")
        drawContours(mat, contours, -1, color)
        val procbitmap = mat.toBitmap()
        image_origin.setImageBitmap(procbitmap)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InferViewModel::class.java)
        // TODO: Use the ViewModel
    }

}

// todo: be able to manually set the bk value
// todo: debug why it become slower when pick more times (memory leak?)
// todo: layout beautify
// todo: slide back should be confirmed
// todo: button data binding
// todo: softmax need not

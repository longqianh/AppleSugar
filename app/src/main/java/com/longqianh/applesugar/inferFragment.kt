package com.longqianh.applesugar

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.longqianh.applesugar.extensions.toBitmap
import com.longqianh.applesugar.extensions.toGray
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
    private var intensity:Double=0.0
    private var features=DoubleArray(7){ _ ->0.0}
    private lateinit var viewModel: InferViewModel
    private lateinit var image_origin:ImageView
    private lateinit var image_processed:ImageView
    private lateinit var contentResolver: ContentResolver
    private lateinit var am:AssetManager
    private lateinit var sugar_text:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.infer_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri ->
            intensity=getIntensity(uri)
        }

        val back_button: Button = view.findViewById(R.id.infer_back_button)

        val button_680: Button = view.findViewById(R.id.pick680_button)
        val button_720: Button = view.findViewById(R.id.pick720_button)
        val button_760: Button = view.findViewById(R.id.pick760_button)
        val button_780: Button = view.findViewById(R.id.pick780_button)
        val button_800: Button = view.findViewById(R.id.pick800_button)
        val button_810: Button = view.findViewById(R.id.pick810_button)
        val get_sugar_button: Button = view.findViewById(R.id.get_sugar_button)
        sugar_text = view.findViewById(R.id.sugar_text)
        sugar_text.visibility=View.GONE
        image_origin = view.findViewById(R.id.image_origin)
        image_processed = view.findViewById(R.id.image_processed)
        am = requireContext().assets

//        val testPath="test.jpg"
//        val testIntensity=testIntensity(testPath)
//        Toast.makeText(requireContext(),"Test: $testIntensity",Toast.LENGTH_LONG).show()


        back_button.setOnClickListener(this)
        button_680.setOnClickListener(this) // click 后会调用 onClick
        button_720.setOnClickListener(this)
        button_760.setOnClickListener(this)
        button_780.setOnClickListener(this)
        button_800.setOnClickListener(this)
        button_810.setOnClickListener(this)
        get_sugar_button.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(v: View?) {


        when(v!!.id) {

            R.id.pick680_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(0, intensity)
                Toast.makeText(requireContext(), "Button680: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button680", "$intensity")
            }

            R.id.pick700_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(1, intensity)
                Toast.makeText(requireContext(), "Button700: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button700", "$intensity")
            }

            R.id.pick720_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(2, intensity)
                Toast.makeText(requireContext(), "Button720: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button720", "$intensity")
            }

            R.id.pick760_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(3, intensity)
                Toast.makeText(requireContext(), "Button760: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button760", "$intensity")
            }

            R.id.pick780_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(4, intensity)
                Toast.makeText(requireContext(), "Button780: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button780", "$intensity")
            }

            R.id.pick800_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(5, intensity)
                Toast.makeText(requireContext(), "Button800: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button800", "$intensity")
            }

            R.id.pick810_button -> {
                getContent.launch("image/*")
                val btn= v.findViewById<Button>(v.id)
                btn.setBackgroundColor(Color.GREEN)
                features.set(6, intensity)
                Toast.makeText(requireContext(), "Button810: $intensity", Toast.LENGTH_SHORT).show()
                Log.i("Button810", "$intensity")
            }

            R.id.get_sugar_button -> {
                Toast.makeText(requireContext(),"input features: [${features[0]},${features[1]}," +
                        "${features[2]},${features[3]},${features[4]},${features[5]},${features[6]}]",Toast.LENGTH_LONG).show()
                val sugar=calSugar(features)
                sugar_text.visibility=View.VISIBLE
                sugar_text.setText("Apple sugar: $sugar Brix")

            }

            R.id.infer_back_button -> {
//                resetButton(v)
                Navigation.findNavController(v).navigateUp()
            }

        }
    }



//    private fun resetButton(v:View)
//    {
        // todo: button data binding
//        val btn_680=v.findViewById<Button>(R.id.pick680_button)
//        val btn_720=v.findViewById<Button>(R.id.pick720_button)
//        val btn_760=v.findViewById<Button>(R.id.pick760_button)
//        val btn_780=v.findViewById<Button>(R.id.pick780_button)
//        val btn_800=v.findViewById<Button>(R.id.pick800_button)
//        val btn_810=v.findViewById<Button>(R.id.pick810_button)
//
//        btn_680.setBackgroundResource(android.R.drawable.btn_default);
//        btn_720.setBackgroundResource(android.R.drawable.btn_default);
//        btn_760.setBackgroundResource(android.R.drawable.btn_default);
//        btn_780.setBackgroundResource(android.R.drawable.btn_default);
//        btn_800.setBackgroundResource(android.R.drawable.btn_default);
//        btn_810.setBackgroundResource(android.R.drawable.btn_default);
//        每次切换的时候Fragment都会重新实列化，重新加载一次数据
//        sugar_text.visibility=View.GONE
//    }

    private fun loadModelFile(): MappedByteBuffer? {
        val fileDescriptor: AssetFileDescriptor = am.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun calSugar(sequence:DoubleArray):Double
    {
        val interpreter = Interpreter(loadModelFile()!!)
        val inputs : Array<FloatArray> = arrayOf( sequence.map{ it.toFloat() }.toFloatArray() )
        val output: Array<FloatArray> = arrayOf(FloatArray(101))

        interpreter.run( inputs , output )
        val maxIdx = output[0].indices.maxByOrNull{ output[0][it] }?:0
//        Toast.makeText(requireContext(),"Result: $maxIdx,${output[0][maxIdx]}",Toast.LENGTH_SHORT).show()
        return 8.0+maxIdx*0.1
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun getIntensity(uri: Uri): Double
    {
        contentResolver=requireContext().getContentResolver()
        val source = ImageDecoder.createSource(contentResolver, uri)
        var bitmap:Bitmap=BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        image_origin.setImageBitmap(bitmap)
        val mat = Mat()
        mat.toGray(bitmap)
        val sz:Size = Size(640.0,480.0)
        resize(mat, mat, sz )
        val binary=Mat()
        threshold(mat,mat, 200.0, 255.0, THRESH_TOZERO_INV)
        threshold(mat,binary, 0.0, 255.0, THRESH_OTSU)
        val kernel = Imgproc.getStructuringElement(MORPH_RECT, Size(2.0, 2.0))
        morphologyEx(binary,binary,MORPH_OPEN, kernel)
        morphologyEx(binary,binary,MORPH_CLOSE, kernel)

        val contour= MatOfPoint()
        findNonZero(binary,contour)
        val y_index = mutableListOf<Double>()
        val x_index = mutableListOf<Double>()

        contour.toArray().forEach {
            y_index.add(it.x) // col index
            x_index.add(it.y) // row index
        }

        val c_y = (y_index.maxOrNull()!!+y_index.minOrNull()!!) / 2 // center
        val c_x = (x_index.maxOrNull()!!+x_index.minOrNull()!!) / 2
        val r_y = (y_index.maxOrNull()!!-y_index.minOrNull()!!) / 2 // radius
        val r_x = (x_index.maxOrNull()!!-x_index.minOrNull()!!) / 2
        val r_max = if(r_y>r_x) r_y else r_x

        val in_bound=0.85
        val out_bound=0.95

        val thres=20.0
        for(y in 0..mat.cols())
        {
            for (x in 0..mat.rows())
            {
                val dist=sqrt((x-c_x)*(x-c_x)+(y-c_y)*(y-c_y))
                if(dist>=out_bound*r_max||dist<=in_bound*r_max)
                    mat.put(x,y,0.0)
            }
        }
        val mask=Mat()
        threshold(mat,mask,thres,255.0, THRESH_BINARY)
        mat.mul(mask)
        image_processed.setImageBitmap(mat.toBitmap())
        val mean_val=MatOfDouble()
        val std_val=MatOfDouble()
        meanStdDev(mat,mean_val,std_val,mask)

        binary.release()
        mask.release()
        std_val.release()
        mat.release()

        Log.d("getIntensity","$mean_val.toArray()[0]")

        return mean_val.toArray()[0]

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun testIntensity(path: String): Double
    {
        val istm: InputStream = am.open(path)
        val bitmap = BitmapFactory.decodeStream(istm)
        image_origin.setImageBitmap(bitmap)
        val mat = Mat()
        mat.toGray(bitmap)
//        val resizeimage:Mat = Mat()
        val sz:Size = Size(640.0,480.0)
        resize(mat, mat, sz )
//        val procBitmap:Bitmap=mat.toBitmap()
        val binary=Mat()
        threshold(mat,binary, 200.0, 255.0, THRESH_TOZERO_INV)
        threshold(mat,binary, 0.0, 255.0, THRESH_OTSU)
        val kernel = Imgproc.getStructuringElement(MORPH_RECT, Size(2.0, 2.0))
        morphologyEx(binary,binary,MORPH_OPEN, kernel)
        morphologyEx(binary,binary,MORPH_CLOSE, kernel)

        val contour= MatOfPoint()
        findNonZero(binary,contour)
        val y_index = mutableListOf<Double>()
        val x_index = mutableListOf<Double>()

        contour.toArray().forEach {
            y_index.add(it.x) // col index
            x_index.add(it.y) // row index
        }

        val c_y = (y_index.maxOrNull()!!+y_index.minOrNull()!!) / 2 // center
        val c_x = (x_index.maxOrNull()!!+x_index.minOrNull()!!) / 2
//        val r_y = (y_index.maxOrNull()!!-y_index.minOrNull()!!) / 2 // radius
//        val r_x = (x_index.maxOrNull()!!-x_index.minOrNull()!!) / 2
//        val r_max = if(r_y>r_x) r_y else r_x
        val r_max=200.0
        val in_bound=0.85
        val out_bound=0.95

        val thres=20.0
        for(y in 0..mat.cols())
        {
            for (x in 0..mat.rows())
            {
                val dist=sqrt((x-c_x)*(x-c_x)+(y-c_y)*(y-c_y))
                if(dist>=out_bound*r_max||dist<=in_bound*r_max)
                    mat.put(x,y,0.0)

            }
        }
        val mask=Mat()
        threshold(mat,mask,thres,255.0, THRESH_BINARY)
        mat.mul(mask)
        image_processed.setImageBitmap(mat.toBitmap())
        val mean_val=MatOfDouble()
        val std_val=MatOfDouble()
        meanStdDev(mat,mean_val,std_val,mask)
//        val res=mean(mat,mask)
//        println("${res::class.qualifiedName}")
//        Log.d("res","${res[0]}, ${res[1]}, ${res[2]}, ${res[3]}")

        binary.release()
        mask.release()
        std_val.release()
        mat.release()

        return mean_val.toArray()[0]

    }


    private fun drawContour(bitmap: Bitmap)
    {
        val mat = Mat()
        mat.toGray(bitmap)
        val binary=Mat()
        threshold(mat,binary, 0.0, 255.0, THRESH_OTSU)
        val contours= mutableListOf<MatOfPoint>()
        val hierarchy=Mat()
        findContours(binary,contours,hierarchy,RETR_TREE, CHAIN_APPROX_SIMPLE)
        val color=Scalar(120.0,155.0,0.0)
//        for(i in 0 until contours.count())
//        {
//            // judge(countors.get(i))
//        }
        Log.d("drawContour","${contours.get(0).size()}")
        drawContours(mat, contours, -1, color)
        val procbitmap=mat.toBitmap()
        image_origin.setImageBitmap(procbitmap)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InferViewModel::class.java)
        // TODO: Use the ViewModel
    }


// todo: multi button listener


//    private fun pickImageIntent(){
//        val intent:Intent= Intent()
//        intent.setType("image/*")
////        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
//        intent.setAction(Intent.ACTION_GET_CONTENT)
//        startActivityForResult(Intent.createChooser(intent,"Select Image"),PICK_IMAGES_CODE)
//        Log.d("pickImageIntent", "end of picking intent")
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Log.d("onActivityResult", "activity result")
//        if (requestCode==PICK_IMAGES_CODE)
//        {
//            if (resultCode==Activity.RESULT_OK)
//            {
//                if (data != null) {
//                    if (data.getClipData()!=null) {
//                        val imageUri:Uri=data.getClipData()?.getItemAt(0)!!.getUri()
//                        imageUris.add(imageUri)
//                        imageIs.setImageURI(imageUris.get(0))
//                    }
//                    else{
//                        val imageUri: Uri =data.getData() as Uri
//                        imageUris.add(imageUri)
//                        imageIs.setImageURI(imageUris.get(0))
//                    }
//                }
//
//            }
//        }
//    }

}
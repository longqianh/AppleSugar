package com.longqianh.applesugar

import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.Navigation
import com.google.common.util.concurrent.ListenableFuture
import com.longqianh.applesugar.databinding.InferFragmentBinding
import com.longqianh.applesugar.view.CameraXPreviewViewTouchListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class cameraFragment : Fragment() {
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraPreviewView: PreviewView
    private var mCameraControl: CameraControl? = null
    private var mCameraInfo: CameraInfo? = null
    private var btControl= BluetoothControl()
    private var m_address:String?=null
    private var stateWavelengthIndex=0

//    680, 700, 730, 760, 780, 800, 830, 850
    val isoArray=intArrayOf(100,200,500,1250,1250,4000,4000,6400)
    val speedArray= longArrayOf(3125000L,3125000L,4000000L,10000000L,20000000L,20000000L,50000000L,66666667L)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputDirectory = MainActivity.getOutputDirectory(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()
        m_address = arguments?.getString("address")
//        Log.d("infer",m_address?:"no address")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(m_address==null)
        {
            Toast.makeText(requireContext(),"Bluetooth not connected.",Toast.LENGTH_SHORT).show()
        }
        else{
            btControl.setAddress(m_address!!)
            val isConnected = btControl.connect(requireContext())
            if(isConnected)
            {
                Toast.makeText(requireContext(),"Light control ok.",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(),"Bluetooth not connected",Toast.LENGTH_SHORT).show()
            }
        }


        cameraPreviewView=view.findViewById(R.id.cameraPreview)
        // Request camera permissions
        if (allPermissionsGranted()) {
            val iso=50
            val exposureTime=8000000L
            startCamera(cameraPreviewView,iso,exposureTime)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        view.findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
            takePhoto(stateWavelengthIndex)
        }

        val btn680=view.findViewById<Button>(R.id.camera_680_button)
        btn680.setOnClickListener {
//            startCamera(cameraPreviewView,isoArray[0],speedArray[0])
            processCameraButton(btn680,0)
        }

        val btn700=view.findViewById<Button>(R.id.camera_700_button)
        btn700.setOnClickListener {
//            startCamera(cameraPreviewView,isoArray[1],speedArray[1])
            processCameraButton(btn700,1)
        }


        val btn730=view.findViewById<Button>(R.id.camera_730_button)
        btn730.setOnClickListener {
//            startCamera(cameraPreviewView,isoArray[2],speedArray[2])
            processCameraButton(btn730,2)
        }

        val btn760=view.findViewById<Button>(R.id.camera_760_button)
        btn760.setOnClickListener {
            processCameraButton(btn760,3)
//            startCamera(cameraPreviewView,isoArray[3],speedArray[3])
        }
        val btn780=view.findViewById<Button>(R.id.camera_780_button)
        btn780.setOnClickListener {
            processCameraButton(btn780,4)
//            startCamera(cameraPreviewView,isoArray[3],speedArray[3])
        }
        val btn800=view.findViewById<Button>(R.id.camera_800_button)
        btn800.setOnClickListener {
            processCameraButton(btn800,5)
//            startCamera(cameraPreviewView,isoArray[3],speedArray[3])
        }
        val btn830=view.findViewById<Button>(R.id.camera_830_button)
        btn830.setOnClickListener {
            processCameraButton(btn830,6)
//            startCamera(cameraPreviewView,isoArray[3],speedArray[3])
        }

        val btn850=view.findViewById<Button>(R.id.camera_850_button)
        btn850.setOnClickListener {
            processCameraButton(btn850,7)
        }


        view.findViewById<Button>(R.id.camera_back_button).setOnClickListener{
            Navigation.findNavController(it).navigateUp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_camera,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_about -> {
                Navigation.findNavController(requireView()).navigate(R.id.action_cameraFragment_to_aboutFragment)
                true
            }
            R.id.menu_help ->{
                Navigation.findNavController(requireView()).navigate(R.id.action_cameraFragment_to_helpFragment)
                true
            }

            R.id.menu_bluetooth ->{
                Navigation.findNavController(requireView()).navigate(R.id.action_cameraFragment_to_selectDeviceFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun processCameraButton(btn:Button,index:Int)
    {
        btn.isSelected=!btn.isSelected
        if (btn.isSelected)
        {
            btControl.sendCommand(index.toString())
            startCamera(cameraPreviewView,isoArray[index],speedArray[index])
        }
        else{
            btControl.sendCommand("f")
        }
        stateWavelengthIndex=index

    }
    private fun startCamera(cameraPreviewView:PreviewView,iso:Int,exposureTime:Long) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val myTime=2040000L // nanoseconds -->1/490*1e9
//            val myISO=400
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val imageCaptureBuilder = ImageCapture.Builder()
            imageCaptureBuilder.setTargetResolution(Size(640, 480))
            Camera2Interop.Extender(imageCaptureBuilder)
//                .setCaptureRequestOption(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE,CameraMetadata.CONTROL_AF_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
                .setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, 25.0F) // mm
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT) // turn off auto white balance
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, true)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF) // turn off auto-exposure
                .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTime)
                .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY,iso)

            imageCapture = imageCaptureBuilder.build()
//            println(imageCaptureBuilder.get(CaptureRequest.SENSOR_SENSITIVITY))
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreviewView.surfaceProvider)
                }
//
//            val imageAnalysis = ImageAnalysis.Builder()
//                .setTargetResolution(Size(640, 480))
//                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera=cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)

            // 相机控制，如点击
            mCameraControl = camera.cameraControl
            mCameraInfo = camera.cameraInfo
            initCameraListener()

        }, ContextCompat.getMainExecutor(requireContext()))


    }

    // 相机点击等相关操作监听
    private fun initCameraListener() {
        val cameraXPreviewViewTouchListener = CameraXPreviewViewTouchListener(requireContext())
        val zoomState: LiveData<ZoomState> = mCameraInfo!!.zoomState
        cameraXPreviewViewTouchListener.setCustomTouchListener(object :
            CameraXPreviewViewTouchListener.CustomTouchListener {

            override fun zoom(delta: Float) {

                Log.d(TAG, "缩放")
                zoomState.value?.let {
                    val currentZoomRatio = it.zoomRatio
                    mCameraControl!!.setZoomRatio(currentZoomRatio * delta)
                }
            }


            // 点击操作
//            override fun click(x: Float, y: Float) {
////                println("clicked")
//                val factory = cameraPreviewView.meteringPointFactory
//                // 设置对焦位置
//                val point = factory.createPoint(x, y)
//                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
//                    // 3秒内自动调用取消对焦
////                    .setAutoCancelDuration(5, TimeUnit.SECONDS)
//                    .build()
//                // 执行对焦
//                mCameraControl!!.startFocusAndMetering(action)
////
//            }

            // 双击操作
            override fun doubleClick(x: Float, y: Float) {
                Log.d(TAG, "双击")
                // 双击放大缩小
                val currentZoomRatio = zoomState.value!!.zoomRatio
                if (currentZoomRatio > zoomState.value!!.minZoomRatio) {
                    mCameraControl!!.setLinearZoom(0f)
                } else {
                    mCameraControl!!.setLinearZoom(0.5f)
                }
            }

//            override fun longPress(x: Float, y: Float) {
//                Log.d(TAG, "长按")
//            }

        })


        // 添加监听事件
        cameraPreviewView.setOnTouchListener(cameraXPreviewViewTouchListener)

    }

    private fun takePhoto(index:Int) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image

        val wavelengthMap= intArrayOf(680,700,730,760,780,800,830,850)
        val photoFile = createFile(outputDirectory, wavelengthMap[index].toString(), PHOTO_EXTENSION)

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }


                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        private const val TAG = "CameraX"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private fun createFile(baseFolder: File, name: String, extension: String) =
//            File(baseFolder, SimpleDateFormat(format, Locale.US)
//                .format(System.currentTimeMillis()) + extension)
            File(baseFolder,name+extension)
    }


}

// todo: integrate processing precedure
// todo: data binding

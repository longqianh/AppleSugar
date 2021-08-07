package com.longqianh.applesugar

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.longqianh.applesugar.databinding.FragmentCameraBinding
import com.longqianh.applesugar.view.CameraXPreviewViewTouchListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.schedule

class cameraFragment : Fragment() {
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
//    private lateinit var cameraPreviewView: PreviewView
    private var mCameraControl: CameraControl? = null
    private var mCameraInfo: CameraInfo? = null
    private var btControl= BluetoothControl()
    private var m_address:String?=null
    private var developer=false
    private var cameraOpened=false
    private var stateIntensity=0.0
    private lateinit var contentResolver: ContentResolver
    private lateinit var am: AssetManager

    private var bk = DoubleArray(8) { _ -> 0.0 }
    private var isSelected=BooleanArray(8){_->false}

    private var _binding: FragmentCameraBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var btnArray: Array<Button>
    private lateinit var viewModel: CameraViewModel


    //    680, 700, 730, 760, 780, 800, 830, 850
    private val isoArray=intArrayOf(100,200,500,1250,1250,4000,6400,6400)
    private val speedArray= longArrayOf(1562500L,1562500L,4000000L,10000000L,20000000L,20000000L,20000000L,66666667L)
    private val waveLength= intArrayOf(680,700,730,760,780,800,830,850)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputDirectory = MainActivity.getOutputDirectory(requireContext(),developer)
        cameraExecutor = Executors.newSingleThreadExecutor()
        m_address = arguments?.getString("address")

//        Log.d("infer",m_address?:"no address")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        viewModel=ViewModelProviders.of(this).get(CameraViewModel::class.java)
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentResolver = requireContext().contentResolver
        am = requireContext().assets
//        inferViewModel = ViewModelProviders.of(inferFragment).get(InferViewModel::class.java)
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


        val appContext = requireContext().applicationContext
        val appName=appContext.resources.getString(R.string.app_name)
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()
        var appleFile:File
        appleFile=File(mediaDir, "$appName/$stateAppleNum")
        while(appleFile.exists())
        {
            stateAppleNum++
            appleFile=File(mediaDir, "$appName/$stateAppleNum")
        }

        stateAppleNum--
        binding.appleNumText.text="Apple Num: $stateAppleNum"

        // Request camera permissions
        if (allPermissionsGranted()) {
            val iso=50
            val exposureTime=8000000L
            startCamera(iso,exposureTime)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


        btnArray= arrayOf(binding.camera680Button,binding.camera700Button,
            binding.camera730Button,binding.camera760Button,
            binding.camera780Button,binding.camera800Button,
            binding.camera830Button,binding.camera850Button)

        for(i in 0..7)
        {
            btnArray[i].setOnClickListener{
                processCameraButton(btnArray[i],i)
            }
        }

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto(viewModel.stateWavelengthIndex)
        }


        binding.oneClickButton.setOnClickListener {
            if (btControl.getConnectState()) {
                CoroutineScope(Dispatchers.Main).launch {
//                    btControl.sendCommand("f")
                    for (i in 0..7) {
                        async {
                            btControl.sendCommand(i.toString())
//                            println("Test: in btcontrol $i")
                        }
                        async {
                            startCamera(isoArray[i], speedArray[i])
//                            println("Test: in start camera $i")
                        }
                        delay(500)
//                        println("Test: before take photo $i")
//                        CoroutineScope(Dispatchers.Main).launch {
//                            withContext(Dispatchers.Default){ takePhoto(i) }
//                        }
                        takePhoto(i)
//                        println("Test: after take photo $i")
//                        if(i==7)
//                        {
//                            delay(2500)
//                        }
                        if(i<7)
                        {
                            delay(1000)
                        }
                        else{
                            delay(1500)
                        }
//                        btControl.sendCommand("f")

                    }
                    btControl.sendCommand("0")
                    startCamera(isoArray[0], speedArray[0])

                }


            }
//            binding.appleNumText.text = "Apple Num: $stateAppleNum"
            else {
                Toast.makeText(
                    requireContext(),
                    "One click require bluetooth connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.cameraNewAppleButton.setOnClickListener{
            stateAppleNum++
            outputDirectory=MainActivity.getOutputDirectory(requireContext(),developer,stateAppleNum)
            binding.appleNumText.text = "Apple Num: $stateAppleNum"
        }

        binding.cameraDropAppleButton.setOnClickListener{
            stateAppleNum--
            outputDirectory=MainActivity.getOutputDirectory(requireContext(),developer,stateAppleNum)
            binding.appleNumText.text = "Apple Num: $stateAppleNum"
        }

        binding.getSugarButton.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch {
                val sugar=Utils.calSugar(viewModel.inputFeatures,binding.modelSwitch.isChecked,am)
                binding.sugarText.visibility=View.VISIBLE
                binding.sugarText.text="Sugar: "+"%.2f".format(sugar)+" Brix"
                binding.intensityText.visibility=View.GONE

            }
//
        }

//        binding.cameraBackButton.setOnClickListener{
//            Navigation.findNavController(it).navigateUp()
//        }
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

            R.id.menu_develpoer ->
            {
                developer=!developer
                if(developer)
                {
                    Toast.makeText(requireContext(),"Developer mode.\nClick again to close.",Toast.LENGTH_SHORT).show()
                    binding.cameraNewAppleButton.visibility=View.VISIBLE
                    binding.cameraDropAppleButton.visibility=View.VISIBLE
                    binding.oneClickButton.visibility=View.VISIBLE
                    binding.appleNumText.visibility=View.VISIBLE
                    binding.appleNumText.text = "Apple Num: $stateAppleNum"
                    binding.getSugarButton.visibility=View.GONE
                    binding.modelSwitch.visibility=View.GONE
                    binding.sugarText.visibility=View.GONE
                    outputDirectory=MainActivity.getOutputDirectory(requireContext(),developer,stateAppleNum)
                }
                else{
                    Toast.makeText(requireContext(),"Close developer mode.",Toast.LENGTH_SHORT).show()
                    binding.cameraNewAppleButton.visibility=View.GONE
                    binding.cameraDropAppleButton.visibility=View.GONE
//                    binding.oneClickButton.visibility=View.GONE
                    binding.appleNumText.visibility=View.GONE
                    binding.getSugarButton.visibility=View.VISIBLE
                    binding.modelSwitch.visibility=View.VISIBLE
                    outputDirectory=MainActivity.getOutputDirectory(requireContext(),developer,stateAppleNum)
                }
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
        CoroutineScope(Dispatchers.IO).launch{
            btControl.sendCommand("f")
        }
        btn.isSelected=!btn.isSelected
        for(i in 0..7)
        {
            if(i==index)
            {
                continue
            }

            if(btnArray[i].isSelected)
            {
                btnArray[i].isSelected=false
                isSelected[i]=false
            }

        }
        if (btn.isSelected)
        {
            CoroutineScope(Dispatchers.Main).launch {
                 btControl.sendCommand(index.toString()) // suspend in IO thread
            }
            startCamera(isoArray[index],speedArray[index])

//            if(!cameraOpened)
//            {
//                startCamera(cameraPreviewView,isoArray[index],speedArray[index])
//            }
//            else{
//                val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//                val imageCaptureBuilder = ImageCapture.Builder()
//                cameraProvider.unbind(imageCapture)
//                imageCaptureBuilder.setTargetResolution(Size(640, 480))
//                Camera2Interop.Extender(imageCaptureBuilder)
////                .setCaptureRequestOption(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE,CameraMetadata.CONTROL_AF_MODE_OFF)
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
//                    .setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, 25.0F) // mm
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0)
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT) // turn off auto white balance
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, true)
//                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF) // turn off auto-exposure
//                    .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, speedArray[index])
//                    .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY,isoArray[index])
//
//                imageCapture = imageCaptureBuilder.build()
//                cameraProvider.bindToLifecycle(
//                    this,CameraSelector.DEFAULT_BACK_CAMERA, imageCapture)
//            }
        }

        viewModel.stateWavelengthIndex=index

    }

    private fun startCamera(iso:Int,exposureTime:Long) {
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
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
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

//        cameraOpened=true
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
        binding.cameraPreview.setOnTouchListener(cameraXPreviewViewTouchListener)

    }

    private fun takePhoto(index:Int){
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image

        val photoFile = createFile(outputDirectory, waveLength[index].toString(), PHOTO_EXTENSION)

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @RequiresApi(Build.VERSION_CODES.P)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

//                    Log.d(TAG, msg)
//                    val path= "$outputDirectory/${waveLength[stateWavelengthIndex]}$PHOTO_EXTENSION"
//                    val intensity=Utils.getIntensityFromPath(path,am)
                    CoroutineScope(Dispatchers.Main).launch {

                        val intensity=Utils.getIntensityFromUri(savedUri,contentResolver)
                        binding.intensityText.text="Intensity: "+"%.2f".format(intensity)
                        binding.intensityText.visibility=View.VISIBLE
                        viewModel.inputFeatures[index]=intensity-bk[index]
//                        Toast
                    }

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

        var stateAppleNum=0

    }


}


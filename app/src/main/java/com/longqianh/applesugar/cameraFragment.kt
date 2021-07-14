package com.longqianh.applesugar

import android.content.pm.PackageManager
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
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
//    private lateinit var camera: Camera
    val width=640
    val height=480
    val isoArray=intArrayOf(50,125,125,400,800,3200,3200)
    val speedArray= longArrayOf(8000000L,6250000L,8000000L,25000000L,50000000L,33333333L,66666667L)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputDirectory = MainActivity.getOutputDirectory(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()
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
//        startCamera(view.findViewById(R.id.cameraPreview))
        view.findViewById<Button>(R.id.camera_capture_button).setOnClickListener {
//            println("hello")
            takePhoto()
        }

        view.findViewById<Button>(R.id.camera_680_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[0],speedArray[0])
        }

        view.findViewById<Button>(R.id.camera_700_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[1],speedArray[1])
        }

        view.findViewById<Button>(R.id.camera_720_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[2],speedArray[2])
        }
        view.findViewById<Button>(R.id.camera_760_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[3],speedArray[3])
        }
        view.findViewById<Button>(R.id.camera_780_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[4],speedArray[4])
        }
        view.findViewById<Button>(R.id.camera_800_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[5],speedArray[5])
        }
        view.findViewById<Button>(R.id.camera_810_button).setOnClickListener {
            startCamera(cameraPreviewView,isoArray[6],speedArray[6])
        }

        view.findViewById<Button>(R.id.camera_back_button).setOnClickListener{
            Navigation.findNavController(it)
                .navigate(R.id.action_cameraFragment_to_sugarFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_camera,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_680 -> {
                startCamera(cameraPreviewView,isoArray[0],speedArray[0])
                true
            }
            R.id.menu_700 ->{
                startCamera(cameraPreviewView,isoArray[1],speedArray[1])
                true
            }

            R.id.menu_720 ->{
                startCamera(cameraPreviewView,isoArray[2],speedArray[2])
                true
            }
            R.id.menu_760 ->{
                startCamera(cameraPreviewView,isoArray[3],speedArray[3])
                true
            }
            R.id.menu_780 ->{
                startCamera(cameraPreviewView,isoArray[4],speedArray[4])
                true
            }
            R.id.menu_800 ->{
                startCamera(cameraPreviewView,isoArray[5],speedArray[5])
                true
            }
            R.id.menu_810 ->{
                startCamera(cameraPreviewView,isoArray[6],speedArray[6])
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera(cameraPreviewView:PreviewView,iso:Int,exposureTime:Long) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val myTime=2040000L // nanoseconds -->1/490*1e9
//            val myISO=400
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val imageCaptureBuilder = ImageCapture.Builder()

            Camera2Interop.Extender(imageCaptureBuilder)
//                .setCaptureRequestOption(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT) // turn off auto white balance
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, true)
                .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE,CameraMetadata.CONTROL_AF_TRIGGER_START)
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
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(width, height))
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture,imageAnalysis)

        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image

        val photoFile = createFile(outputDirectory, FILENAME_FORMAT, PHOTO_EXTENSION)

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
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)
    }

}
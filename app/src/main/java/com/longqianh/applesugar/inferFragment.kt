package com.longqianh.applesugar

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.longqianh.applesugar.databinding.InferFragmentBinding
import com.longqianh.applesugar.extensions.toBitmap
import com.longqianh.applesugar.extensions.toGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.core.*
import org.opencv.core.Core.findNonZero
import org.opencv.core.Core.meanStdDev
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.sqrt


class inferFragment: Fragment(), View.OnClickListener {

//    private val REQUEST_CONNECT_DEVICE_SECURE = 1
//    private val REQUEST_CONNECT_DEVICE_INSECURE = 2
    private val REQUEST_ENABLE_BT = 3

    private var _binding: InferFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var viewModel: InferViewModel

    private lateinit var getContent: ActivityResultLauncher<String>
    private var intensity: Double = 0.0
    private lateinit var contentResolver: ContentResolver
    private lateinit var am: AssetManager
//    private var m_address:String?=null
//    private var btControl= BluetoothControl()



//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        m_address = arguments?.getString("address")
//        Log.d("infer",m_address?:"no address")

    //    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
//        println("Thread: ${Thread.currentThread().name}")
        Log.i("inferFragment", "onCreateView")
        viewModel = ViewModelProviders.of(this).get(InferViewModel::class.java)
        _binding = InferFragmentBinding.inflate(inflater, container, false)
        return binding.root

//        return inflater.inflate(R.layout.infer_fragment, container, false)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentResolver = requireContext().contentResolver
        am = requireContext().assets

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri ->
            CoroutineScope(Dispatchers.Main).launch {
                intensity = Utils.getIntensityFromUri(uri,contentResolver)
            }
        }

        binding.sugarText.visibility = View.GONE

        binding.pick680Button.setOnClickListener(this) // click 后会调用 onClick
        binding.pick700Button.setOnClickListener(this)
        binding.pick730Button.setOnClickListener(this)
        binding.pick760Button.setOnClickListener(this)
        binding.pick780Button.setOnClickListener(this)
        binding.pick800Button.setOnClickListener(this)
        binding.pick830Button.setOnClickListener(this)
        binding.pick850Button.setOnClickListener(this)
        binding.captureButton.setOnClickListener(this)
        binding.pickButton.setOnClickListener(this)
//        binding.lightControlSwitch.setOnClickListener(this)
        binding.getSugarButton.setOnClickListener(this)
        binding.inferModelSwitch.setOnClickListener(this)
//        binding.bkSwitch.setOnClickListener(this)


    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(v: View?) {

        when (v!!.id) {

            R.id.pick680_button -> {
                processButton(binding.pick680Button,0)
            }

            R.id.pick700_button -> {
                processButton(binding.pick700Button,1)
            }

            R.id.pick730_button -> {
                processButton(binding.pick730Button,2)
            }

            R.id.pick760_button -> {
                processButton(binding.pick760Button,3)
            }

            R.id.pick780_button -> {
                processButton(binding.pick780Button,4)
            }

            R.id.pick800_button -> {
                processButton(binding.pick800Button,5)
            }

            R.id.pick830_button -> {
                processButton(binding.pick830Button,6)
            }

            R.id.pick850_button -> {
                processButton(binding.pick830Button,7)
            }

            R.id.get_sugar_button -> {
                Toast.makeText(
                    requireContext(),
                    "input features: [${viewModel.features[0]},${viewModel.features[1]}," +
                            "${viewModel.features[2]},${viewModel.features[3]}," +
                            "${viewModel.features[4]},${viewModel.features[5]}," +
                            "${viewModel.features[6]}]",
                    Toast.LENGTH_LONG
                ).show()
                CoroutineScope(Dispatchers.Main).launch {
                    val sugar = Utils.calSugar(viewModel.features,binding.inferModelSwitch.isChecked,am)
                    binding.sugarText.visibility = View.VISIBLE
                    binding.sugarText.text = "Apple sugar: $sugar Brix"
                }

            }

            R.id.capture_button -> {
                Navigation.findNavController(v).navigate(R.id.action_inferFragment_to_cameraFragment)
            }

            R.id.pick_button -> {
                getContent.launch("image/*")
            }

            R.id.model_switch -> {
                if(binding.inferModelSwitch.isChecked)
                {
                    Toast.makeText(requireContext(),"Change to Regresion Model.",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(),"Change to Classification Model.",Toast.LENGTH_SHORT).show()
                }
            }

//            R.id.bk_switch ->{
//                if(!binding.lightControlSwitch.isChecked)
//                {
//                    if(binding.bkSwitch.isChecked)
//                    {
//                        changeButtonState(2)
//                    }
//                    else{
//                        changeButtonState(3)
//                    }
//                }
//            }

//            R.id.light_control_switch ->{
//                if(binding.lightControlSwitch.isChecked)
//                {
//                    changeButtonState(1)
//                    if(m_address==null)
//                    {
//                        Toast.makeText(requireContext(),"Bluetooth not connect!",Toast.LENGTH_SHORT).show()
//                        binding.lightControlSwitch.isChecked=false
//                    }
//                    else{
//                        btControl.setAddress(m_address!!)
//                        val isConnected = btControl.connect(requireContext())
//                        if(isConnected)
//                        {
//                            Toast.makeText(requireContext(),"Light control mode.",Toast.LENGTH_SHORT).show()
//                        }
//                        else{
//                            Toast.makeText(requireContext(),"Not connected",Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                }
//                else{
//                    if(binding.bkSwitch.isChecked)
//                    {
//                        changeButtonState(2)
//                    }
//                    else{
//                        changeButtonState(3)
//                    }
//                }
//            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_ENABLE_BT){
            if (resultCode== Activity.RESULT_OK)
            {
                if(bluetoothAdapter.isEnabled)
                {
                    Toast.makeText(requireContext(),"Bluetooth has been enabled.",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireContext(),"Bluetooth has not been enabled.",Toast.LENGTH_SHORT).show()
                }
            }
        }

//   todo:   pick intent result check
    }

//    private fun changeButtonState(stateType:Int)
//    {
//        when(stateType)
//        {
//            1-> // light control
//            {
//                binding.pick680Button.isSelected=viewModel.lightControlSelect[0]
//                binding.pick700Button.isSelected=viewModel.lightControlSelect[1]
//                binding.pick730Button.isSelected=viewModel.lightControlSelect[2]
//                binding.pick760Button.isSelected=viewModel.lightControlSelect[3]
//                binding.pick780Button.isSelected=viewModel.lightControlSelect[4]
//                binding.pick800Button.isSelected=viewModel.lightControlSelect[5]
//                binding.pick830Button.isSelected=viewModel.lightControlSelect[6]
//            }
//            2-> // feature setting
//            {
//                Toast.makeText(requireContext(),"Change to feature setting mode.",Toast.LENGTH_SHORT).show()
//                binding.pick680Button.isSelected=viewModel.inferButtonSelect[0]
//                binding.pick700Button.isSelected=viewModel.inferButtonSelect[1]
//                binding.pick730Button.isSelected=viewModel.inferButtonSelect[2]
//                binding.pick760Button.isSelected=viewModel.inferButtonSelect[3]
//                binding.pick780Button.isSelected=viewModel.inferButtonSelect[4]
//                binding.pick800Button.isSelected=viewModel.inferButtonSelect[5]
//                binding.pick830Button.isSelected=viewModel.inferButtonSelect[6]
//            }
//            3-> // bk setting
//            {
//                Toast.makeText(requireContext(),"Change to background setting mode.",Toast.LENGTH_SHORT).show()
//                binding.pick680Button.isSelected=viewModel.bkButtonSelect[0]
//                binding.pick700Button.isSelected=viewModel.bkButtonSelect[1]
//                binding.pick730Button.isSelected=viewModel.bkButtonSelect[2]
//                binding.pick760Button.isSelected=viewModel.bkButtonSelect[3]
//                binding.pick780Button.isSelected=viewModel.bkButtonSelect[4]
//                binding.pick800Button.isSelected=viewModel.bkButtonSelect[5]
//                binding.pick830Button.isSelected=viewModel.bkButtonSelect[6]
//            }
//        }
//
//    }

    private fun processButton(btn:Button,index:Int)
    {
        // in a certain state
        btn.isSelected=!btn.isSelected
        if (btn.isSelected)
        {
            viewModel.features[index] = intensity - viewModel.bk[index]
        }
        else{
            viewModel.features[index] = 0.0
        }
//        if(!binding.lightControlSwitch.isChecked)
//        {
//        if(binding.bkSwitch.isChecked)
//        {
//            viewModel.inferButtonSelect[index] = btn.isSelected
//            binding.showSugarText.text = "Feature-$index: ${viewModel.features[index]}"
//            if (btn.isSelected)
//            {
//                if(viewModel.bk[index]==0.0)
//                {
//                    Toast.makeText(requireContext(),"Please set background intensity first.",Toast.LENGTH_SHORT).show()
//                }
//                else{
//                    viewModel.features[index] = intensity / viewModel.bk[index]
//                }
//            }
//            else{
//                viewModel.features[index] = 0.0
//                Toast.makeText(requireContext(),"Set feature intensity to 0.",Toast.LENGTH_SHORT).show()
//            }
//        }
//        else{
//            viewModel.bkButtonSelect[index] = btn.isSelected
//            binding.showSugarText.text = "Background-$index: ${viewModel.bk[index]}"
//            if (btn.isSelected)
//            {
//                viewModel.bk[index] = intensity
//            }
//            else{
//                viewModel.bk[index] = 0.0
//                Toast.makeText(requireContext(),"Set background intensity to 0.",Toast.LENGTH_SHORT).show()
//            }
//        }

//        }

//        else{
//            viewModel.lightControlSelect[index]=btn.isSelected
//            if (btn.isSelected)
//            {
//                btControl.sendCommand(index.toString())
//            }
//            else{
//                btControl.sendCommand("f")
//            }
////            sendMessage(index) // turn on index and turn off others
//            Toast.makeText(requireContext(),"Set $index light on.",Toast.LENGTH_SHORT).show()
//        }



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

//            R.id.menu_bluetooth ->{
//                Navigation.findNavController(requireView()).navigate(R.id.action_inferFragment_to_selectDeviceFragment)
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InferViewModel::class.java)
        // TODO: Use the ViewModel
    }

}


// todo: softmax need not
// todo: bluetooth disconnect
// todo: static capturing
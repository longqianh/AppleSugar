package com.longqianh.applesugar

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.longqianh.applesugar.databinding.FragmentSelectDeviceBinding
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class selectDeviceFragment : Fragment() {
    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var m_pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1
//    private var btControl= BluetoothControl()


    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
    }

    private var _binding: FragmentSelectDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null) {
            Toast.makeText(requireContext(),"this device doesn't support bluetooth",Toast.LENGTH_SHORT).show()
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSelectDeviceBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
//        return inflater.inflate(R.layout.fragment_select_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController=Navigation.findNavController(view)
        binding.selectDeviceRefreshButton.setOnClickListener{
            if(m_bluetoothAdapter==null)
            {
                Toast.makeText(requireContext(),"Cannot refresh, not support bluetooth.",Toast.LENGTH_SHORT).show()
            }
            else{
                pairedDeviceList(navController)
            }
        }

        binding.selectDeviceBackButton.setOnClickListener{
            navController.navigateUp()
        }
    }

    private fun pairedDeviceList(navController: NavController) {
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()

        if (m_pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                list.add(device)
                Log.i("device", ""+device)
            }
        } else {
            Toast.makeText(requireContext(),"no paired bluetooth devices found",Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        binding.selectDeviceList.adapter = adapter
        binding.selectDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
//            btControl.setAddress(address)
//            val isConnected = btControl.connect(requireContext())
//            if(isConnected)
//            {
//                Toast.makeText(requireContext(),"Light control mode.",Toast.LENGTH_SHORT).show()
//            }
//            else{
//                Toast.makeText(requireContext(),"Not connected",Toast.LENGTH_SHORT).show()
//            }
            val bundle = bundleOf("address" to address)
//            navController.navigate(R.id.action_selectDeviceFragment_to_inferFragment, bundle)
            navController.navigate(R.id.action_selectDeviceFragment_to_cameraFragment,bundle)

//            val intent = Intent(requireContext(), ControlActivity::class.java)
//            intent.putExtra(EXTRA_ADDRESS, address)
//            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (m_bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(requireContext(),"Bluetooth has been enabled",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(),"\"Bluetooth has been disabled\"",Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(),"Bluetooth enabling has been canceled",Toast.LENGTH_SHORT).show()
            }
        }
    }



}
package com.longqianh.applesugar


import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageSwitcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import org.opencv.android.OpenCVLoader
import java.io.File
import java.util.concurrent.ExecutorService

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(OpenCVLoader.initDebug())
        {
            Log.d("main","opencv loaded")
        }
        else{
            Log.d("main","opencv not loaded")
        }
//        val sugarFrag=sugarFragment()
//        supportFragmentManager.beginTransaction().add(R.id.fragment_container,sugarFrag).commit()
    }


}


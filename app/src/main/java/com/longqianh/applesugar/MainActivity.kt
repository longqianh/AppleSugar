package com.longqianh.applesugar


import android.content.Context
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

//    var m_address: String? = null

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

    companion object {

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context,developer:Boolean=false,index:Int?=1): File {
            val appContext = context.applicationContext
            val appName=appContext.resources.getString(R.string.app_name)
            val mediaDir = context.externalMediaDirs.firstOrNull()
            var appleFile:File
//            var tmp:Int=0
            if(developer)
            {
                appleFile=File(mediaDir, appName+"/"+index!!.toString())
//                while(appleFile.exists())
//                {
//                    tmp++
//                    appleFile=File(mediaDir, appName+"/"+(index+tmp).toString())
//
//                    println(appleFile)
//                }
            }
            else{
                appleFile=File(mediaDir, appName)
            }
            if(!appleFile.exists())
            {
                appleFile.mkdirs()
            }

//            cameraFragment.stateAppleNum=tmp+index!!

            return appleFile
//            .let {
//                File(it, index.toString()).apply { mkdirs() } }
//            return if (mediaDir != null && mediaDir.exists())
//                mediaDir else appContext.filesDir
        }


    }


}


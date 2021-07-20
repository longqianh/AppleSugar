package com.longqianh.applesugar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InferViewModel : ViewModel() {
    init {
        Log.i("inferViewModel","initialize inferViewModel.")
    }
    // TODO: Implement the ViewModel
    var features = DoubleArray(8) { _ -> 0.0 }
    var bk = doubleArrayOf(
        0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
    )

//    var inferButtonSelect= BooleanArray(7){_->false}
//    var bkButtonSelect= BooleanArray(7){_->false}
//    var lightControlSelect = BooleanArray(7){_->false}

}
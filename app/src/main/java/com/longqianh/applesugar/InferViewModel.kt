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
    var features = DoubleArray(7) { _ -> 0.0 }
    var bk = doubleArrayOf(
        76.50252212,
        81.3847853,
        61.46050539,
        58.0978088,
        59.74946987,
        59.76914235,
        61.46982301
    )

    var inferButtonSelect= BooleanArray(7){_->false}
    var bkButtonSelect= BooleanArray(7){_->false}
    var lightControlSelect = BooleanArray(7){_->false}

}
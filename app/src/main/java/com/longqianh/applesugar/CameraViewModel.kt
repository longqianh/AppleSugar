package com.longqianh.applesugar

import androidx.lifecycle.ViewModel

class CameraViewModel:ViewModel() {
    var inputFeatures = DoubleArray(8) { _ -> 0.0 }
}
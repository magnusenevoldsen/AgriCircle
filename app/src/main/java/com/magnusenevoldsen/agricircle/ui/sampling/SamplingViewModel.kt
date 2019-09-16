package com.magnusenevoldsen.agricircle.ui.sampling

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SamplingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is sampling Fragment"
    }
    val text: LiveData<String> = _text
}
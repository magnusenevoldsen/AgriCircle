package com.magnusenevoldsen.agricircle.ui.workspace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkspaceViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is workspace Fragment"
    }
    val text: LiveData<String> = _text
}
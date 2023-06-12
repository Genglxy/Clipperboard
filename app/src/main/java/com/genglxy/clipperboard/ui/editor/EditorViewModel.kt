package com.genglxy.clipperboard.ui.editor

import androidx.lifecycle.ViewModel
import com.genglxy.clipperboard.logic.model.Photo

class EditorViewModel : ViewModel() {
    var photo: Photo? = null
    var sliderValue: Float? = null
    val fixedHeight = ArrayList<Int>()
    var index : Int? = null
}
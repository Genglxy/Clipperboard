package com.genglxy.clipperboard.ui.clipper

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.genglxy.clipperboard.logic.model.Photo

class ClipperViewModel : ViewModel() {
    val photoList = ArrayList<Photo>()
    val fixedHeight = ArrayList<Int>()
    var addButtonVisibility = 0
    var currentFixedHeightIndex = -1

    val fileUri = MutableLiveData(Uri.EMPTY)
    val fileBitmap = MutableLiveData<Bitmap>()
}
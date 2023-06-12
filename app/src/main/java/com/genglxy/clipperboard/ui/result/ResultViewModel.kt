package com.genglxy.clipperboard.ui.result

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.genglxy.clipperboard.logic.ResultRepository
import com.genglxy.clipperboard.logic.model.Photo
import com.genglxy.clipperboard.logic.model.Result

class ResultViewModel: ViewModel() {

    private val resultRepository = ResultRepository.get()
    val photoList = ArrayList<Photo>()
    val fixedHeight = ArrayList<Int>()
    var addButtonVisibility = 0
    var currentFixedHeightIndex = -1

    val fileUri = MutableLiveData(Uri.EMPTY)
    val fileBitmap = MutableLiveData<Bitmap>()

    suspend fun addResult(result: Result) {
        resultRepository.addResult(result)
    }
}
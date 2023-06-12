package com.genglxy.clipperboard.logic.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.net.URI
import java.util.UUID

@Parcelize
data class Photo(
    val uri: Uri,
    val width: Int,
    val height: Int,
    var fixedHeight: Int,
    var checked: Boolean = true,
    var clicked: Int = 0,
    var fixed: Boolean = false,
    var autoFixed: Boolean = true
) : Parcelable

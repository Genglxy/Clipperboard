package com.genglxy.clipperboard.logic.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Result(
    @PrimaryKey val id: UUID,
    val filename: String,
    val pathname: String,
    val date: Date,
    val uri: Uri,
    val width: Int,
    val height: Int
)

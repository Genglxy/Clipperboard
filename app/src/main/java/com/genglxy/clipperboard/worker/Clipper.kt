package com.genglxy.clipperboard.worker

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.genglxy.clipperboard.ClipperboardApplication.Companion.context
import com.genglxy.clipperboard.logic.ResultRepository
import com.genglxy.clipperboard.logic.model.Photo
import com.genglxy.clipperboard.logic.model.Result
import jp.wasabeef.glide.transformations.CropTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID

class Clipper : Service() {

    private val mBinder = ClipperBinder()
    private val resultRepository = ResultRepository.get()

    private var progressTo = 0
    private var progress = 0
    private lateinit var bitmap: Bitmap
    private var fileUri: Uri = Uri.EMPTY

    inner class ClipperBinder : Binder() {
        fun startClip(photoList: ArrayList<Photo>) = tempClip(photoList)
        fun getProgress() = Pair(progress, progressTo)
        fun getUri() = fileUri
        fun getBitmap() = bitmap
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun tempClip(photoList: List<Photo>) {
        var bitmapWidth = 0
        for (photo in photoList) {
            if (photo.width > bitmapWidth && photo.checked) {
                bitmapWidth = photo.width
            }
        }
        var bitmapHeight = 0
        for (photo in photoList) {
            if (photo.checked) {
                bitmapHeight += scaledHeight(photo, bitmapWidth)
                progressTo++
            }
        }
        bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var tempHeight = 0
        for (photo in photoList) {
            val thisHeight = tempHeight
            if (photo.checked) {
                val scaledHeight = scaledHeight(photo, bitmapWidth)
                val height = if (photo.fixed) {
                    photo.fixedHeight
                } else {
                    photo.height
                }

                Glide.with(context).asBitmap().load(photo.uri).apply(
                    RequestOptions.bitmapTransform(
                        CropTransformation(
                            photo.width, height, CropTransformation.CropType.BOTTOM
                        )
                    )
                ).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        Log.d(
                            "debugInfo",
                            "photo $progress start, width $bitmapWidth, height $scaledHeight"
                        )
                        val newBitmap = scaleBitmap(resource, bitmapWidth, scaledHeight)
                        canvas.drawBitmap(newBitmap, 0F, thisHeight.toFloat(), null)
                        Log.d("debugInfo", "photo $progress finished")
                        progress++
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
                tempHeight += scaledHeight
            }
        }
        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            while (progress != progressTo) {
                delay(200)
            }
            progress++
            fileUri = bitmap2Cache(context, bitmap)
        }
    }

    private fun bitmap2Cache(context: Context, bitmap: Bitmap): Uri {
        val format =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS
            else Bitmap.CompressFormat.PNG
        val extension = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "webp" else "png"
        val fileName = "${System.currentTimeMillis()}.$extension"
        val pathName = "${context.externalCacheDir}${File.separator}"
        val path =
            File("$pathName$fileName")
        try {
            val os = FileOutputStream(path)
            bitmap.compress(format, 100, os)
            progress++
            os.close()
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                addResult(
                    Result(
                        id = UUID.randomUUID(),
                        filename = fileName,
                        pathname = pathName,
                        date = Date(),
                        uri = Uri.fromFile(path),
                        width = bitmap.width,
                        height = bitmap.height
                    )
                )
            }
            Log.d("debugInfo", "file saved, ${Uri.fromFile(path)}")
            return Uri.fromFile(path)
        } catch (_: Exception) {
        }
        return Uri.EMPTY
    }

    private fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val scaleWidth: Float = width.toFloat() / bitmap.width
        val scaleHeight: Float = height.toFloat() / bitmap.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaledHeight(
        photo: Photo, maxIndex: Int
    ) = if (photo.fixed) {
        photo.fixedHeight * maxIndex / photo.width
    } else {
        photo.height * maxIndex / photo.width
    }

    private suspend fun addResult(result: Result) {
        resultRepository.addResult(result)
    }
}
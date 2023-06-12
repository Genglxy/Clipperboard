package com.genglxy.clipperboard.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.genglxy.clipperboard.ClipperboardApplication.Companion.context
import com.genglxy.clipperboard.R
import com.genglxy.clipperboard.databinding.ClipperItemBinding
import com.genglxy.clipperboard.databinding.HistoryItemBinding
import com.genglxy.clipperboard.logic.model.Photo
import com.genglxy.clipperboard.logic.model.Result
import com.genglxy.clipperboard.ui.clipper.ClipperAdapter
import jp.wasabeef.glide.transformations.CropTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HistoryAdapter(private val historyList: List<Result>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private lateinit var job: CoroutineScope

    inner class ViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Result) {
            Glide.with(context).load(photo.uri).apply(
                RequestOptions.bitmapTransform(
                    CropTransformation(
                        photo.width, photo.height, CropTransformation.CropType.BOTTOM
                    )
                )
            ).into(binding.resultImage)
            binding.delete.setOnClickListener {

            }
            binding.detail.setOnClickListener {

            }
        }
    }
        /*
        private fun setHeight(photo: Photo) {
            val windowsWidth = context.resources.displayMetrics.widthPixels
            if (photo.fixed) {
                val params = binding.screenshot.layoutParams
                params.height =
                    (photo.fixedHeight.toDouble() / photo.width.toDouble() * windowsWidth.toDouble()).toInt()
                binding.screenshot.layoutParams = params
                Glide.with(fragment).load(photo.uri).apply(
                    RequestOptions.bitmapTransform(
                        CropTransformation(
                            photo.width, photo.fixedHeight, CropTransformation.CropType.BOTTOM
                        )
                    )
                ).into(binding.screenshot)
            } else {
                val params = binding.screenshot.layoutParams
                params.height =
                    ((photo.height.toDouble()) / photo.width.toDouble() * windowsWidth.toDouble()).toInt()
                binding.screenshot.layoutParams = params
                Glide.with(fragment).load(photo.uri).into(binding.screenshot)
            }
        }
    }

         */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = historyList[position]
        holder.bind(photo)
    }

    fun cancelJob() {
        job.cancel()
    }

    override fun getItemCount() = historyList.size
}
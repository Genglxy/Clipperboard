package com.genglxy.clipperboard.ui.clipper

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.genglxy.clipperboard.R
import com.genglxy.clipperboard.databinding.ClipperItemBinding
import com.genglxy.clipperboard.logic.model.Photo
import jp.wasabeef.glide.transformations.CropTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ClipperAdapter(
    private val fragment: Fragment,
    private val photoList: List<Photo>,
    private val onPhotoClicked: (show: Boolean) -> Unit,
    private val onEditClicked: (photo: Photo, position: Int) -> Unit,
    private val onCheckedClicked: () -> Unit,
    private val onDeleteClicked: (position: Int) -> Unit,
    private val onFinalTestClicked: (imageView: ImageView) -> Unit
) : RecyclerView.Adapter<ClipperAdapter.ViewHolder>() {

    private lateinit var job: CoroutineScope

    inner class ViewHolder(private val binding: ClipperItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photo) {
            setHeight(photo)
            binding.show.setOnClickListener {
                if (photo.checked) {
                    photo.checked = false
                    binding.show.setIconResource(R.drawable.unchecked_icon)
                    binding.screenshot.alpha = 0.5F
                } else {
                    photo.checked = true
                    binding.show.setIconResource(R.drawable.checked_icon)
                    binding.screenshot.alpha = 1F
                }
                onCheckedClicked()
            }
            binding.screenshot.setOnClickListener {
                if (photo.clicked++ == 0) {
                    binding.switchHeight.visibility = View.VISIBLE
                    binding.edit.visibility = View.VISIBLE
                    //binding.finalTest.visibility = View.VISIBLE
                    onPhotoClicked(false)
                }
                job = CoroutineScope(Dispatchers.Default)
                job.launch {
                    delay(3000)
                    if (job.isActive) {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (photo.clicked != 0) {
                                if (photo.clicked-- == 1) {
                                    binding.edit.visibility = View.GONE
                                    binding.switchHeight.visibility = View.GONE
                                    //binding.finalTest.visibility = View.GONE
                                    onPhotoClicked(true)
                                }
                            }
                        }
                    }
                }
            }
            binding.edit.setOnClickListener {
                onEditClicked(photo, adapterPosition)
            }
            binding.switchHeight.setOnClickListener {
                photo.fixed = !photo.fixed
                setHeight(photo)
            }
            /*
            binding.finalTest.setOnClickListener {
                val params = binding.screenshot.layoutParams
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.screenshot.layoutParams = params
                onFinalTestClicked(binding.screenshot)
            }

             */
            binding.delete.setOnClickListener {
                onDeleteClicked(adapterPosition)
            }
        }

        private fun setHeight(photo: Photo) {
            val windowsWidth = fragment.resources.displayMetrics.widthPixels
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ClipperItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photoList[position]
        holder.bind(photo)
    }

    fun cancelJob() {
        job.cancel()
    }

    override fun getItemCount() = photoList.size
}
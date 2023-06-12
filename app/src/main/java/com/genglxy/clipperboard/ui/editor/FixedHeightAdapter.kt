package com.genglxy.clipperboard.ui.editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.genglxy.clipperboard.databinding.FixedHeightItemBinding

class FixedHeightAdapter(
    private val fixedHeight: List<Int>, private val setHeight: (fixedHeight: Int) -> Unit
) : RecyclerView.Adapter<FixedHeightAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: FixedHeightItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(height: Int) {
            binding.changeHeight.text = "$height Px"
            binding.changeHeight.setOnClickListener {
                setHeight(height)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FixedHeightItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val height = fixedHeight[position]
        holder.bind(height)
    }

    override fun getItemCount() = fixedHeight.size

}


package com.genglxy.clipperboard.ui.editor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.genglxy.clipperboard.databinding.FragmentEditorBinding
import java.lang.NumberFormatException

class EditorFragment : Fragment() {
    private var _binding: FragmentEditorBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel by lazy { ViewModelProvider(this)[EditorViewModel::class.java] }
    private val args: EditorFragmentArgs by navArgs()

    private lateinit var adapter: FixedHeightAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(layoutInflater, container, false)
        val layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.fixedHeightRecyclerView.layoutManager = layoutManager
        adapter = FixedHeightAdapter(viewModel.fixedHeight) { height ->
            val topMargin = viewModel.photo!!.height - height
            changeMaskHeight(topMargin)
            binding.slider.value = topMargin.toFloat()
            binding.editText.setText(topMargin.toString())
            viewModel.sliderValue = topMargin.toFloat()
        }
        binding.fixedHeightRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.photo == null) {
            viewModel.photo = args.photo
        }
        if (viewModel.fixedHeight.isEmpty()) {
            for (height in args.fixedHeight) {
                viewModel.fixedHeight.add(height)
            }
        }

        if (viewModel.index == null) {
            viewModel.index = args.index
        }

        if (viewModel.photo != null) {
            if (viewModel.sliderValue == null) {
                if (viewModel.photo!!.fixedHeight == viewModel.photo!!.height) {
                    viewModel.sliderValue = 0F
                } else {
                    viewModel.sliderValue =
                        (viewModel.photo!!.height - viewModel.photo!!.fixedHeight).toFloat()
                }
            }
            Glide.with(this).load(viewModel.photo?.uri).into(binding.editorImageView)
            binding.slider.valueTo = viewModel.photo?.height!!.toFloat()
            binding.slider.value = viewModel.sliderValue!!
            changeMaskHeight(viewModel.sliderValue!!.toInt())

            binding.editText.setText(viewModel.sliderValue!!.toInt().toString())
            binding.slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    binding.editText.setText(value.toInt().toString())
                    viewModel.sliderValue = value
                    changeMaskHeight(viewModel.sliderValue!!.toInt())
                }
            }
            binding.editText.addTextChangedListener {
                try {
                    val content = it.toString().toFloat()
                    if (content <= viewModel.photo?.height!!) {
                        binding.warnText.visibility = View.GONE
                        binding.slider.value = content
                        viewModel.sliderValue = content
                        changeMaskHeight(viewModel.sliderValue!!.toInt())
                    } else {
                        throw NumberFormatException("1")
                    }
                } catch (e: NumberFormatException) {
                    if (e.message == "1") {
                        binding.warnText.text =
                            "The input number must be between 0 and ${viewModel.photo!!.height}."
                        binding.warnText.visibility = View.VISIBLE
                    } else {
                        binding.warnText.text = "Please enter a valid pixel count."
                        binding.warnText.visibility = View.VISIBLE
                    }
                }
            }
        }
        binding.apply.setOnClickListener {
            setFragmentResult(
                REQUEST_KEY_HEIGHT, bundleOf(
                    BUNDLE_KEY_HEIGHT to (viewModel.photo!!.height - viewModel.sliderValue!!.toInt()),
                    BUNDLE_KEY_ORDER to viewModel.index
                )
            )
            findNavController().popBackStack()
        }
    }

    companion object {
        const val REQUEST_KEY_HEIGHT = "REQUEST_KEY_HEIGHT"
        const val BUNDLE_KEY_HEIGHT = "BUNDLE_KEY_HEIGHT"
        const val BUNDLE_KEY_ORDER = "BUNDLE_KEY_ORDER"
    }

    private fun changeMaskHeight(height: Int) {
        val windowsWidth = this.resources.displayMetrics.widthPixels
        val grayMaskParams = binding.grayMask.layoutParams
        val invisibleMaskParams = binding.invisibleMask.layoutParams
        grayMaskParams.height =
            (height.toDouble() / viewModel.photo?.width!!.toDouble() * windowsWidth.toDouble()).toInt()
        invisibleMaskParams.height =
            ((viewModel.photo?.height!!.toDouble() - height.toDouble()) / viewModel.photo?.width!!.toDouble() * windowsWidth.toDouble()).toInt()
        binding.grayMask.layoutParams = grayMaskParams
        binding.invisibleMask.layoutParams = invisibleMaskParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
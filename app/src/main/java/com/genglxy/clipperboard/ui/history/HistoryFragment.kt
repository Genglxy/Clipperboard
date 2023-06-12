package com.genglxy.clipperboard.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.genglxy.clipperboard.databinding.FragmentEditorBinding
import com.genglxy.clipperboard.databinding.FragmentHistoryBinding
import com.genglxy.clipperboard.ui.editor.EditorFragmentArgs
import com.genglxy.clipperboard.ui.editor.EditorViewModel
import com.genglxy.clipperboard.ui.editor.FixedHeightAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HistoryFragment: Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel by lazy { ViewModelProvider(this)[HistoryViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        val layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerView.layoutManager = layoutManager
        viewModel.viewModelScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.historyList.collect { historyList ->
                    binding.historyRecyclerView.adapter = HistoryAdapter(historyList)
                }
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
    }
}
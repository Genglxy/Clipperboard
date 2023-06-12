package com.genglxy.clipperboard.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genglxy.clipperboard.logic.ResultRepository
import com.genglxy.clipperboard.logic.model.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HistoryViewModel: ViewModel() {
    private val resultRepository = ResultRepository.get()
    private val _historyList: MutableStateFlow<List<Result>> = MutableStateFlow(emptyList())
    val historyList: StateFlow<List<Result>>
        get() = _historyList.asStateFlow()
    init {
        viewModelScope.launch {
            resultRepository.getResults().collect {
                _historyList.value = it
            }
        }
    }
}
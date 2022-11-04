package com.hfad.guineapiglog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

interface LinkedEntityFetcher<T> {
    fun fetch(viewModel: ViewModel, id: Long, listToFill: MutableLiveData<List<T>>)
}
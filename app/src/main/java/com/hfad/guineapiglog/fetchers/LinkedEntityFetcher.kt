package com.hfad.guineapiglog.fetchers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

interface LinkedEntityFetcher<T> {
    fun fetch(viewModel: ViewModel, id: Long, listToFill: MutableLiveData<List<T>>)
}
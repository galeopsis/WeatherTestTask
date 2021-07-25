package com.galeopsis.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galeopsis.weatherapp.model.repository.WeatherRepository
import com.galeopsis.weatherapp.utils.LoadingState
import kotlinx.coroutines.launch

class MainViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    val data = weatherRepository.data

   /* init {
        fetchData()
    }*/

    fun fetchData(data: String) {
        viewModelScope.launch {
            try {
                _loadingState.value = LoadingState.LOADING
                weatherRepository.refresh(data)
                _loadingState.value = LoadingState.LOADED
            } catch (e: Exception) {
                _loadingState.value = LoadingState.error(e.message)
            }
        }
    }
}
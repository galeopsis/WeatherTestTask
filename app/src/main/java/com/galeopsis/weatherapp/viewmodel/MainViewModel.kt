package com.galeopsis.weatherapp.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchData(inputData: String, method: String) {
        viewModelScope.launch {
            try {
                Log.d("testWeatherApp", "Fetching data with: fetchData")
                _loadingState.postValue(LoadingState.LOADING)
                Log.d("testWeatherApp", "Fetching data with: $inputData, method: $method")

                weatherRepository.refresh(inputData, method)

                // Добавим проверку данных
                weatherRepository.data.value?.let {
                    Log.d("testWeatherApp", "Data received: ${it.size} items")
                } ?: Log.e("testWeatherApp", "Data is null")

                _loadingState.postValue(LoadingState.SUCCESS)
            } catch (e: Exception) {
                Log.e("testWeatherApp", "Error fetching data: ${e.stackTraceToString()}")
                _loadingState.postValue(LoadingState.error(e.message))
            }
        }
    }
}

package com.galeopsis.weatherapp.model.repository

import android.content.Context
import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import com.galeopsis.weatherapp.utils.SharedPreferencesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao,

    ) {
    private val context: Context? = null
    private val preferences by lazy { context?.let { SharedPreferencesUtils(it) } }
    val data = weatherDao.findAll()
    private var zip = preferences?.string
//    private val zip = "10001"

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val weatherData = zip?.let { weatherApi.getWeatherAsync(API_KEY, it).await() }
            weatherDao.deleteAllData()
            if (weatherData != null) {
                weatherDao.add(weatherData)
            }
        }
    }
}

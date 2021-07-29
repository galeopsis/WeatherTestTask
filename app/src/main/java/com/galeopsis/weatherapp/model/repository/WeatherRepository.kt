package com.galeopsis.weatherapp.model.repository

import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao,

    ) {

    val data = weatherDao.findAll()

    suspend fun refresh(data: String) {
        withContext(Dispatchers.IO) {
            val weatherData = weatherApi.getWeatherByCityNameAsync(API_KEY, data).await()
            weatherDao.deleteAllData()
            weatherDao.add(weatherData)
        }
    }
}


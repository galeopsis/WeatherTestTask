package com.galeopsis.weatherapp.model.repository

import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao

    ) {
    val data = weatherDao.findAll()

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val weatherData = weatherApi.getWeatherAsync().await()
            weatherDao.deleteAllData()
            weatherDao.add(weatherData)
        }
    }
}

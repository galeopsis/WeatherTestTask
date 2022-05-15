package com.galeopsis.weatherapp.model.repository

import android.util.Log
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
    suspend fun refresh(data: String, method: String) {
        withContext(Dispatchers.IO) {
            when (method) {
                "zip" -> {
                    val weatherData = weatherApi.getWeatherByZipCodeAsync(API_KEY, data).await()
                    weatherDao.deleteAllData()
                    weatherDao.add(weatherData)
                }
                "name" -> {
                    val weatherData = weatherApi.getWeatherByCityNameAsync(API_KEY, data).await()
                    weatherDao.deleteAllData()
                    weatherDao.add(weatherData)
                }
                "coordinates" -> {
                    val lat1 = data.substringAfter("=")
                    val lat = lat1.substringBefore("&")
                    val lon = data.substringAfterLast("=")
                    Log.d("latlonger", "refresh: lat = $lat long = $lon")
                    val weatherData = weatherApi.getWeatherByCoordinatesAsync(API_KEY, lat, lon).await()
                    weatherDao.deleteAllData()
                    weatherDao.add(weatherData)
                }
            }

        }
    }
}


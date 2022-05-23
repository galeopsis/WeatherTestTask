package com.galeopsis.weatherapp.model.repository

import android.util.Log
import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.FInfo
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import com.galeopsis.weatherapp.model.data.forecastResponse.RInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
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
                    val weatherData =
                        weatherApi.getWeatherByCoordinatesAsync(API_KEY, lat, lon).await()
                    weatherDao.deleteAllData()
                    weatherDao.add(weatherData)
                }
                "forecast" -> {
                    /*val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                    val currentDate = sdf.format(Date())*/
                    val lat1 = data.substringAfter("=")
                    val lat = lat1.substringBefore("&")
                    val lon = data.substringAfterLast("=")
                    val weatherData = weatherApi.getForecastAsync(API_KEY, lat, lon).await()
                    FInfo.dTemp = weatherData.list[10].main.temp?.toInt().toString()
                    FInfo.dDescription =
                        weatherData.list[10].weather[0]?.description.toString()
//                    Log.d("foreInfo", "${weatherData.list[10].main.temp}")
                    Log.d("foreInfo", "${FInfo.dTemp}")
                }
                else -> {}
            }
        }
    }
}


package com.galeopsis.weatherapp.model.repository

import android.util.Log
import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.FInfo
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
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
               /* "zip" -> {
                    val weatherData = weatherApi.getWeatherByZipCodeAsync(API_KEY, data).await()
                    weatherDao.deleteAllData()
                    weatherDao.add(weatherData)
                }*/
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
                    FInfo.lat = lat
                    FInfo.lon = lon
                    weatherDao.deleteAllData()
                    weatherDao.add(weatherData)
                }
                "forecast" -> {
                    val sdf = SimpleDateFormat("!yyyy-MM-dd hh:mm:ss", Locale.getDefault())
                    val cD = sdf.format(Date())
                    val forecastArray = mutableListOf<String>()
                    val l1 = cD.substringBefore(" ")
                    val currentDay = l1.substringAfterLast("-").toInt()
                    val lat1 = data.substringAfter("=")
                    val lat = lat1.substringBefore("&")
                    val lon = data.substringAfterLast("=")
                    val weatherData = weatherApi.getForecastAsync(API_KEY, lat, lon).await()
                    for (i in 0 until weatherData.list.size) {
                        val x1 = weatherData.list[i].dtTxt.substringBefore(" ")
                        val x2 = x1.substringAfterLast("-").toInt()
                        if (x2 == (currentDay + 1)) {
                            forecastArray.add(weatherData.list[i].dtTxt)
                            forecastArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastArray.add(weatherData.list[i].weather[0]?.description.toString())
                        }
                    }
                    val tomorrowTempAt15 = forecastArray[16]
                    val tomorrowDescAt15 = forecastArray[17]
                    Log.d("foreInfo", "${forecastArray.toList()}")
                    Log.d("foreInfo", "$tomorrowTempAt15, $tomorrowDescAt15")

                    FInfo.dTemp = tomorrowTempAt15
                    FInfo.dDescription = tomorrowDescAt15
                    FInfo.mainTemp = weatherData.list[0].main.temp?.toInt().toString()
                }
                else -> {}
            }
        }
    }
}


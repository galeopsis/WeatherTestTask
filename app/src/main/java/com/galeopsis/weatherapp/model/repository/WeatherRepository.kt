package com.galeopsis.weatherapp.model.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.FInfo
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class WeatherRepository(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
) {

    val data = weatherDao.findAll()

    @RequiresApi(Build.VERSION_CODES.O)
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
                    val forecastTomorrowArray = mutableListOf<String>()
                    val forecastAfterTomorrowArray = mutableListOf<String>()
                    val forecastAfterAfterTomorrowArray = mutableListOf<String>()
                    val forecastDaysArray = mutableListOf<String>()
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
                            forecastTomorrowArray.add(weatherData.list[i].dtTxt)
                            forecastTomorrowArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastTomorrowArray.add(weatherData.list[i].weather[0]?.description.toString())
                        }
                        if (x2 == (currentDay + 2)){
                            forecastAfterTomorrowArray.add(weatherData.list[i].dtTxt)
                            forecastAfterTomorrowArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastAfterTomorrowArray.add(weatherData.list[i].weather[0]?.description.toString())
                        }
                        if (x2 == (currentDay + 3)){
                            forecastAfterAfterTomorrowArray.add(weatherData.list[i].dtTxt)
                            forecastAfterAfterTomorrowArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastAfterAfterTomorrowArray.add(weatherData.list[i].weather[0]?.description.toString())
                        }
                    }

                    val monday = "понедельник"
                    val tuesday = "вторник"
                    val wednesday = "среда"
                    val thursday = "четверг"
                    val friday = "пятница"
                    val saturday = "суббота"
                    val sunday = "воскресенье"

                    val tomorrowTemp = forecastTomorrowArray[16]
                    val tomorrowDesc = forecastTomorrowArray[17]
                    val tomorrowAfterTemp = forecastAfterTomorrowArray[16]
                    val tomorrowAfterDesc = forecastAfterTomorrowArray[17]
                    val tomorrowAfterAfterTemp = forecastAfterAfterTomorrowArray[16]
                    val tomorrowAfterAfterDesc = forecastAfterAfterTomorrowArray[17]
                    /*Log.d("foreInfo", "${forecastTomorrowArray.toList()}")
                    Log.d("foreInfo", "${forecastAfterTomorrowArray.toList()}")
                    Log.d("foreInfo", "${forecastAfterAfterTomorrowArray.toList()}")*/

                    FInfo.dTempTomorrow = tomorrowTemp
                    FInfo.dDescriptionTomorrow = tomorrowDesc
                    FInfo.dTempAfterTomorrow = tomorrowAfterTemp
                    FInfo.dDescriptionAfterTomorrow = tomorrowAfterDesc
                    FInfo.dTempAfterAfterTomorrow = tomorrowAfterAfterTemp
                    FInfo.dDescriptionAfterAfterTomorrow = tomorrowAfterAfterDesc
                    FInfo.mainTemp = weatherData.list[0].main.temp?.toInt().toString()
                    FInfo.humidity = weatherData.list[0].main.humidity.toString()
                    FInfo.wind = weatherData.list[0].wind.speed.toInt().toString()
                    val dTomorrow = forecastTomorrowArray[15].substringBefore(" ")
                    val dAfterTomorrow = forecastAfterTomorrowArray[15].substringBefore(" ")
                    val dAfterAfterTomorrow = forecastAfterAfterTomorrowArray[15].substringBefore(" ")

                    val dateTomorrow = LocalDate.parse(dTomorrow)
                    val dateAfterTomorrow = LocalDate.parse(dAfterTomorrow)
                    val dateAfterAfterTomorrow = LocalDate.parse(dAfterAfterTomorrow)
                    val formatter = DateTimeFormatter.ofPattern("dd MMMM")
                    val formattedDateTomorrow = dateTomorrow.format(formatter)
                    val tomorrowDayOfWeek = dateTomorrow.dayOfWeek.toString()
                    val formattedDateAfterTomorrow = dateAfterTomorrow.format(formatter)
                    val tomorrowAfterDayOfWeek = dateAfterTomorrow.dayOfWeek.toString()
                    val formattedDateAfterAfterTomorrow = dateAfterAfterTomorrow.format(formatter)
                    val tomorrowAfterAfterDayOfWeek = dateAfterAfterTomorrow.dayOfWeek.toString()

                    FInfo.tomorrowDate = "${translateDayOfWeek(tomorrowDayOfWeek)}, $formattedDateTomorrow "
                    FInfo.tomorrowAfterDate = "${translateDayOfWeek(tomorrowAfterDayOfWeek)}, $formattedDateAfterTomorrow"
                    FInfo.tomorrowAfterAfterDate = "${translateDayOfWeek(tomorrowAfterAfterDayOfWeek)}, $formattedDateAfterAfterTomorrow"

                    Log.d("foreInfo", "${FInfo.tomorrowDate} ${FInfo.dTempTomorrow}, ${FInfo.dDescriptionTomorrow}")
                    Log.d("foreInfo", "${FInfo.tomorrowAfterDate} ${FInfo.dTempAfterTomorrow}, ${FInfo.dDescriptionAfterTomorrow}")
                    Log.d("foreInfo", "${FInfo.tomorrowAfterAfterDate} ${FInfo.dTempAfterAfterTomorrow}, ${FInfo.dDescriptionAfterAfterTomorrow}")

                }
                else -> {}
            }
        }
    }

    private fun translateDayOfWeek(date: String): String {
        var newDate = date
       if (date == "MONDAY") newDate = "понедельник"
       if (date == "TUESDAY") newDate = "вторник"
       if (date == "WEDNESDAY") newDate = "среда"
       if (date == "THURSDAY") newDate = "четверг"
       if (date == "FRIDAY") newDate = "пятница"
       if (date == "SATURDAY") newDate = "суббота"
       if (date == "SUNDAY") newDate = "воскресенье"
        return newDate
    }
}


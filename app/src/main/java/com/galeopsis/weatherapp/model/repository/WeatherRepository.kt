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
                    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
                    val cD = sdf.format(Date())

                    val forecastTomorrowArray = mutableListOf<String>()
                    val forecastAfterTomorrowArray = mutableListOf<String>()
                    val forecastAfterAfterTomorrowArray = mutableListOf<String>()

                    val iconForecastArray = mutableListOf<String>()
                    val iconAfterForecastArray = mutableListOf<String>()
                    val iconAfterAfterForecastArray = mutableListOf<String>()

                    val lat1 = data.substringAfter("=")
                    val lat = lat1.substringBefore("&")
                    val lon = data.substringAfterLast("=")
                    val currentDate = cD.substringBefore(" ")
                    val weatherData = weatherApi.getForecastAsync(API_KEY, lat, lon).await()

                    val dayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val mainDate = LocalDate.parse(currentDate, dayFormat)
                    val afterMainDate = mainDate.plusDays(1)
                    val afterAfterMainDate = mainDate.plusDays(2)
                    val afterAfterAfterMainDate = mainDate.plusDays(3)

                    Log.d("foreInfo", "$mainDate $afterMainDate $afterAfterMainDate $afterAfterAfterMainDate")

                    for (i in 0 until weatherData.list.size) {
                        val x1 = weatherData.list[i].dtTxt.substringBefore(" ")

                        if (x1 == afterMainDate.toString()) {
                            forecastTomorrowArray.add(weatherData.list[i].dtTxt)
                            forecastTomorrowArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastTomorrowArray.add(weatherData.list[i].weather[0]?.description.toString())
                            iconForecastArray.add(weatherData.list[i].dtTxt)
                            iconForecastArray.add(weatherData.list[i].weather[0].toString())
                        }
                        if (x1 == afterAfterMainDate.toString()){
                            forecastAfterTomorrowArray.add(weatherData.list[i].dtTxt)
                            forecastAfterTomorrowArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastAfterTomorrowArray.add(weatherData.list[i].weather[0]?.description.toString())
                            iconAfterForecastArray.add(weatherData.list[i].dtTxt)
                            iconAfterForecastArray.add(weatherData.list[i].weather[0].toString())
                        }
                        if (x1 == afterAfterAfterMainDate.toString()){
                            forecastAfterAfterTomorrowArray.add(weatherData.list[i].dtTxt)
                            forecastAfterAfterTomorrowArray.add(weatherData.list[i].main.temp?.toInt().toString())
                            forecastAfterAfterTomorrowArray.add(weatherData.list[i].weather[0]?.description.toString())
                            iconAfterAfterForecastArray.add(weatherData.list[i].dtTxt)
                            iconAfterAfterForecastArray.add(weatherData.list[i].weather[0].toString())
                        }
                    }

                    Log.d("foreInfo", "${iconForecastArray}")
                    Log.d("foreInfo", "${iconAfterForecastArray}")
                    Log.d("foreInfo", "${iconAfterAfterForecastArray}")

                    Log.d("foreInfo", "${iconForecastArray[10]} ${iconForecastArray[11]}")
                    Log.d("foreInfo", "${iconAfterForecastArray[10]} ${iconAfterForecastArray[11]}")
                    Log.d("foreInfo", "${iconAfterAfterForecastArray[10]} ${iconAfterAfterForecastArray[11]}")

                    val i1= iconForecastArray[11].substringAfter("icon=")
                    val icon1 = i1.substringBefore(",")
                    val i2= iconAfterForecastArray[11].substringAfter("icon=")
                    val icon2 = i2.substringBefore(",")
                    val i3= iconAfterAfterForecastArray[11].substringAfter("icon=")
                    val icon3 = i3.substringBefore(",")

                    FInfo.tomorrowIcon = icon1
                    FInfo.tomorrowAfterIcon = icon2
                    FInfo.tomorrowAfterAfterIcon = icon3




                    val tomorrowTemp = forecastTomorrowArray[16]
                    val tomorrowDesc = forecastTomorrowArray[17]
                    val tomorrowAfterTemp = forecastAfterTomorrowArray[16]
                    val tomorrowAfterDesc = forecastAfterTomorrowArray[17]
                    val tomorrowAfterAfterTemp = forecastAfterAfterTomorrowArray[16]
                    val tomorrowAfterAfterDesc = forecastAfterAfterTomorrowArray[17]

                   /* Log.d("foreInfo", "${weatherData.list[0].weather.toList()}")
                    Log.d("foreInfo", "temp now = ${weatherData.list[0].main.temp?.toInt().toString()}")
                    Log.d("foreInfo", "$forecastTomorrowArray")
                    Log.d("foreInfo", "$forecastAfterTomorrowArray")
                    Log.d("foreInfo", "$forecastAfterAfterTomorrowArray")*/

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

                   /* Log.d("foreInfo", "${FInfo.tomorrowDate} ${FInfo.dTempTomorrow}, ${FInfo.dDescriptionTomorrow} ${FInfo.tomorrowIcon} ")
                    Log.d("foreInfo", "${FInfo.tomorrowAfterDate} ${FInfo.dTempAfterTomorrow}, ${FInfo.dDescriptionAfterTomorrow} ${FInfo.tomorrowAfterIcon} ")
                    Log.d("foreInfo", "${FInfo.tomorrowAfterAfterDate} ${FInfo.dTempAfterAfterTomorrow}, ${FInfo.dDescriptionAfterAfterTomorrow} ${FInfo.tomorrowAfterAfterIcon} ")*/

                }
                else -> {}
            }
        }
    }

    private fun translateDayOfWeek(date: String): String {
        var newDate = date
       if (date == "MONDAY") newDate = "в понедельник"
       if (date == "TUESDAY") newDate = "во вторник"
       if (date == "WEDNESDAY") newDate = "в среду"
       if (date == "THURSDAY") newDate = "в четверг"
       if (date == "FRIDAY") newDate = "в пятницу"
       if (date == "SATURDAY") newDate = "в субботу"
       if (date == "SUNDAY") newDate = "в воскресенье"
        return newDate
    }
}


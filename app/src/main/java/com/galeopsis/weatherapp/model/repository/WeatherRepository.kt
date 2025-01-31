package com.galeopsis.weatherapp.model.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.FInfo
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import com.galeopsis.weatherapp.utils.unixTimestampToTimeString
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
            Log.d("testWeatherApp", "Запрос данных: inputData=$data, method=$method")
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

                    val timezone = weatherData.city?.timezone
                    val xxx = timezone?.let { weatherData.list[0].dt.unixTimestampToTimeString(it) }

                    val dayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val mainDate = LocalDate.parse(currentDate, dayFormat)
                    val afterMainDate = mainDate.plusDays(1)
                    val afterAfterMainDate = mainDate.plusDays(2)
                    val afterAfterAfterMainDate = mainDate.plusDays(3)

                    Log.d("foreInfo", "$mainDate $afterMainDate $afterAfterMainDate $afterAfterAfterMainDate ttt $xxx")

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

                    val fMax1 = maxOf(
                        forecastTomorrowArray[1].toInt(),
                        forecastTomorrowArray[4].toInt(),
                        forecastTomorrowArray[7].toInt(),
                        forecastTomorrowArray[10].toInt(),
                        forecastTomorrowArray[13].toInt(),
                        forecastTomorrowArray[16].toInt(),
                        forecastTomorrowArray[19].toInt(),
                        forecastTomorrowArray[22].toInt()
                    )
                    val fMax2 = maxOf(
                        forecastAfterTomorrowArray[1].toInt(),
                        forecastAfterTomorrowArray[4].toInt(),
                        forecastAfterTomorrowArray[7].toInt(),
                        forecastAfterTomorrowArray[10].toInt(),
                        forecastAfterTomorrowArray[13].toInt(),
                        forecastAfterTomorrowArray[16].toInt(),
                        forecastAfterTomorrowArray[19].toInt(),
                        forecastAfterTomorrowArray[22].toInt()
                    )
                    val fMax3 = maxOf(
                        forecastAfterAfterTomorrowArray[1].toInt(),
                        forecastAfterAfterTomorrowArray[4].toInt(),
                        forecastAfterAfterTomorrowArray[7].toInt(),
                        forecastAfterAfterTomorrowArray[10].toInt(),
                        forecastAfterAfterTomorrowArray[13].toInt(),
                        forecastAfterAfterTomorrowArray[16].toInt(),
                        forecastAfterAfterTomorrowArray[19].toInt(),
                        forecastAfterAfterTomorrowArray[22].toInt()
                    )

                    Log.d("foreInfo", "${forecastTomorrowArray.toList()}")
                    Log.d("foreInfo", "${forecastAfterTomorrowArray.toList()}")
                    Log.d("foreInfo", "${forecastAfterAfterTomorrowArray.toList()}")

                    Log.d("foreInfo", "${iconForecastArray[6]} ${iconForecastArray[7]}")
                    Log.d("foreInfo", "${iconAfterForecastArray[6]} ${iconAfterForecastArray[7]}")
                    Log.d("foreInfo", "${iconAfterAfterForecastArray[6]} ${iconAfterAfterForecastArray[7]}")

                    val i1= iconForecastArray[7].substringAfter("icon=")
                    val icon1 = i1.substringBefore(",")
                    val i2= iconAfterForecastArray[7].substringAfter("icon=")
                    val icon2 = i2.substringBefore(",")
                    val i3= iconAfterAfterForecastArray[7].substringAfter("icon=")
                    val icon3 = i3.substringBefore(",")

                    FInfo.tomorrowIcon = icon1
                    FInfo.tomorrowAfterIcon = icon2
                    FInfo.tomorrowAfterAfterIcon = icon3


                    val tomorrowTemp = fMax1.toString()
                    val tomorrowDesc = forecastTomorrowArray[11]
                    val tomorrowAfterTemp = fMax2.toString()
                    val tomorrowAfterDesc = forecastAfterTomorrowArray[11]
                    val tomorrowAfterAfterTemp = fMax3.toString()
                    val tomorrowAfterAfterDesc = forecastAfterAfterTomorrowArray[11]



                    /*val tomorrowTemp = forecastTomorrowArray[10]
                    val tomorrowDesc = forecastTomorrowArray[11]
                    val tomorrowAfterTemp = forecastAfterTomorrowArray[10]
                    val tomorrowAfterDesc = forecastAfterTomorrowArray[11]
                    val tomorrowAfterAfterTemp = forecastAfterAfterTomorrowArray[10]
                    val tomorrowAfterAfterDesc = forecastAfterAfterTomorrowArray[11]*/

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
                    val dTomorrow = forecastTomorrowArray[9].substringBefore(" ")
                    val dAfterTomorrow = forecastAfterTomorrowArray[9].substringBefore(" ")
                    val dAfterAfterTomorrow = forecastAfterAfterTomorrowArray[9].substringBefore(" ")

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


package com.galeopsis.weatherapp.model.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

const val WEATHER_ID = 0

@Entity(tableName = "weather")
data class WeatherEntity(
    val base: String?,
    @Embedded
    val clouds: Clouds?,
    val cod: Int?,
    @Embedded
    val coord: Coord?,
    val dt: Int?,
    val id: Int?,
    @Embedded
    val main: Main?,
    val name: String?,
   /* @Embedded
    val weather: List<Weather?>,*/
    @Embedded
    val sys: Sys?,
    val timezone: Int?,
    val visibility: Int?,
    @Embedded
    val wind: Wind?
) {
    @PrimaryKey(autoGenerate = false)
    var dataBaseId: Int = WEATHER_ID
}

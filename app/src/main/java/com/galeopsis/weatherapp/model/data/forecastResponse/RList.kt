package com.galeopsis.weatherapp.model.data.forecastResponse


import com.google.gson.annotations.SerializedName

data class RList(
    val RClouds: RClouds,
    val dt: Int,
    @SerializedName("dt_txt")
    val dtTxt: String,
    val main: RMain,
    val pop: Double,
    val RRain: RRain,
    val RSys: RSys,
    val visibility: Int,
    val wind: RWind,
    val weather: List<RWeather?>
)
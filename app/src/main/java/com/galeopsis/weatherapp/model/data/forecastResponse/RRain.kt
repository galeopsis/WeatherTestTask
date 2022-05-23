package com.galeopsis.weatherapp.model.data.forecastResponse


import com.google.gson.annotations.SerializedName

data class RRain(
    @SerializedName("3h")
    val h: Double
)
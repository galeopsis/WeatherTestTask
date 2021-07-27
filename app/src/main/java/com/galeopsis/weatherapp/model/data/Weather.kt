package com.galeopsis.weatherapp.model.data

import com.google.gson.annotations.SerializedName

data class Weather(
    val icon: String?,
    @SerializedName("id")
    val weather_id: Int?,
    val main: String?
)
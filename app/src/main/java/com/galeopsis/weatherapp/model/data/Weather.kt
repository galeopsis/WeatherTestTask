package com.galeopsis.weatherapp.model.data

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("id")
    val weatherId: Int,
    val main: String,
    val description: String,
    val icon: String
)
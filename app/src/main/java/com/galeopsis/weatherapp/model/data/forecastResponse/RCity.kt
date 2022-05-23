package com.galeopsis.weatherapp.model.data.forecastResponse

data class RCity(
    val RCoord: RCoord,
    val country: String,
    val id: Int,
    val name: String?,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)
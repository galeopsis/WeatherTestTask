package com.galeopsis.weatherapp.model.data.forecastResponse

data class RResponse (
    val city: RCity?,
    val cnt: Int,
    val cod: String,
    val list: List<RList>,
    val message: Int
)
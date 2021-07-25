package com.galeopsis.weatherapp.model.api

import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.data.WeatherEntity
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    fun getWeatherAsync(
        @Query("units") units: String = "metric",
        @Query("appid") appId: String = API_KEY,
        @Query("zip") zip: String = "94040",
        @Query("lang") language: String = "us"
    ): Deferred<WeatherEntity>
}
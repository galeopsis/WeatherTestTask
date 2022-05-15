package com.galeopsis.weatherapp.model.api

import com.galeopsis.weatherapp.BuildConfig.API_KEY
import com.galeopsis.weatherapp.model.data.WeatherEntity
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather?units=metric&lang=ru")
    fun getWeatherByZipCodeAsync(
        @Query("appid") appId: String = API_KEY,
        @Query("zip") zip: String
    ): Deferred<WeatherEntity>

    @GET("weather?units=metric&lang=ru")
    fun getWeatherByCityNameAsync(
        @Query("appid") appId: String = API_KEY,
        @Query("q") q: String
    ): Deferred<WeatherEntity>

    @GET("weather?units=metric&lang=ru")
    fun getWeatherByCoordinatesAsync(
        @Query("appid") appId: String = API_KEY,
        @Query("lat") lat: String,
        @Query("lon") long: String
    ): Deferred<WeatherEntity>
}
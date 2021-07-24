package com.galeopsis.weatherapp.model.api

import com.galeopsis.weatherapp.model.data.WeatherEntity
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface WeatherApi {

    @GET("weather?zip=660132,ru&units=metric&appid=6d167d56b8ba7026a1e2247aba7cf5ec")
    fun getWeatherAsync(
        /* @Query("zip") zip: String,
         @Query("lang") lang: String,
         @Query("units") units: String,
         @Query("appid") appid: String = API_KEY*/
    ): Deferred<WeatherEntity>
}
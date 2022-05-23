package com.galeopsis.weatherapp.utils

import android.util.Log
import androidx.room.TypeConverter
import com.galeopsis.weatherapp.model.data.Weather
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken

class ListConverter {
    @TypeConverter
    fun listToJson(weather: List<Weather?>?): String? {
        if (weather == null) return null
        val type = object : TypeToken<List<Weather?>?>() {}.type
        val json = Gson().toJson(weather, type)
        Log.i("JSON", "toJson: $json")
        return if (weather.isEmpty()) null else json
    }

    @TypeConverter
    fun jsonToList(json: String?): List<Weather>? {
        val gson = Gson()
        val type = object : TypeToken<List<Weather?>?>() {}.type
        return gson.fromJson<List<Weather>>(json, type)
    }
}
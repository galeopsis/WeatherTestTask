package com.galeopsis.weatherapp.utils

import com.galeopsis.weatherapp.model.data.Weather
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ListConverter {
    @androidx.room.TypeConverter
    fun fromString(value: String?): List<Weather?>? {
        val listType: Type = object : TypeToken<List<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @androidx.room.TypeConverter
    fun fromList(list: List<Weather?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}
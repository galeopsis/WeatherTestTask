package com.galeopsis.weatherapp.model

import androidx.room.Database
import androidx.room.RoomDatabase
import com.galeopsis.weatherapp.model.dao.WeatherDao
import com.galeopsis.weatherapp.model.data.WeatherEntity

@Database(entities = [WeatherEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}
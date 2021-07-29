package com.galeopsis.weatherapp.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.galeopsis.weatherapp.model.dao.WeatherDao
import com.galeopsis.weatherapp.model.data.WeatherEntity
import com.galeopsis.weatherapp.utils.ListConverter

@TypeConverters(ListConverter::class)
@Database(entities = [WeatherEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val weatherDao: WeatherDao
}
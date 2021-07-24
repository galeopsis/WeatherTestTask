package com.galeopsis.weatherapp.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.galeopsis.weatherapp.model.data.WeatherEntity

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather")
    fun findAll(): LiveData<List<WeatherEntity>?>

    @Query("DELETE FROM weather")
    fun deleteAllData()

}
package com.galeopsis.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.galeopsis.weatherapp.ui.WeatherMainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, WeatherMainFragment.newInstance())
                .commitNow()
        }
    }
}
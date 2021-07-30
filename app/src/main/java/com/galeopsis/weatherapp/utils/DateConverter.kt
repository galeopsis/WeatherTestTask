package com.galeopsis.weatherapp.utils

import java.text.SimpleDateFormat
import java.util.*

fun Int.unixTimestampToTimeString(zone: Int): String {

    try {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this * 1000.toLong()
        val localZone = zone / 3600
        val outputDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
        outputDateFormat.timeZone = TimeZone.getTimeZone("GMT+$localZone")
        return outputDateFormat.format(calendar.time)

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return this.toString()
}
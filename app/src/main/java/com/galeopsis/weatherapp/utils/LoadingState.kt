package com.galeopsis.weatherapp.utils

sealed class LoadingState {
    object LOADING : LoadingState()
    object SUCCESS : LoadingState()
    object FAILED : LoadingState()
    data class ERROR(val message: String?) : LoadingState()

    companion object {
        fun error(msg: String?) = ERROR(msg)
    }
}

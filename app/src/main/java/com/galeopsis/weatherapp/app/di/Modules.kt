package com.galeopsis.weatherapp.app.di

import android.app.Application
import androidx.room.Room
import com.galeopsis.weatherapp.model.AppDatabase
import com.galeopsis.weatherapp.model.api.WeatherApi
import com.galeopsis.weatherapp.model.dao.WeatherDao
import com.galeopsis.weatherapp.model.repository.WeatherRepository
import com.galeopsis.weatherapp.viewmodel.MainViewModel
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

val viewModelModule = module {
    single { MainViewModel(get()) }
}

val apiModule = module {
    fun provideUserApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }

    single { provideUserApi(get()) }
}

val netModule = module {
    fun provideCache(application: Application): Cache {
        val cacheSize = 10 * 1024 * 1024
        return Cache(application.cacheDir, cacheSize.toLong())
    }

    fun provideHttpClient(cache: Cache): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .cache(cache)

        return okHttpClientBuilder.build()
    }

    fun provideGson(): Gson {
        return GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()
    }


    fun provideRetrofit(factory: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(factory))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()

    }

    single { provideCache(androidApplication()) }
    single { provideHttpClient(get()) }
    single { provideGson() }
    single { provideRetrofit(get(), get()) }

}

val databaseModule = module {

    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "weather.database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }


    fun provideDao(database: AppDatabase): WeatherDao {
        return database.weatherDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
}

val repositoryModule = module {
    fun provideUserRepository(api: WeatherApi, dao: WeatherDao): WeatherRepository {
        return WeatherRepository(api, dao)
    }

    single { provideUserRepository(get(), get()) }
}
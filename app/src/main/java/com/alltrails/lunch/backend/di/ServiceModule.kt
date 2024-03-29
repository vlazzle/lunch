package com.alltrails.lunch.backend.di

import com.alltrails.lunch.BuildConfig
import com.alltrails.lunch.backend.PlacesService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun providePlacesService(): PlacesService {
        // TODO: use multibindings for interceptors
        val httpClientBuilder = OkHttpClient.Builder()
            .readTimeout(10L, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val url = originalRequest.url.newBuilder()
                    .addQueryParameter("key", BuildConfig.MAPS_API_KEY)
                    .addQueryParameter("keyword", "restaurant")
                    // Set rankby=distance because https://github.com/googlemaps/openapi-specification/pull/364
                    .addQueryParameter("rankby", "distance")
                    .build()
                val newRequest = originalRequest.newBuilder()
                    .url(url)
                    .build()
                chain.proceed(newRequest)
            }
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            httpClientBuilder.addInterceptor(loggingInterceptor)
        }
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .client(httpClientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
        return retrofit.create()
    }
}
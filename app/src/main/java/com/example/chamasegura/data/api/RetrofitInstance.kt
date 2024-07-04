// RetrofitInstance.kt
package com.example.chamasegura.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private fun getRetrofitInstance(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://queim-aqui-api.vercel.app/api/")
           // .baseUrl("https://api-chama-segura.vercel.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    fun create(context: Context): ApiService {
        return getRetrofitInstance(context).create(ApiService::class.java)
    }

    private val geoApiRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://json.geoapi.pt/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val geoApiService: GeoApiService by lazy {
        geoApiRetrofit.create(GeoApiService::class.java)
    }
}

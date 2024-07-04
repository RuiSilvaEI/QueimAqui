package com.example.chamasegura.data.api

import com.example.chamasegura.data.entities.LocationResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GeoApiService {
    @GET("gps/{latitude},{longitude}/base")
    fun getLocationInfo(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Call<LocationResponse>
}

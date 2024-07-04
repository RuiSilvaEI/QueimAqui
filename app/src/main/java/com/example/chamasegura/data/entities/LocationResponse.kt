package com.example.chamasegura.data.entities

data class LocationResponse(
    val lon: Double,
    val lat: Double,
    val distrito: String,
    val concelho: String,
    val freguesia: String
)

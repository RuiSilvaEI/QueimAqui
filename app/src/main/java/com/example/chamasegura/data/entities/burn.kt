package com.example.chamasegura.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Burn(
    val id: Int,
    val date: String,
    val reason: String,
    val latitude: Float,
    val longitude: Float,
    val otherData: String?,
    val createdAt: String,
    val updatedAt: String,
    val userId: Int,
    val type: BurnType,
    val distrito: String,
    val concelho: String,
    val freguesia: String,
    val state: BurnState
) : Parcelable

enum class BurnType {
    REGCLEAN,
    PARTICULAR
}

enum class BurnState {
    APPROVED,
    PENDING,
    DENIED
}
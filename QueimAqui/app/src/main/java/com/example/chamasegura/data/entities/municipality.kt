package com.example.chamasegura.data.entities

data class Municipality(
    val id: Int,
    val name: String,
    val taxNumber: String,
    val address: String,
    val state: State,
    val responsible: Int?,
    val createdAt: String,
    val updatedAt: String
)

enum class State {
    APPROVED,
    PENDING,
    DENIED,
    DELETED
}
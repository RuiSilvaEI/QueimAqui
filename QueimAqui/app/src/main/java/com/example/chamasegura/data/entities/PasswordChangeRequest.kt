package com.example.chamasegura.data.entities

data class PasswordChangeRequest(
    val oldPassword: String,
    val newPassword: String
)
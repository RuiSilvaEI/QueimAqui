package com.example.chamasegura.utils

import android.util.Log
import com.auth0.android.jwt.JWT
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.entities.UserType

object JwtUtils {

    fun getUserIdFromToken(token: String): Int? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("id").asInt()
        } catch (e: Exception) {
            null
        }
    }

    fun getUserTypeFromToken(token: String): UserType {
        return try {
            val jwt = JWT(token)
            val type = jwt.getClaim("type").asString()
            when (type) {
                "ICNF" -> UserType.ICNF
                "CM" -> UserType.CM
                else -> UserType.REGULAR
            }
        } catch (e: Exception) {
            UserType.REGULAR
        }
    }

    fun getUserStateFromToken(token: String): StateUser {
        return try {
            val jwt = JWT(token)
            val state = jwt.getClaim("state").asString()
            when (state) {
                "DISABLED" -> StateUser.DISABLED
                else -> StateUser.ENABLED
            }
        } catch (e: Exception) {
            StateUser.ENABLED
        }
    }
}

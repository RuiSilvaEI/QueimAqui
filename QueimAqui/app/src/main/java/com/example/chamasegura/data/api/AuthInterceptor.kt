package com.example.chamasegura.data.api

import android.content.Context
import com.example.chamasegura.utils.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val authManager = AuthManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = authManager.getToken()

        val authenticatedRequest = token?.let {
            request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        } ?: request

        return chain.proceed(authenticatedRequest)
    }
}
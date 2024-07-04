package com.example.chamasegura.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chamasegura.data.api.RetrofitInstance
import com.example.chamasegura.data.entities.LoginResponse
import com.example.chamasegura.data.entities.PasswordChangeRequest
import com.example.chamasegura.data.entities.PasswordChangeRequestIcnf
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.entities.User
import com.example.chamasegura.utils.AuthManager
import com.example.chamasegura.utils.JwtUtils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository(private val context: Context) {

    private val api = RetrofitInstance.create(context)
    private val authManager = AuthManager(context)

    fun signIn(email: String, password: String, onResult: (User?) -> Unit) {
        val credentials = mapOf("email" to email, "password" to password)
        api.signIn(credentials).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        authManager.saveAuthData(loginResponse.name, loginResponse.token)
                        val userId = JwtUtils.getUserIdFromToken(loginResponse.token) ?: 0
                        // Fetch user profile to get additional details like nif
                        api.getUser(userId).enqueue(object : Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                if (response.isSuccessful) {
                                    response.body()?.let { user ->
                                        onResult(user)
                                    } ?: run {
                                        Log.e("UserRepository", "User profile response body is null")
                                        onResult(null)
                                    }
                                } else {
                                    Log.e("UserRepository", "Failed to fetch user profile")
                                    onResult(null)
                                }
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Log.e("UserRepository", "Failed to fetch user profile: ${t.message}")
                                onResult(null)
                            }
                        })
                    } ?: run {
                        Log.e("UserRepository", "Login response body is null")
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun signUp(user: User, onResult: (LoginResponse?) -> Unit) {
        api.signUp(user).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun getUser(userId: Int): LiveData<User?> {
        val data = MutableLiveData<User?>()
        api.getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                data.value = response.body()
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                data.value = null
            }
        })
        return data
    }

    fun updateUserState(userId: Int, newState: StateUser, onResult: (Boolean, String?) -> Unit) {
        val requestBody = mapOf("state" to newState.name)
        api.updateUserState(userId, requestBody).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }


    suspend fun getNumberOfBurnRequests(userId: Int): Int {
        return api.getNumberOfBurnRequests(userId).count
    }

    fun getUserById(userId: Int): Call<User> {
        return api.getUser(userId)
    }

    fun updateUser(id: Int, user: User, onResult: (Boolean, String?) -> Unit) {
        api.updateUser(id, user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun getAllUsers(onResult: (List<User>?) -> Unit) {
        api.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun updatePhoto(id: Int, photo: MultipartBody.Part, onResult: (Boolean, String?) -> Unit) {
        api.updatePhoto(id, photo).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun changePassword(userId: Int, oldPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val token = authManager.getToken() ?: return onResult(false, "No token found")

        val request = PasswordChangeRequest(oldPassword, newPassword)
        api.changePassword(userId, "Bearer $token", request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun changePasswordIcnf(userId: Int, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val token = authManager.getToken() ?: return onResult(false, "No token found")

        val request = PasswordChangeRequestIcnf(newPassword = newPassword)
        api.changePasswordIcnf(userId, "Bearer $token", request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun sendContactMessage(token: String, subject: String, message: String, onResult: (Boolean, String?) -> Unit) {
        val messageMap = mapOf("subject" to subject, "message" to message)
        api.sendContactMessage("Bearer $token", messageMap).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun sendPasswordResetCode(email: String, onResult: (Boolean, String?) -> Unit) {
        val request = mapOf("email" to email)
        api.sendPasswordResetCode(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = if (response.code() == 404) {
                        "Email not found"
                    } else {
                        response.errorBody()?.string()
                    }
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun verifyPasswordResetCode(code: String, onResult: (Boolean, String?) -> Unit) {
        val request = mapOf("token" to code)
        api.verifyPasswordResetCode(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }

    fun resetPassword(token: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val request = mapOf("token" to token, "password" to password)
        api.resetPassword(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false, t.message)
            }
        })
    }
}
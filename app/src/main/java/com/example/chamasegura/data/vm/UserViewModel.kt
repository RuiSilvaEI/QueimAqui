package com.example.chamasegura.data.vm

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chamasegura.data.entities.LoginResponse
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.entities.User
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.repository.UserRepository
import com.example.chamasegura.utils.AuthManager
import com.example.chamasegura.utils.JwtUtils
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application.applicationContext)
    val user: MutableLiveData<User> = MutableLiveData()
    val users: MutableLiveData<List<User>?> = MutableLiveData()
    private var isSortedAscending = true
    private var originalUsers: List<User>? = null
    val numberOfBurnRequests = MutableLiveData<Int>()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            repository.signIn(email, password) { user ->
                user?.let {
                    Log.d("UserViewModel", "User state: ${it.state}")
                    if (it.state == StateUser.ENABLED) {
                        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("user_type", it.type.name).apply()
                        Log.d("UserViewModel", "User type saved: ${it.type.name}")
                        this@UserViewModel.user.postValue(it)
                    } else {
                        this@UserViewModel.user.postValue(null)
                        Log.e("UserViewModel", "Account is disabled")
                    }
                } ?: run {
                    this@UserViewModel.user.postValue(null)
                    Log.e("UserViewModel", "Login failed")
                }
            }
        }
    }

    fun signUp(user: User, onResult: (LoginResponse?) -> Unit) {
        viewModelScope.launch {
            repository.signUp(user, onResult)
        }
    }

    fun getUser(userId: Int): LiveData<User?> {
        return repository.getUser(userId)
    }

    fun updateUserState(userId: Int, newState: StateUser, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updateUserState(userId, newState, onResult)
        }
    }

    fun getNumberOfBurnRequests(userId: Int) {
        viewModelScope.launch {
            val count = repository.getNumberOfBurnRequests(userId)
            numberOfBurnRequests.postValue(count)
        }
    }

    fun getUserById(userId: Int): LiveData<User?> {
        return repository.getUser(userId)
    }

    fun getAllUsers() {
        viewModelScope.launch {
            repository.getAllUsers {
                originalUsers = it
                users.postValue(it)
            }
        }
    }

    fun sortUsersByName() {
        val sortedUsers = if (isSortedAscending) {
            users.value?.sortedBy { it.name }
        } else {
            users.value?.sortedByDescending { it.name }
        }
        isSortedAscending = !isSortedAscending
        users.postValue(sortedUsers)
    }

    fun searchUsers(query: String) {
        if (query.isEmpty()) {
            users.postValue(originalUsers)
        } else {
            val filteredUsers = originalUsers?.filter {
                it.name.contains(query, true) || it.email.contains(query, true)
            }
            users.postValue(filteredUsers)
        }
    }

    fun updateUser(id: Int, user: User, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updateUser(id, user, onResult)
        }
    }

    fun updatePhoto(id: Int, photo: MultipartBody.Part, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updatePhoto(id, photo, onResult)
        }
    }

    fun changePassword(userId: Int, oldPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.changePassword(userId, oldPassword, newPassword, onResult)
        }
    }

    fun changePasswordIcnf(userId: Int, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.changePasswordIcnf(userId, newPassword, onResult)
        }
    }

    fun sendContactMessage(subject: String, message: String, onResult: (Boolean, String?) -> Unit) {
        val token = AuthManager(getApplication()).getToken()
        if (token != null) {
            val userId = JwtUtils.getUserIdFromToken(token)
            if (userId != null) {
                repository.getUser(userId).observeForever { user ->
                    if (user != null) {
                        val fullMessage = """
                        |$message
                        |
                        |
                        |Nome: ${user.name}
                        |Email: ${user.email}
                        |NIF: ${user.nif}
                    """.trimMargin()
                        repository.sendContactMessage(token, subject, fullMessage, onResult)
                    } else {
                        onResult(false, "User data not found")
                    }
                }
            } else {
                onResult(false, "User ID not found")
            }
        } else {
            onResult(false, "Token not found")
        }
    }

    fun sendPasswordResetCode(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.sendPasswordResetCode(email, onResult)
        }
    }

    fun verifyPasswordResetCode(code: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.verifyPasswordResetCode(code, onResult)
        }
    }

    fun resetPassword(token: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.resetPassword(token, newPassword, onResult)
        }
    }
}
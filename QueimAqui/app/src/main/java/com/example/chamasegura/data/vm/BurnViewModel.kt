package com.example.chamasegura.data.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.chamasegura.data.api.RetrofitInstance
import com.example.chamasegura.data.entities.Burn
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.entities.LocationResponse
import com.example.chamasegura.data.repository.BurnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BurnViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = BurnRepository(application.applicationContext)
    val burns: MutableLiveData<List<Burn>?> = MutableLiveData()
    val createBurnResult: MutableLiveData<Pair<Boolean, String?>> = MutableLiveData()
    private var isSortedDescending = true
    val locationInfo: MutableLiveData<LocationResponse> = MutableLiveData()
    private val _pendingBurns = MutableLiveData<List<Burn>?>()
    val pendingBurns: MutableLiveData<List<Burn>?> get() = _pendingBurns

    fun getBurns() {
        viewModelScope.launch {
            repository.getBurns {
                burns.postValue(it)
            }
        }
    }

    fun getBurnsOrderedByDate(burnType: BurnType?) {
        viewModelScope.launch {
            repository.getBurns {
                var filteredBurns = it
                if (burnType != null) {
                    filteredBurns = it?.filter { burn -> burn.type == burnType }
                }
                val sortedBurns = if (isSortedDescending) {
                    filteredBurns?.sortedByDescending { burn -> burn.date }
                } else {
                    filteredBurns?.sortedBy { burn -> burn.date }
                }
                isSortedDescending = !isSortedDescending
                burns.postValue(sortedBurns)
            }
        }
    }

    fun getBurnsByType(type: BurnType) {
        viewModelScope.launch {
            repository.getBurnsByType(type) {
                burns.postValue(it)
            }
        }
    }

    fun getBurnsByUser(userId: Int) {
        viewModelScope.launch {
            repository.getBurnsByUser(userId) {
                burns.postValue(it)
            }
        }
    }

    fun getBurnsByUserOrderedByDate(userId: Int, burnType: BurnType?) {
        viewModelScope.launch {
            repository.getBurnsByUser(userId) {
                var filteredBurns = it
                if (burnType != null) {
                    filteredBurns = it?.filter { burn -> burn.type == burnType }
                }
                val sortedBurns = if (isSortedDescending) {
                    filteredBurns?.sortedByDescending { burn -> burn.date }
                } else {
                    filteredBurns?.sortedBy { burn -> burn.date }
                }
                isSortedDescending = !isSortedDescending
                burns.postValue(sortedBurns)
            }
        }
    }

    fun getBurnsByUserAndType(userId: Int, type: BurnType) {
        viewModelScope.launch {
            repository.getBurnsByUserAndType(userId, type) {
                burns.postValue(it)
            }
        }
    }

    fun createBurn(burn: Burn) {
        viewModelScope.launch {
            repository.createBurn(burn) { success, newBurn, error ->
                if (success) {
                    // Adiciona o novo burn à lista existente
                    val updatedBurns = burns.value?.toMutableList() ?: mutableListOf()
                    newBurn?.let { updatedBurns.add(it) }
                    burns.postValue(updatedBurns)
                    createBurnResult.postValue(Pair(true, null))
                } else {
                    createBurnResult.postValue(Pair(false, error))
                }
            }
        }
    }

    fun fetchLocationInfo(latitude: Double, longitude: Double) {
        RetrofitInstance.geoApiService.getLocationInfo(latitude, longitude).enqueue(object :
            Callback<LocationResponse> {
            override fun onResponse(call: Call<LocationResponse>, response: Response<LocationResponse>) {
                if (response.isSuccessful) {
                    locationInfo.postValue(response.body())
                }
            }

            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    fun getPendingBurns() {
        viewModelScope.launch {
            repository.getBurnsByState("PENDING") {
                _pendingBurns.value = it
            }
        }
    }

    fun getPendingBurnsOrderedByDate(concelho: String, burnType: BurnType?) {
        viewModelScope.launch {
            repository.getBurnsByStateAndConcelho("PENDING", concelho) { burns ->
                val filteredBurns = burnType?.let { type ->
                    burns?.filter { it.type == type }
                } ?: burns

                val sortedBurns = if (isSortedDescending) {
                    filteredBurns?.sortedByDescending { it.date }
                } else {
                    filteredBurns?.sortedBy { it.date }
                }
                isSortedDescending = !isSortedDescending
                _pendingBurns.value = sortedBurns
            }
        }
    }

    fun getPendingBurnsByType(type: BurnType) {
        viewModelScope.launch {
            repository.getBurnsByStateAndType("PENDING", type) {
                _pendingBurns.value = it
            }
        }
    }

    fun getPendingBurnsByStateAndConcelho(state: String, concelho: String) {
        viewModelScope.launch {
            repository.getBurnsByState(state) { burns ->
                _pendingBurns.value = burns?.filter { it.concelho == concelho }
            }
        }
    }

    fun getPendingBurnsByStateConcelhoAndType(state: String, concelho: String, type: BurnType) {
        viewModelScope.launch {
            repository.getBurnsByStateAndType(state, type) { burns ->
                _pendingBurns.value = burns?.filter { it.concelho == concelho }
            }
        }
    }

    fun getBurnsByConcelho(concelho: String) {
        viewModelScope.launch {
            repository.getBurnsByConcelho(concelho) {
                burns.postValue(it)
            }
        }
    }

    fun getBurnsByConcelhoOrdered(concelho: String, burnType: BurnType?) {
        viewModelScope.launch {
            repository.getBurnsByConcelho(concelho) { burns ->
                val filteredBurns = burnType?.let { type ->
                    burns?.filter { it.type == type }
                } ?: burns

                val sortedBurns = if (isSortedDescending) {
                    filteredBurns?.sortedByDescending { it.date }
                } else {
                    filteredBurns?.sortedBy { it.date }
                }
                isSortedDescending = !isSortedDescending
                this@BurnViewModel.burns.postValue(sortedBurns)
            }
        }
    }

    fun updateBurnState(burnId: Int, newState: String) = liveData(Dispatchers.IO) {
        try {
            val success = repository.updateBurnState(burnId, newState)
            emit(success)
        } catch (e: Exception) {
            emit(false)
        }
    }
}
package com.example.chamasegura.data.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chamasegura.data.entities.Municipality
import com.example.chamasegura.data.repository.MunicipalityRepository
import kotlinx.coroutines.launch

class MunicipalityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MunicipalityRepository(application.applicationContext)
    val municipalities: MutableLiveData<List<Municipality>> = MutableLiveData()

    fun getMunicipalities() {
        viewModelScope.launch {
            repository.getMunicipalities {
                municipalities.postValue(it)
            }
        }
    }

    fun createMunicipality(municipality: Municipality) {
        viewModelScope.launch {
            repository.createMunicipality(municipality) {
                // Atualize o estado conforme necessário
            }
        }
    }

    fun updateMunicipalityResponsible(municipalityId: Int, userId: Int) {
        viewModelScope.launch {
            repository.updateMunicipalityResponsible(municipalityId, userId) {
                // Handle the result as needed
            }
        }
    }

    fun getMunicipalityByResponsibleUser(userId: Int): LiveData<Municipality> {
        val result = MutableLiveData<Municipality>()
        viewModelScope.launch {
            repository.getMunicipalityByUserId(userId) {
                result.postValue(it)
            }
        }
        return result
    }
}
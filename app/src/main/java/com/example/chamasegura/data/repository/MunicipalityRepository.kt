package com.example.chamasegura.data.repository

import android.content.Context
import com.example.chamasegura.data.api.RetrofitInstance
import com.example.chamasegura.data.entities.Municipality
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MunicipalityRepository(private val context: Context) {

    private val api = RetrofitInstance.create(context)

    fun getMunicipalities(onResult: (List<Municipality>?) -> Unit) {
        api.getMunicipalities().enqueue(object : Callback<List<Municipality>> {
            override fun onResponse(call: Call<List<Municipality>>, response: Response<List<Municipality>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Municipality>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun createMunicipality(municipality: Municipality, onResult: (Municipality?) -> Unit) {
        api.createMunicipality(municipality).enqueue(object : Callback<Municipality> {
            override fun onResponse(call: Call<Municipality>, response: Response<Municipality>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<Municipality>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun updateMunicipalityResponsible(municipalityId: Int, userId: Int, onResult: (Boolean) -> Unit) {
        val updateData = mapOf("responsible" to userId)
        api.updateMunicipality(municipalityId, updateData).enqueue(object : Callback<Municipality> {
            override fun onResponse(call: Call<Municipality>, response: Response<Municipality>) {
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Municipality>, t: Throwable) {
                onResult(false)
            }
        })
    }

    fun getMunicipalityByUserId(userId: Int, onResult: (Municipality?) -> Unit) {
        api.getMunicipalityByUserId(userId).enqueue(object : Callback<Municipality> {
            override fun onResponse(call: Call<Municipality>, response: Response<Municipality>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<Municipality>, t: Throwable) {
                onResult(null)
            }
        })
    }
}
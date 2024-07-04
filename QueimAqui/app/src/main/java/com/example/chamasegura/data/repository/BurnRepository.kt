package com.example.chamasegura.data.repository

import android.content.Context
import com.example.chamasegura.data.api.RetrofitInstance
import com.example.chamasegura.data.entities.Burn
import com.example.chamasegura.data.entities.BurnType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BurnRepository(private val context: Context) {

    private val api = RetrofitInstance.create(context)

    fun getBurns(onResult: (List<Burn>?) -> Unit) {
        api.getBurns().enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun getBurnsByType(type: BurnType, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByType(type).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun getBurnsByUser(userId: Int, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByUser(userId).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun getBurnsByUserAndType(userId: Int, type: BurnType, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByUserAndType(userId, type).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun createBurn(burn: Burn, onResult: (Boolean, Burn?, String?) -> Unit) {
        api.createBurn(burn).enqueue(object : Callback<Burn> {
            override fun onResponse(call: Call<Burn>, response: Response<Burn>) {
                if (response.isSuccessful) {
                    onResult(true, response.body(), null)
                } else {
                    onResult(false, null, response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<Burn>, t: Throwable) {
                onResult(false, null, t.message)
            }
        })
    }

    fun getBurnsByState(state: String, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByState(state).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun getBurnsByStateAndType(state: String, type: BurnType, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByStateAndType(state, type).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    suspend fun updateBurnState(burnId: Int, newState: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.updateBurnState(burnId, newState).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    fun getBurnsByConcelho(concelho: String, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByConcelho(concelho).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }


    fun getBurnsByStateAndConcelho(state: String, concelho: String, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByStateAndConcelho(state, concelho).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    fun getBurnsByStateConcelhoAndType(state: String, concelho: String, type: BurnType, onResult: (List<Burn>?) -> Unit) {
        api.getBurnsByStateConcelhoAndType(state, concelho, type).enqueue(object : Callback<List<Burn>> {
            override fun onResponse(call: Call<List<Burn>>, response: Response<List<Burn>>) {
                onResult(response.body())
            }

            override fun onFailure(call: Call<List<Burn>>, t: Throwable) {
                onResult(null)
            }
        })
    }
}
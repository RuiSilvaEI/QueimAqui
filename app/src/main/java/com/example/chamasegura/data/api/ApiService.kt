package com.example.chamasegura.data.api

import com.example.chamasegura.data.entities.Burn
import com.example.chamasegura.data.entities.BurnCountResponse
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.entities.LoginResponse
import com.example.chamasegura.data.entities.Municipality
import com.example.chamasegura.data.entities.PasswordChangeRequest
import com.example.chamasegura.data.entities.PasswordChangeRequestIcnf
import com.example.chamasegura.data.entities.User
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("auth/signin")
    fun signIn(@Body credentials: Map<String, String>): Call<LoginResponse>

    @POST("auth/signup")
    fun signUp(@Body user: User): Call<LoginResponse>

    @GET("auth/user/{id}")
    fun getUser(@Path("id") id: Int): Call<User>

    @PUT("auth/updateuser/{id}")
    fun updateUser(@Path("id") id: Int, @Body user: User): Call<User>

    @PUT("auth/changepassword/{id}")
    fun changePassword(@Path("id") userId: Int, @Header("Authorization") token: String, @Body passwordChangeRequest: PasswordChangeRequest): Call<Void>

    @PUT("auth/changePasswordicnf/{id}") fun changePasswordIcnf(@Path("id") userId: Int, @Header("Authorization") token: String, @Body request: PasswordChangeRequestIcnf): Call<Void>

    @GET("auth/users")
    fun getAllUsers(): Call<List<User>>

    @PUT("auth/updatestate/{id}")
    fun updateUserState(@Path("id") id: Int, @Body requestBody: Map<String, String>): Call<User>

    @GET("burns")
    fun getBurns(): Call<List<Burn>>

    @GET("burns/type/{type}")
    fun getBurnsByType(@Path("type") type: BurnType): Call<List<Burn>>

    @GET("burns/user/{id}")
    fun getBurnsByUser(@Path("id") userId: Int): Call<List<Burn>>

    @GET("burns/count/{userId}")
    suspend fun getNumberOfBurnRequests(@Path("userId") userId: Int): BurnCountResponse

    @GET("burns/user/{userId}/type/{type}")
    fun getBurnsByUserAndType(@Path("userId") userId: Int, @Path("type") type: BurnType): Call<List<Burn>>

    @POST("burns/create")
    fun createBurn(@Body burn: Burn): Call<Burn>

    @GET("burns/state/{state}")
    fun getBurnsByState(@Path("state") state: String): Call<List<Burn>>

    @GET("burns/state/{state}/type/{type}")
    fun getBurnsByStateAndType(@Path("state") state: String, @Path("type") type: BurnType): Call<List<Burn>>

    @GET("burns/state/{state}/concelho/{concelho}")
    fun getBurnsByStateAndConcelho(@Path("state") state: String, @Path("concelho") concelho: String): Call<List<Burn>>

    @GET("burns/state/{state}/concelho/{concelho}/type/{type}")
    fun getBurnsByStateConcelhoAndType(@Path("state") state: String, @Path("concelho") concelho: String, @Path("type") type: BurnType): Call<List<Burn>>

    @PUT("burns/{id}/state/{state}")
    fun updateBurnState(@Path("id") burnId: Int, @Path("state") newState: String): Call<Void>

    @GET("burns/concelho/{concelho}")
    fun getBurnsByConcelho(@Path("concelho") concelho: String): Call<List<Burn>>

    @DELETE("burns/delete/{number}")
    fun deleteBurn(@Path("number") number: Int): Call<Void>

    @GET("municipality")
    fun getMunicipalities(): Call<List<Municipality>>

    @GET("municipality/responsible/{userId}")
    fun getMunicipalityByUserId(@Path("userId") userId: Int): Call<Municipality>

    @GET("municipality/{number}")
    fun getMunicipalityById(@Path("number") number: Int): Call<Municipality>

    @POST("municipality/create")
    fun createMunicipality(@Body municipality: Municipality): Call<Municipality>

    @PUT("municipality/update/{number}")
    fun updateMunicipality(@Path("number") number: Int, @Body updateData: Map<String, Int>): Call<Municipality>

    @DELETE("municipalities/delete/{number}")
    fun deleteMunicipality(@Path("number") number: Int): Call<Void>

    @Multipart
    @PUT("photo/updatephoto/{id}")
    fun updatePhoto(@Path("id") id: Int, @Part photo: MultipartBody.Part): Call<Void>

    @POST("contact/send-message")
    fun sendContactMessage(@Header("Authorization") token: String, @Body message: Map<String, String>): Call<Void>

    @POST("auth/forgotpassword")
    fun sendPasswordResetCode(@Body email: Map<String, String>): Call<Void>

    @POST("auth/resetpassword/verify")
    fun verifyPasswordResetCode(@Body code: Map<String, String>): Call<Void>

    @POST("auth/resetpassword")
    fun resetPassword(@Body request: Map<String, String>): Call<Void>
}

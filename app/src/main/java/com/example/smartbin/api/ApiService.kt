package com.example.smartbin.api

import androidx.lifecycle.LiveData
import com.example.smartbin.model.remote.LoginResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth")
    fun login(): LiveData<ApiResponse<LoginResponse>>

//    @GET("users/me")
//    fun getProfile(): LiveData<>
}
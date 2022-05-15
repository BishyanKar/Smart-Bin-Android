package com.example.smartbin.api

import androidx.lifecycle.LiveData
import com.example.smartbin.model.remote.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth")
    fun login(): LiveData<ApiResponse<LoginResponse>>

    @GET("users/me")
    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>>

    @GET("users/transaction")
    fun getAllTransactions(): LiveData<ApiResponse<TransactionResponse>>

    @GET("bin")
    fun getAllBins(): LiveData<ApiResponse<BinResponse>>

    @POST("users/connect")
    fun connectToNewWallet(): LiveData<ApiResponse<ProfileResponse>>

    @POST("users/connect")
    fun connectToExistingWallet(@Body phrase: Phrase): LiveData<ApiResponse<ProfileResponse>>
}
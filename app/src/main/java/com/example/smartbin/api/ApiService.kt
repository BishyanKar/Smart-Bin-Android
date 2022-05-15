package com.example.smartbin.api

import androidx.lifecycle.LiveData
import com.example.smartbin.model.remote.*
import retrofit2.http.*

interface ApiService {
    @POST("auth")
    fun login(): LiveData<ApiResponse<LoginResponse>>

    @GET("users/me")
    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>>

    @GET("users/balance")
    fun getWalletBalance(): LiveData<ApiResponse<BalanceResponse>>

    @GET("users/transaction")
    fun getAllTransactions(): LiveData<ApiResponse<TransactionResponse>>

    @GET("bin")
    fun getAllBins(@Query("lat") lat: Double, @Query("lng") lng: Double): LiveData<ApiResponse<BinResponse>>

    @POST("users/connect")
    fun connectToNewWallet(): LiveData<ApiResponse<ProfileResponse>>

    @POST("users/connect")
    fun connectToExistingWallet(@Body body: HashMap<String, Any>): LiveData<ApiResponse<ProfileResponse>>

    @POST("users/transfer")
    fun transfer()

    @PATCH("users/disconnect")
    fun disconnectWallet(): LiveData<ApiResponse<ProfileResponse>>
}
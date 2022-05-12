package com.example.smartbin.repos

import androidx.lifecycle.LiveData
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.api.ApiService
import com.example.smartbin.model.remote.BinResponse
import com.example.smartbin.model.remote.ProfileResponse
import com.example.smartbin.model.remote.TransactionResponse
import javax.inject.Inject

class ProfileRepo @Inject constructor(private val apiService: ApiService){

    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>> {
        return apiService.getProfileInfo()
    }

    fun getAllTransactions(): LiveData<ApiResponse<TransactionResponse>> {
        return apiService.getAllTransactions()
    }

    fun getAllBins(): LiveData<ApiResponse<BinResponse>> {
        return apiService.getAllBins()
    }
}
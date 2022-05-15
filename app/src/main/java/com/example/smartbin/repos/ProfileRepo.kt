package com.example.smartbin.repos

import androidx.lifecycle.LiveData
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.api.ApiService
import com.example.smartbin.model.remote.*
import javax.inject.Inject

class ProfileRepo @Inject constructor(private val apiService: ApiService){

    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>> {
        return apiService.getProfileInfo()
    }

    fun getAllTransactions(): LiveData<ApiResponse<TransactionResponse>> {
        return apiService.getAllTransactions()
    }

    fun connectToNewWallet(): LiveData<ApiResponse<ProfileResponse>> {
        return apiService.connectToNewWallet()
    }

    fun connectToExistingWallet(phrase: String): LiveData<ApiResponse<ProfileResponse>> {
        return apiService.connectToExistingWallet(Phrase(phrase))
    }

    fun getWalletBalance(): LiveData<ApiResponse<BalanceResponse>> {
        return apiService.getWalletBalance()
    }

    fun transfer() {

    }

    fun disconnectWallet(): LiveData<ApiResponse<ProfileResponse>> {
        return apiService.disconnectWallet()
    }
}
package com.example.smartbin.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.model.remote.BinResponse
import com.example.smartbin.model.remote.ProfileResponse
import com.example.smartbin.model.remote.TransactionResponse
import com.example.smartbin.repos.ProfileRepo
import com.google.android.gms.common.api.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val profileRepo: ProfileRepo): ViewModel() {

    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>> {
        return profileRepo.getProfileInfo()
    }

    fun getAllTransactions(): LiveData<ApiResponse<TransactionResponse>> {
        return profileRepo.getAllTransactions()
    }

    fun getAllBins(): LiveData<ApiResponse<BinResponse>> {
        return profileRepo.getAllBins()
    }
}
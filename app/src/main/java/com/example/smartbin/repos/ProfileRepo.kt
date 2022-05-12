package com.example.smartbin.repos

import androidx.lifecycle.LiveData
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.api.ApiService
import com.example.smartbin.model.remote.ProfileResponse
import javax.inject.Inject

class ProfileRepo @Inject constructor(private val apiService: ApiService){

    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>> {
        return apiService.getProfileInfo()
    }
}
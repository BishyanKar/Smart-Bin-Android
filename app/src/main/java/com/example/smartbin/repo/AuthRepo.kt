package com.example.smartbin.repo

import androidx.lifecycle.LiveData
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.api.ApiService
import com.example.smartbin.model.remote.LoginResponse
import javax.inject.Inject

class AuthRepo @Inject constructor(private val apiService: ApiService){

    fun login(): LiveData<ApiResponse<LoginResponse>> {
        return apiService.login()
    }
}
package com.example.smartbin.repos

import androidx.lifecycle.LiveData
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.api.ApiService
import com.example.smartbin.model.remote.BinResponse
import javax.inject.Inject

class HomeRepo @Inject constructor(private val apiService: ApiService){
    fun getAllBins(): LiveData<ApiResponse<BinResponse>> {
        return apiService.getAllBins()
    }
}
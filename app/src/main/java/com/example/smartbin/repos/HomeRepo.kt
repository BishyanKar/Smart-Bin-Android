package com.example.smartbin.repos

import androidx.lifecycle.LiveData
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.api.ApiService
import com.example.smartbin.model.remote.BinResponse
import javax.inject.Inject

class HomeRepo @Inject constructor(private val apiService: ApiService){
    fun getAllBins(lat: Double, lng: Double): LiveData<ApiResponse<BinResponse>> {
        //dummy data for test 22.5394422,88.3260267
        //dummy data for test 0,0
        //todo change to lat, lng
        return apiService.getAllBins(0.0,0.0)
    }
}
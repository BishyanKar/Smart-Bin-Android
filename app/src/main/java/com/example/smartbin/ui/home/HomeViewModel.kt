package com.example.smartbin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.model.remote.BinResponse
import com.example.smartbin.repos.HomeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val homeRepo: HomeRepo): ViewModel() {

    fun getAllBins(): LiveData<ApiResponse<BinResponse>> {
        return homeRepo.getAllBins()
    }
}
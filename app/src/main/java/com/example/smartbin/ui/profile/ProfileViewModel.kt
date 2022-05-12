package com.example.smartbin.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.model.remote.ProfileResponse
import com.example.smartbin.repos.ProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val profileRepo: ProfileRepo): ViewModel() {

    fun getProfileInfo(): LiveData<ApiResponse<ProfileResponse>> {
        return profileRepo.getProfileInfo()
    }
}
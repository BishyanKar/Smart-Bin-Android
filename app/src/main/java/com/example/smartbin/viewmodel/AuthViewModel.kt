package com.example.smartbin.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.smartbin.api.ApiResponse
import com.example.smartbin.model.remote.LoginResponse
import com.example.smartbin.repo.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepo: AuthRepo): ViewModel() {

    fun login(): LiveData<ApiResponse<LoginResponse>> {
        return authRepo.login()
    }
}
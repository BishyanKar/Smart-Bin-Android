package com.example.smartbin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.smartbin.repos.ProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeActivityViewModel @Inject constructor(val profileRepo: ProfileRepo): ViewModel() {
    var currentLatitude: Double? = null
    var currentLongitude: Double? = null
}
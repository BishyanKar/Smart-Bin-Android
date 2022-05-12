package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginResponse {
    @SerializedName("token")
    @Expose
    var token: String? = null

    @SerializedName("error")
    @Expose
    var error: Boolean = false

    @SerializedName("message")
    @Expose
    var message: String? = null
}
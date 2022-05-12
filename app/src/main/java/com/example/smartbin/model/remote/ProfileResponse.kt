package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileResponse {
    @SerializedName("err")
    @Expose
    var err: Boolean = false

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("user")
    @Expose
    var user: User? = null
}
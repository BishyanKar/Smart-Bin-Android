package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileResponse {
    @SerializedName("error")
    @Expose
    var err: Boolean = false

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("user")
    @Expose
    var user: User? = null

    @SerializedName("phrase")
    @Expose
    var phrase: String? = null
}
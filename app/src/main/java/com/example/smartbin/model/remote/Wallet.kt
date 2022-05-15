package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Wallet {
    @SerializedName("publicKey")
    @Expose
    var publicKey: String? = null

    @SerializedName("address")
    @Expose
    var address: String? = null

    @SerializedName("_id")
    @Expose
    var id: String? = null
}
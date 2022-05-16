package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class BalanceResponse {
    @SerializedName("wallet")
    @Expose
    var wallet: Wallet? = null

    @SerializedName("error")
    @Expose
    var error: Boolean = false

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("coins")
    @Expose
    var coins: Double = 0.0
}
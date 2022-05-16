package com.example.smartbin.model.remote

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

class User {
    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("email")
    @Expose
    var email: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("permissions")
    @Expose
    var permissions: List<Any>? = null

    @SerializedName("avatar")
    @Expose
    var avatar: String? = null

    @SerializedName("ourCoins")
    @Expose
    var ourCoins: Double? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("updatedAt")
    @Expose
    var updatedAt: String? = null

    @SerializedName("wallet")
    @Expose
    var wallet: Wallet? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null
}
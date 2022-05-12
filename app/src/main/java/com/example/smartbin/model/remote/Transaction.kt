package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Transaction {
    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("binId")
    @Expose
    var binId: String? = null

    @SerializedName("userId")
    @Expose
    var userId: String? = null

    @SerializedName("coins")
    @Expose
    var coins: Int? = null

    @SerializedName("wasteType")
    @Expose
    var wasteType: String? = null

    @SerializedName("height")
    @Expose
    var height: Int? = null

    @SerializedName("weight")
    @Expose
    var weight: Int? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("updatedAt")
    @Expose
    var updatedAt: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null
}
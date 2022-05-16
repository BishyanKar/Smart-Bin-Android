package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Bin {
    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("location")
    @Expose
    var location: Location? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("filled")
    @Expose
    var filled: Double? = null

    @SerializedName("height")
    @Expose
    var height: Double? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("updatedAt")
    @Expose
    var updatedAt: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

    @SerializedName("wet")
    @Expose
    var wet: Int? = null

    @SerializedName("dry")
    @Expose
    var dry: Int? = null

    @SerializedName("transactionId")
    @Expose
    var transactionId: String? = null

    var relativeDistance: Long = 0
}
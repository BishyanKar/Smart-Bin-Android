package com.example.smartbin.model.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BinResponse {
    @SerializedName("bins")
    @Expose
    var bins: List<Bin>? = null
}
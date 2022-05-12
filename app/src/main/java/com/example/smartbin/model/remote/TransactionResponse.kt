package com.example.smartbin.model.remote

import android.view.SurfaceControl.Transaction
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TransactionResponse {
    @SerializedName("transactions")
    @Expose
    var transactions: List<Transaction>? = null
}
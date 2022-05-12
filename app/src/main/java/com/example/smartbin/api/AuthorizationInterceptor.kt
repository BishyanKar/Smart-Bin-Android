package com.example.smartbin.api

import android.content.Context
import com.example.smartbin.Constants
import com.example.smartbin.R
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(val context: Context): Interceptor {
    private val sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", sharedPreferences.getString(Constants.KEY_AUTH_TOKEN,null) ?: sharedPreferences.getString(Constants.KEY_ID_TOKEN,"")!!)
            .build()

        return chain.proceed(request)
    }
}
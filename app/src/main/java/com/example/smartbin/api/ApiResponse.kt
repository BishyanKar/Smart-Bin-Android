package com.example.smartbin.api

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.collection.ArrayMap
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.lang.NumberFormatException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class ApiResponse<T> {
    val code: Int

    @Nullable
    val body: T?

    @Nullable
    val errorMessage: String?
    val links: MutableMap<String, String>

    constructor(error: Throwable) {
        code = 500
        body = null
        errorMessage = error.message
        links = Collections.emptyMap()
    }

    constructor(response: Response<T>) {
        code = response.code()
        if (response.isSuccessful) {
            body = response.body()
            errorMessage = null
        } else {
            var message: String? = null
            if (response.errorBody() != null) {
                try {
                    message = response.errorBody()!!.string()
                } catch (ignored: IOException) {
                    Timber.e(ignored, "error while parsing response")
                }
            }
            if (message == null || message.trim { it <= ' ' }.isEmpty()) {
                message = response.message()
            }
            errorMessage = message
            body = null
        }
        val linkHeader: String? = response.headers()["link"]
        if (linkHeader == null) {
            links = Collections.emptyMap()
        } else {
            links = ArrayMap()
            val matcher: Matcher = LINK_PATTERN.matcher(linkHeader)
            while (matcher.find()) {
                val count: Int = matcher.groupCount()
                if (count == 2) {
                    links[matcher.group(2)] = matcher.group(1)
                }
            }
        }
    }

    val isSuccessful: Boolean
        get() = code in 200..299

    companion object {
        private val LINK_PATTERN: Pattern = Pattern
            .compile("<([^>]*)>[\\s]*;[\\s]*rel=\"([a-zA-Z0-9]+)\"")
        private val PAGE_PATTERN: Pattern = Pattern.compile("page=(\\d)+")
        private const val NEXT_LINK = "next"
    }
}
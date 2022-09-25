package com.github.niqdev.mjpeg

import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.Authenticator
import java.net.CookieManager
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.PasswordAuthentication
import java.net.URL

class MjpegK(private val type: MjpegKType = MjpegKType.DEFAULT) {

    private val msCookieManager by lazy { CookieManager() }

    private var sendConnectionCloseHeader = false

    /**
     * Configure authentication.
     *
     * @param username credential
     * @param password credential
     * @return Mjpeg instance
     */
    fun credential(username: String?, password: String?): MjpegK {
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password.toCharArray())
                }
            })
        }
        return this
    }

    /**
     * Configure cookies.
     *
     * @param cookie cookie string
     * @return Mjpeg instance
     */
    fun addCookie(cookie: String?): MjpegK {
        if (!cookie.isNullOrBlank()) {
            msCookieManager.cookieStore.add(null, HttpCookie.parse(cookie)[0])
        }
        return this
    }

    /**
     * Send a "Connection: close" header to fix
     * `java.net.ProtocolException: Unexpected status line`
     *
     * @return Observable Mjpeg stream
     */
    fun sendConnectionCloseHeader(): MjpegK {
        sendConnectionCloseHeader = true
        return this
    }

    private fun connect(url: String): Flow<MjpegInputStream> {
        try {
            val urlConnection = URL(url).openConnection() as HttpURLConnection
            loadConnectionProperties(urlConnection)
            val inputStream = urlConnection.inputStream
            return flow {
                when (type) {
                    MjpegKType.DEFAULT -> emit(MjpegInputStreamDefault(inputStream))
                    MjpegKType.NATIVE -> emit(MjpegInputStreamNative(inputStream))
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "error during connection", e)
            throw IllegalStateException(e)
        }
    }

    /**
     * Connect to a Mjpeg stream.
     *
     * @param url source
     * @return Observable Mjpeg stream
     */
    fun open(url: String, timeout: Long = 5L): Flow<MjpegInputStream> = flow {
        withTimeoutOrNull(timeout) {
            connect(url).collect {
                emit(it)
            }
        } ?: throw IllegalStateException()
    }

    /**
     * Configure request properties
     *
     * @param urlConnection the url connection to add properties and cookies to
     */
    private fun loadConnectionProperties(urlConnection: HttpURLConnection) {
        urlConnection.setRequestProperty("Cache-Control", "no-cache")
        if (sendConnectionCloseHeader) {
            urlConnection.setRequestProperty("Connection", "close")
        }
        if (msCookieManager.cookieStore.cookies.isNotEmpty()) {
            urlConnection.setRequestProperty(
                "Cookie",
                TextUtils.join(";", msCookieManager.cookieStore.cookies)
            )
        }
    }

    private companion object PrivateValues {
        val TAG: String = MjpegK::class.java.simpleName
    }

}

/**
 * Library implementation type
 */
enum class MjpegKType {
    DEFAULT, NATIVE
}
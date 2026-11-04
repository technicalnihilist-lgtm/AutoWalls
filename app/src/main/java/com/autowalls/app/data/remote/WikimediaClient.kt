package com.autowalls.app.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Builds the Commons client.
 *
 * Wikimedia's API etiquette requires an identifiable User-Agent; requests without
 * one are throttled. A short retry loop absorbs transient 429/5xx responses.
 */
object WikimediaClient {

    private const val USER_AGENT =
        "AutoWalls/1.0 (Android wallpaper app; https://commons.wikimedia.org/; contact: nihaonihao54059@gmail.com)"

    fun createApi(): WikimediaApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(RetryInterceptor(maxAttempts = 3))
            .build()

        return Retrofit.Builder()
            .baseUrl(WikimediaApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikimediaApi::class.java)
    }

    private class UserAgentInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()
            return chain.proceed(request)
        }
    }

    private class RetryInterceptor(private val maxAttempts: Int) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var attempt = 0
            var lastError: IOException? = null

            while (attempt < maxAttempts) {
                attempt++
                try {
                    val response = chain.proceed(request)
                    val retryable = response.code == 429 || response.code >= 500
                    if (retryable && attempt < maxAttempts) {
                        response.close()
                        Thread.sleep(400L * attempt)
                        continue
                    }
                    return response
                } catch (io: IOException) {
                    lastError = io
                    if (attempt >= maxAttempts) break
                    Thread.sleep(400L * attempt)
                }
            }
            throw lastError ?: IOException("Request failed after $maxAttempts attempts")
        }
    }
}

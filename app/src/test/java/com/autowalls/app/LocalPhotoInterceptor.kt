package com.autowalls.app

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import java.io.File

/**
 * Serves `photo://carN.jpg` from the bundled test photographs instead of the network,
 * so the screenshot tests render the real screens with real photography, offline.
 */
class LocalPhotoInterceptor(private val dir: File) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data
        if (data is String && data.startsWith(PREFIX)) {
            val file = File(dir, data.removePrefix(PREFIX))
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    return SuccessResult(
                        drawable = BitmapDrawable(chain.request.context.resources, bitmap),
                        request = chain.request,
                        dataSource = DataSource.DISK
                    )
                }
            }
        }
        return chain.proceed(chain.request)
    }

    companion object {
        const val PREFIX = "photo://"
        fun url(name: String) = PREFIX + name
    }
}

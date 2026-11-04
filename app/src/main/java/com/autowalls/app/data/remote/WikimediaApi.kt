package com.autowalls.app.data.remote

import com.autowalls.app.data.remote.dto.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Wikimedia Commons action API — https://commons.wikimedia.org/w/api.php
 */
interface WikimediaApi {

    @GET("w/api.php")
    suspend fun searchImages(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: String = "2",
        @Query("generator") generator: String = "search",
        @Query("gsrsearch") search: String,
        @Query("gsrnamespace") namespace: String = "6",
        @Query("gsrlimit") limit: Int = 30,
        @Query("gsroffset") offset: Int? = null,
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") iiProp: String = "url|size|mime|extmetadata|user",
        @Query("iiurlwidth") thumbWidth: Int = 1200,
        @Query("iiextmetadatafilter") extMetadataFilter: String = EXT_METADATA_FILTER,
        @Query("maxlag") maxLag: Int = 5
    ): SearchResponse

    @GET("w/api.php")
    suspend fun imageByPageId(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("formatversion") formatVersion: String = "2",
        @Query("pageids") pageIds: Long,
        @Query("prop") prop: String = "imageinfo",
        @Query("iiprop") iiProp: String = "url|size|mime|extmetadata|user",
        @Query("iiurlwidth") thumbWidth: Int = 1200,
        @Query("iiextmetadatafilter") extMetadataFilter: String = EXT_METADATA_FILTER
    ): SearchResponse

    companion object {
        const val BASE_URL = "https://commons.wikimedia.org/"

        /** Every extmetadata key the app reads; narrow to keep responses small. */
        const val EXT_METADATA_FILTER =
            "ObjectName|ImageDescription|Artist|Credit|LicenseShortName|LicenseUrl|" +
                "UsageTerms|AttributionRequired|DateTimeOriginal"
    }
}

package com.autowalls.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Response of `action=query&generator=search&prop=imageinfo` with formatversion=2. */
data class SearchResponse(
    @SerializedName("batchcomplete") val batchComplete: Boolean? = null,
    @SerializedName("continue") val continueToken: ContinueToken? = null,
    val query: QueryResult? = null,
    val error: ApiError? = null
)

data class ContinueToken(
    @SerializedName("gsroffset") val gsrOffset: Int? = null,
    @SerializedName("continue") val continueValue: String? = null
)

data class QueryResult(
    val pages: List<PageDto>? = null
)

data class PageDto(
    val pageid: Long = 0,
    val ns: Int = 0,
    val title: String = "",
    val index: Int = 0,
    val imageinfo: List<ImageInfoDto>? = null
)

data class ImageInfoDto(
    val url: String? = null,
    val descriptionurl: String? = null,
    val descriptionshorturl: String? = null,
    val thumburl: String? = null,
    val thumbwidth: Int = 0,
    val thumbheight: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val size: Long = 0,
    val mime: String? = null,
    val user: String? = null,
    val extmetadata: Map<String, MetaValue>? = null
)

data class MetaValue(
    val value: String? = null,
    val source: String? = null
)

data class ApiError(
    val code: String? = null,
    val info: String? = null
)

package com.autowalls.app.data.remote

import android.text.Html
import com.autowalls.app.data.remote.dto.MetaValue
import com.autowalls.app.data.remote.dto.PageDto
import com.autowalls.app.data.remote.dto.SearchResponse
import com.autowalls.app.domain.LicensePolicy
import com.autowalls.app.domain.model.VehicleCategory
import com.autowalls.app.domain.model.Wallpaper

/**
 * Turns raw Commons payloads into [Wallpaper] models.
 *
 * Anything that fails the license gate, is not a real bitmap, or is too small to
 * look good on a phone is dropped — never shown with placeholder credits.
 */
object WikimediaMapper {

    private const val MIN_WIDTH = 800
    private const val MIN_BYTES = 40_000L
    private const val MAX_DESCRIPTION = 420

    private val TAG_REGEX = Regex("<[^>]*>")
    private val WHITESPACE = Regex("\\s+")
    private val THUMB_WIDTH_REGEX = Regex("""/(\d+)px-""")

    fun mapAll(response: SearchResponse, category: VehicleCategory): List<Wallpaper> =
        response.query?.pages.orEmpty().mapNotNull { mapPage(it, category) }

    fun mapPage(page: PageDto, category: VehicleCategory): Wallpaper? {
        val info = page.imageinfo?.firstOrNull() ?: return null
        val ext = info.extmetadata.orEmpty()

        val mime = info.mime.orEmpty()
        if (!mime.startsWith("image/jpeg") && !mime.startsWith("image/png")) return null
        if (info.width < MIN_WIDTH || info.height <= 0) return null
        if (info.size in 1 until MIN_BYTES) return null

        val fullUrl = info.url?.takeIf { it.isNotBlank() } ?: return null
        val previewUrl = info.thumburl?.takeIf { it.isNotBlank() } ?: fullUrl
        val sourcePage = info.descriptionurl?.takeIf { it.isNotBlank() } ?: return null

        val licenseName = ext["LicenseShortName"].clean()
        if (!LicensePolicy.isAllowed(licenseName)) return null

        val licenseUrl = ext["LicenseUrl"].clean()
            .ifBlank { LicensePolicy.fallbackUrl(licenseName) }

        val artist = ext["Artist"].clean()
            .ifBlank { ext["Credit"].clean() }
            .ifBlank { info.user.orEmpty() }
            .ifBlank { "Unknown author" }

        val title = page.title.ifBlank { ext["ObjectName"].clean() }
        if (title.isBlank()) return null

        val displayName = title
            .removePrefix("File:")
            .substringBeforeLast('.')
            .replace('_', ' ')
            .trim()
        if (displayName.isBlank()) return null

        val description = ext["ImageDescription"].clean().let {
            if (it.length > MAX_DESCRIPTION) it.take(MAX_DESCRIPTION).trimEnd() + "…" else it
        }

        return Wallpaper(
            id = page.pageid,
            title = title,
            displayName = displayName,
            fullUrl = fullUrl,
            previewUrl = previewUrl,
            thumbUrl = smallThumb(previewUrl),
            width = info.width,
            height = info.height,
            mime = mime,
            fileSizeBytes = info.size,
            artist = artist,
            credit = ext["Credit"].clean(),
            description = description,
            licenseName = licenseName,
            licenseUrl = licenseUrl,
            sourcePage = sourcePage,
            uploadedBy = info.user.orEmpty(),
            dateOriginal = ext["DateTimeOriginal"].clean(),
            categoryId = category.id,
            categoryName = category.name
        )
    }

    /**
     * Commons thumbnail URLs follow `/thumb/<a>/<ab>/<file>/<N>px-<file>`.
     * Asking the CDN for a 640px render keeps grids light without a second API call.
     */
    private fun smallThumb(previewUrl: String): String {
        val match = THUMB_WIDTH_REGEX.find(previewUrl) ?: return previewUrl
        val current = match.groupValues[1].toIntOrNull() ?: return previewUrl
        if (current <= 640) return previewUrl
        return previewUrl.replaceRange(match.range, "/640px-")
    }

    private fun MetaValue?.clean(): String {
        val raw = this?.value ?: return ""
        val withoutTags = TAG_REGEX.replace(raw, " ")
        val decoded = runCatching {
            Html.fromHtml(withoutTags, Html.FROM_HTML_MODE_LEGACY).toString()
        }.getOrElse { withoutTags }
        return decoded
            .replace('\u00A0', ' ')
            .replace(WHITESPACE, " ")
            .trim()
    }
}

package com.autowalls.app.domain.model

/**
 * A wallpaper backed by a real file on Wikimedia Commons.
 *
 * Every field comes from the Commons `imageinfo` + `extmetadata` payload.
 * Nothing is hardcoded: if a value is missing on Commons the field is empty and
 * the UI degrades gracefully.
 */
data class Wallpaper(
    val id: Long,
    /** Original Commons file title, e.g. "File:Lamborghini Aventador S.jpg". */
    val title: String,
    /** Human friendly name without the "File:" prefix and extension. */
    val displayName: String,
    /** Full resolution original file. */
    val fullUrl: String,
    /** Commons thumbnail (1200px) used for detail previews. */
    val previewUrl: String,
    /** Smaller derived thumbnail used for grid cells. */
    val thumbUrl: String,
    val width: Int,
    val height: Int,
    val mime: String,
    val fileSizeBytes: Long,
    /** Photographer / creator, HTML stripped. */
    val artist: String,
    /** Credit line from the Commons description page, HTML stripped. */
    val credit: String,
    val description: String,
    val licenseName: String,
    val licenseUrl: String,
    /** commons.wikimedia.org/wiki/File:... page. */
    val sourcePage: String,
    val uploadedBy: String,
    val dateOriginal: String,
    val categoryId: String,
    val categoryName: String
) {
    val resolution: String get() = "$width × $height"

    val megapixels: String
        get() {
            val mp = (width.toLong() * height.toLong()) / 1_000_000.0
            return if (mp >= 1.0) String.format("%.1f MP", mp) else "< 1 MP"
        }

    val aspectRatio: Float get() = if (height == 0) 1f else width.toFloat() / height.toFloat()

    /** Attribution text used when sharing and copying credits. */
    fun attributionText(): String = buildString {
        append("\"$displayName\"")
        if (artist.isNotBlank()) append(" by $artist")
        append("\nLicense: ").append(licenseName)
        if (licenseUrl.isNotBlank()) append(" (").append(licenseUrl).append(")")
        append("\nSource: ").append(sourcePage)
        append("\n\nShared from AutoWalls. AutoWalls does not own this photograph; ")
        append("all rights remain with the creator named above.")
    }
}

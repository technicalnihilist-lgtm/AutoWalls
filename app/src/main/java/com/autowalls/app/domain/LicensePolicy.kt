package com.autowalls.app.domain

/**
 * Only free licenses that permit commercial redistribution are accepted.
 *
 * Accepted: CC0, Public Domain / PD-*, CC BY, CC BY-SA.
 * Rejected: a missing license, CC BY-NC*, CC BY-ND*, "fair use", "non-free",
 * or an unparseable string. AutoWalls ships commercially, so non-commercial and
 * no-derivatives licenses must never appear in the app.
 */
object LicensePolicy {

    private val allowedPrefixes = listOf("cc0", "cc by", "cc-by", "public domain", "pd-", "pd ")

    private val blockedMarkers = listOf(
        "-nc", " nc", "nc-", "noncommercial", "non-commercial",
        "-nd", " nd", "nd-", "noderiv", "no-deriv",
        "fair use", "non-free", "nonfree", "all rights reserved", "copyright"
    )

    fun isAllowed(licenseName: String?): Boolean {
        val raw = licenseName?.trim().orEmpty()
        if (raw.isEmpty()) return false
        val n = raw.lowercase()
        if (blockedMarkers.any { n.contains(it) }) return false
        if (n.startsWith("pd") && n.length > 2 && !n.startsWith("pd-") && !n.startsWith("pd ")) {
            return false
        }
        return allowedPrefixes.any { n.startsWith(it) }
    }

    /** Commons license deed for a short name, used when the API omits LicenseUrl. */
    fun fallbackUrl(licenseName: String): String {
        val n = licenseName.lowercase().trim()
        return when {
            n.startsWith("cc0") -> "https://creativecommons.org/publicdomain/zero/1.0/"
            n.startsWith("public domain") || n.startsWith("pd") ->
                "https://commons.wikimedia.org/wiki/Commons:Copyright_tags#Public_domain"
            n.contains("by-sa") || n.contains("by sa") -> {
                val v = Regex("""(\d\.\d)""").find(n)?.value ?: "4.0"
                "https://creativecommons.org/licenses/by-sa/$v/"
            }
            n.contains("by") -> {
                val v = Regex("""(\d\.\d)""").find(n)?.value ?: "4.0"
                "https://creativecommons.org/licenses/by/$v/"
            }
            else -> "https://commons.wikimedia.org/wiki/Commons:Licensing"
        }
    }

    /** Short human-readable summary shown on the credits page. */
    fun summary(licenseName: String): String {
        val n = licenseName.lowercase()
        return when {
            n.startsWith("cc0") ->
                "Released into the public domain. No attribution legally required; AutoWalls credits the creator anyway."
            n.startsWith("public domain") || n.startsWith("pd") ->
                "Public domain. Free to use and redistribute."
            n.contains("by-sa") ->
                "Attribution required. Derivatives must be shared under the same license."
            n.contains("by") ->
                "Attribution required. Redistribution permitted with credit."
            else -> "Free license. See the license page for the exact terms."
        }
    }
}

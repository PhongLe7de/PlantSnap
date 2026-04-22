package com.plantsnap.ui.util

/** Fallback image URL shown when a plant/habitat image URL from the AI is null or invalid. */
const val FALLBACK_IMAGE_URL = "https://picsum.photos/seed/plant/600/400"

/**
 * We ask Gemini for Wikimedia Commons URLs. Accept only URLs from that CDN
 * to filter out hallucinated / irrelevant URLs. Everything else → fall back.
 */
private val WIKIMEDIA_URL_REGEX =
    Regex("^https://upload\\.wikimedia\\.org/.+", RegexOption.IGNORE_CASE)

/**
 * Returns the trimmed URL if it points to Wikimedia Commons (`upload.wikimedia.org`),
 * otherwise null. Use with the `?:` operator to fall back to [FALLBACK_IMAGE_URL].
 */
fun String?.validImageUrlOrNull(): String? {
    if (this.isNullOrBlank()) return null
    val trimmed = trim()
    return if (WIKIMEDIA_URL_REGEX.matches(trimmed)) trimmed else null
}

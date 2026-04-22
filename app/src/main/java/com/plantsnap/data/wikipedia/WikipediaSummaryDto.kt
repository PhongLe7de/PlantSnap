package com.plantsnap.data.wikipedia

import kotlinx.serialization.Serializable

@Serializable
data class WikipediaSummaryDto(
    val thumbnail: WikipediaImageDto? = null,
    val originalimage: WikipediaImageDto? = null,
)

@Serializable
data class WikipediaImageDto(
    val source: String? = null,
)

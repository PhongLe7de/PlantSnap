package com.plantsnap.ui.screens.profile.model

enum class PlantRank(
    val displayName: String,
    val emoji: String,
    val minScans: Int,
    val maxScans: Int // exclusive; Int.MAX_VALUE for top rank
) {
    SEEDLING("Seedling", "\uD83C\uDF31", 0, 5),
    SPROUT("Sprout", "\uD83C\uDF3F", 5, 15),
    PLANT_LOVER("Plant Lover", "\uD83C\uDF3B", 15, 30),
    BOTANIST("Botanist", "\uD83C\uDF3E", 30, 50),
    MASTER_GARDENER("Master Gardener", "\uD83C\uDF33", 50, Int.MAX_VALUE);

    companion object {
        fun fromScanCount(count: Int): PlantRank =
            entries.last { count >= it.minScans }

        fun progressToNext(count: Int): Float {
            val rank = fromScanCount(count)
            if (rank == MASTER_GARDENER) return 1f
            val range = rank.maxScans - rank.minScans
            return ((count - rank.minScans).toFloat() / range).coerceIn(0f, 1f)
        }

        fun scansToNextRank(count: Int): Int {
            val rank = fromScanCount(count)
            if (rank == MASTER_GARDENER) return 0
            return rank.maxScans - count
        }
    }
}

package com.plantsnap.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.plantsnap.domain.models.ScanResult

// 1-N relationship between ScanEntity and CandidateEntity
data class ScanWithCandidates(
    @Embedded val scan: ScanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scanId"
    )
    val candidates: List<CandidateEntity>
)

fun ScanWithCandidates.toDomain() = ScanResult(
    id = scan.id,
    imagePath = scan.imagePath,
    organ = scan.organ,
    bestMatch = scan.bestMatch,
    candidates = candidates.map { it.toDomain() },
    aiInfo = scan.aiInfo,
    timestamp = scan.timestamp,
    synced = scan.synced
)

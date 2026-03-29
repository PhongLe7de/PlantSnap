package com.plantsnap.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

// 1-N relationship between ScanEntity and CandidateEntity
data class ScanWithCandidates(
    @Embedded val scan: ScanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scanId"
    )
    val candidates: List<CandidateEntity>
)

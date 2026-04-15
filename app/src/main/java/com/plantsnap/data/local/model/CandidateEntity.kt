package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.plantsnap.domain.models.Candidate

// Cascade delete candidates too
@Entity(
    tableName = "candidates",
    foreignKeys = [ForeignKey(
        entity = ScanEntity::class,
        parentColumns = ["id"],
        childColumns = ["scanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("scanId")]
)
data class CandidateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanId: String,
    val scientificName: String,
    val commonNames: String,    // comma-joined, e.g. "Rose,Dog rose"
    val family: String,
    val score: Float,
    val iucnCategory: String?,
    val imageUrl: String?,
    val aiInfo: String?,
)

fun Candidate.toEntity(scanId: String) = CandidateEntity(
    scanId = scanId,
    scientificName = scientificName,
    commonNames = commonNames.joinToString(","),
    family = family,
    score = score,
    iucnCategory = iucnCategory,
    imageUrl = imageUrl,
    aiInfo = aiInfo,
)

fun CandidateEntity.toDomain() = Candidate(
    scientificName = scientificName,
    commonNames = commonNames.split(",").filter { it.isNotEmpty() },
    family = family,
    score = score,
    iucnCategory = iucnCategory,
    imageUrl = imageUrl,
    aiInfo = aiInfo,
)

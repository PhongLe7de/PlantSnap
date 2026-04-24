package com.plantsnap.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.plantsnap.domain.models.Candidate
import com.plantsnap.domain.models.SavedPlant
import kotlinx.serialization.json.Json

@Entity(
    tableName = "saved_plants",
    foreignKeys = [ForeignKey(
        entity = ScanEntity::class,
        parentColumns = ["id"],
        childColumns = ["sourceScanId"],
        onDelete = ForeignKey.SET_NULL,
    )],
    indices = [
        Index("sourceScanId"),
        Index(value = ["sourceScanId", "scientificName"], unique = true),
    ],
)
data class SavedPlantEntity(
    @PrimaryKey val id: String,
    val candidateJson: String,
    val scientificName: String,
    val sourceScanId: String?,
    val savedAt: Long,
)

fun SavedPlantEntity.toDomain(json: Json): SavedPlant = SavedPlant(
    id = id,
    plant = json.decodeFromString(Candidate.serializer(), candidateJson),
    sourceScanId = sourceScanId,
    savedAt = savedAt,
)

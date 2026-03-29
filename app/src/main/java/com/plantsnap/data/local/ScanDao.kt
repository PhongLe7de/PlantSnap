package com.plantsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.plantsnap.data.local.model.CandidateEntity
import com.plantsnap.data.local.model.ScanEntity
import com.plantsnap.data.local.model.ScanWithCandidates
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    // Reads
    @Transaction
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun getScansWithCandidates(): Flow<List<ScanWithCandidates>>

    @Transaction
    @Query("SELECT * FROM scans WHERE id = :scanId")
    fun observeScanById(scanId: String): Flow<ScanWithCandidates?>

    // Scans not synced to Supabase
    @Transaction
    @Query("SELECT * FROM scans WHERE synced = 0")
    suspend fun getUnsyncedScans(): List<ScanWithCandidates>

    // Writes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<CandidateEntity>)

    @Query("UPDATE scans SET aiInfo = :aiInfo WHERE id = :scanId")
    suspend fun updateAiInfo(scanId: String, aiInfo: String)

    @Query("UPDATE scans SET synced = 1 WHERE id = :scanId")
    suspend fun markSynced(scanId: String)

    @Query("DELETE FROM scans WHERE id = :scanId")
    suspend fun deleteScan(scanId: String)
}

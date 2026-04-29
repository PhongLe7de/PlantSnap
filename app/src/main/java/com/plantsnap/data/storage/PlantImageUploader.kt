package com.plantsnap.data.storage

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads the original captured scan image to the `plant_images` Supabase Storage
 * bucket at `{userId}/{scanId}.jpg`. Returns the bucket path (not a URL) so callers
 * can persist it; signed URLs are produced lazily by [PlantImageUrlResolver] at
 * display time.
 */
@Singleton
class PlantImageUploader @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private companion object {
        const val TAG = "PlantImageUploader"
        const val BUCKET = "plant_images"
    }

    /** Returns `{userId}/{scanId}.jpg` on success, null otherwise. */
    suspend fun uploadScanImage(localImagePath: String, scanId: String): String? {
        val userId = supabase.auth.currentUserOrNull()?.id ?: run {
            Log.d(TAG, "uploadScanImage: not authenticated, skipping")
            return null
        }
        return withContext(Dispatchers.IO) {
            val file = File(localImagePath)
            if (!file.exists()) {
                Log.w(TAG, "uploadScanImage: source file missing at $localImagePath")
                return@withContext null
            }
            val bytes = try {
                file.readBytes()
            } catch (e: Exception) {
                Log.w(TAG, "uploadScanImage: failed to read $localImagePath", e)
                return@withContext null
            }
            val path = "$userId/$scanId.jpg"
            try {
                supabase.storage.from(BUCKET).upload(path, bytes) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }
                path
            } catch (e: Exception) {
                Log.w(TAG, "uploadScanImage: upload failed for $path", e)
                null
            }
        }
    }
}

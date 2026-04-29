package com.plantsnap.data.storage

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Turns a stored `image_url` value into something Coil can fetch.
 *
 * The same column holds two kinds of strings:
 * - **Bucket paths** like `{userId}/{scanId}.jpg` — uploaded by [PlantImageUploader],
 *   need a signed URL.
 * - **Absolute URLs** (e.g. PlantNet reference images) — pass through unchanged.
 *
 * Disambiguation: parse with [URI] and inspect the scheme. A null/blank scheme means
 * the value is a relative path → sign it. Otherwise, return as-is.
 */
@Singleton
class PlantImageUrlResolver @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private companion object {
        const val TAG = "PlantImageUrlResolver"
        const val BUCKET = "plant_images"
        val DEFAULT_TTL: Duration = 1.hours
    }

    suspend fun resolve(value: String?): String? {
        if (value.isNullOrBlank()) return null
        if (isAbsoluteUrl(value)) return value
        if (supabase.auth.currentUserOrNull() == null) return null
        return runCatching {
            supabase.storage.from(BUCKET).createSignedUrl(value, expiresIn = DEFAULT_TTL)
        }.onFailure { Log.w(TAG, "createSignedUrl failed for $value", it) }.getOrNull()
    }

    /**
     * Resolves several values in one go: absolute URLs map to themselves; bucket paths
     * are signed in parallel. Each successful input appears in the returned map keyed
     * by its original value. Inputs that fail signing are absent — callers should
     * treat absence as "no displayable image".
     */
    suspend fun resolveAll(values: Collection<String?>): Map<String, String> {
        val distinct = values.asSequence()
            .filterNotNull()
            .filter { it.isNotBlank() }
            .toSet()
        if (distinct.isEmpty()) return emptyMap()

        val (urls, paths) = distinct.partition { isAbsoluteUrl(it) }
        val passthrough = urls.associateWith { it }
        if (paths.isEmpty()) return passthrough
        if (supabase.auth.currentUserOrNull() == null) return passthrough

        val signed = coroutineScope {
            paths.map { path ->
                async {
                    runCatching {
                        path to supabase.storage.from(BUCKET)
                            .createSignedUrl(path, expiresIn = DEFAULT_TTL)
                    }.onFailure {
                        Log.w(TAG, "createSignedUrl failed for $path", it)
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull().toMap()
        }
        return passthrough + signed
    }

    private fun isAbsoluteUrl(value: String): Boolean {
        val scheme = runCatching { URI(value).scheme }.getOrNull()
        return !scheme.isNullOrBlank()
    }
}

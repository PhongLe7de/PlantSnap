package com.plantsnap.ui.screens.identify.camera

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.plantsnap.utils.MAX_PHOTOS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapturedPhotosHolder @Inject constructor() {
    private val _photos = MutableStateFlow<List<Uri>>(emptyList())
    val photos: StateFlow<List<Uri>> = _photos.asStateFlow()

    /** 1:1 mapping of photo URI to its organ type (e.g. "leaf", "flower"). Defaults to "auto". */
    private val _organByPhoto = MutableStateFlow<Map<Uri, String>>(emptyMap())
    val organByPhoto: StateFlow<Map<Uri, String>> = _organByPhoto.asStateFlow()

    val count: Int get() = _photos.value.size

    fun addPhoto(uri: Uri, organ: String = "auto") {
        _photos.update { current ->
            if (current.size < MAX_PHOTOS) current + uri else current
        }
        _organByPhoto.update { it + (uri to organ) }
    }

    fun removePhoto(index: Int) {
        var removed: Uri? = null
        _photos.update { current ->
            if (index in current.indices) {
                removed = current[index]
                current.toMutableList().apply { removeAt(index) }
            } else current
        }
        removed?.let { uri -> _organByPhoto.update { it - uri } }
    }

    fun setOrganForPhoto(uri: Uri, organ: String) {
        _organByPhoto.update { current ->
            if (uri in current) current + (uri to organ) else current
        }
    }

    fun clear() {
        _photos.update { emptyList() }
        _organByPhoto.update { emptyMap() }
    }
}

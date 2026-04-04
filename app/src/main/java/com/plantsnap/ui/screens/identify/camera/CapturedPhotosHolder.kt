package com.plantsnap.ui.screens.identify.camera

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        if (_photos.value.size < 5) {
            _photos.value = _photos.value + uri
            _organByPhoto.value = _organByPhoto.value + (uri to organ)
        }
    }

    fun removePhoto(index: Int) {
        val current = _photos.value
        if (index in current.indices) {
            val removed = current[index]
            _photos.value = current.toMutableList().apply { removeAt(index) }
            _organByPhoto.value = _organByPhoto.value - removed
        }
    }

    fun setOrganForPhoto(uri: Uri, organ: String) {
        if (uri in _organByPhoto.value) {
            _organByPhoto.value = _organByPhoto.value + (uri to organ)
        }
    }

    fun clear() {
        _photos.value = emptyList()
        _organByPhoto.value = emptyMap()
    }
}

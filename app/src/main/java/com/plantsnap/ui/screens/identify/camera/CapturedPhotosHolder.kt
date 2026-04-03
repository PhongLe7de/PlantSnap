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

    val count: Int get() = _photos.value.size

    fun addPhoto(uri: Uri) {
        if (_photos.value.size < 5) {
            _photos.value = _photos.value + uri
        }
    }

    fun removePhoto(index: Int) {
        val current = _photos.value
        if (index in current.indices) {
            _photos.value = current.toMutableList().apply { removeAt(index) }
        }
    }

    fun clear() {
        _photos.value = emptyList()
    }
}

package com.plantsnap.ui.screens.identify.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Uri>>(UiState.Idle)
    val uiState: StateFlow<UiState<Uri>> = _uiState.asStateFlow()

    fun setImageUri(uri: Uri) {
        _uiState.value = UiState.Success(uri)
        //TODO: Implement any additional logic needed when the image URI is set, e.g. validation or preprocessing
    }
}

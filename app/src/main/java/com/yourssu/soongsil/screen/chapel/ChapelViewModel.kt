package com.yourssu.soongsil.screen.chapel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed interface ChapelUiState {
    data object Loading : ChapelUiState
    data object Chapel : ChapelUiState
}

@HiltViewModel
class ChapelViewModel @Inject constructor() : ViewModel() {

}
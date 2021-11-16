package com.example.petsmatchingapp.model

import android.net.Uri

data class SelectedUri(
    val uri: Uri,
    var selected: Boolean = false
)

package com.example.smartcutapp.presentation.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/** Съёмка на камеру телефона и выбор из галереи; результат приходит в [onPhoto] как Uri. */
class PhotoPicker internal constructor(
    private val takePhoto: () -> Unit,
    private val pickFromGallery: () -> Unit
) {
    fun camera() = takePhoto()
    fun gallery() = pickFromGallery()
}

@Composable
fun rememberPhotoPicker(onPhoto: (Uri) -> Unit): PhotoPicker {
    val context = LocalContext.current
    var pending by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        pending?.takeIf { ok }?.let(onPhoto)
        pending = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onPhoto)
    }

    return PhotoPicker(
        takePhoto = {
            val uri = newPhotoUri(context)
            pending = uri
            cameraLauncher.launch(uri)
        },
        pickFromGallery = { galleryLauncher.launch("image/*") }
    )
}

private fun newPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "photos").apply { mkdirs() }
    val file = File.createTempFile("product_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

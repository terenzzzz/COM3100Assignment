package com.example.mobilesoftware.view

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesoftware.view.viewmodels.ImageListViewModel
import com.example.mobilesoftware.view.viewmodels.ImageViewModelFactory

open class ImageAppCompatActivity: AppCompatActivity() {

    // Instantiate the ViewModel from the ImageViewModelFactory
    // which extends ViewModelProvider.Factory
    protected val imageViewModel: ImageListViewModel by viewModels {
        ImageViewModelFactory((application as ImageApplication).imgrepository,(application as ImageApplication).triprepository, application)
    }
}
package com.example.mobilesoftware.view

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesoftware.view.viewmodels.ImageListViewModel
import com.example.mobilesoftware.view.viewmodels.ImageViewModelFactory
import com.example.mobilesoftware.view.viewmodels.TripListViewModel
import com.example.mobilesoftware.view.viewmodels.TripListViewModelFactory

open class TripAppCompatActivity: AppCompatActivity() {

    // Instantiate the ViewModel from the ImageViewModelFactory
    // which extends ViewModelProvider.Factory
    protected val tripListViewModel: TripListViewModel by viewModels {
        TripListViewModelFactory((application as ImageApplication).triprepository, application)
    }
}
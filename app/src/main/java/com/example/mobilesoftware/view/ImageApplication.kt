package com.example.mobilesoftware.view

import android.app.Application
import com.example.mobilesoftware.view.database.AppDatabase
import com.example.mobilesoftware.view.respository.ImageRepository
import com.example.mobilesoftware.view.respository.TripRepository

class ImageApplication: Application() {
    // This has been updated to initialize the repository along with the database when the
    // using by lazy again,which ensures either are created until they are
    // needed (i.e. referenced for the first time).
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val imgrepository: ImageRepository by lazy { ImageRepository(database.imageDao()) }
    val triprepository: TripRepository by lazy { TripRepository(database.tripDao(),database.locationDao()) }
}
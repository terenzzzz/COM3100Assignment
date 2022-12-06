package com.example.mobilesoftware.view.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.constraintlayout.widget.ConstraintSet.Transform
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.example.mobilesoftware.R
import com.example.mobilesoftware.view.database.ImageEntity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.TripElement
import com.example.mobilesoftware.view.respository.ImageRepository
import com.example.mobilesoftware.view.respository.asDomainModel
import com.example.mobilesoftware.view.respository.asDomainModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * ImageViewModel stores and manage UI-related data in a lifecycle aware way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * tasks can continue through configuration changes and deliver results after Fragment or Activity
 * is available.
 *
 * @param repository - data access is through the repository.
 */
class ImageListViewModel(private val imgrepository: ImageRepository, private val applicationContext: Application) : ViewModel() {

    // Receive the Flow of ImageEntity data from the repository, but transform to the LiveData of Images
    // that will be observed fom the view
    var images: LiveData<List<Image>> = Transformations.map(imgrepository.images){
        it.asDomainModels(applicationContext)
    } as MutableLiveData<List<Image>>
    /**
     * Retrieves a single Image object for the specified id
     */
    fun getImage(id: Int) : LiveData<Image> = Transformations.map(imgrepository.getImage(id)){
        it.asDomainModel(applicationContext)
    }

    /**
     * Filters the images to only include those with the correct tripID and
     * ordered in desired setting
     */
    fun filter(tripID : Int,setting: Int){
        imgrepository.filter(tripID,setting)
        images = Transformations.map(imgrepository.images){
            it.asDomainModels(applicationContext)
        } as MutableLiveData<List<Image>>
    }

    /**
     * Launching a new coroutine to INSERT an Image object in a non-blocking way
     *
     * Usinh the viewModelScope means this function (and others below), don't need to be
     * suspending. Allowing the function to be directly consumable
     * from the view classes without declaring a coroutine scope in the view.
     */
    fun insert(image: Image) = viewModelScope.launch {
        imgrepository.insert(image)
    }

    /**
     * Inserts an Image into the database providing property values of an Image object
     */
    fun insert(
        image_uri: Uri,
        title: String = "Default",
        description: String? = null
    ) = viewModelScope.launch {
        imgrepository.insert(
            image_uri = image_uri,
            title = title,
            description = description,
            date = LocalDate.now(),
            context = applicationContext)
    }

    /**
     * Launching a new coroutine to UPDATE an Image object in a non-blocking way
     */
    fun update(image: Image) = viewModelScope.launch {
        imgrepository.update(image)
    }

    /**
     * Launching a new coroutine to DELETE an Image object in a non-blocking way
     */
    fun delete(image: Image) = viewModelScope.launch {
        imgrepository.delete(image)
    }

}

// Extends the ViewModelProvider.Factory allowing us to control the viewmodel creation
// and provide the right parameters
class ImageViewModelFactory(private val repository: ImageRepository,  private val applicationContext: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageListViewModel::class.java)) {
            return ImageListViewModel(repository, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
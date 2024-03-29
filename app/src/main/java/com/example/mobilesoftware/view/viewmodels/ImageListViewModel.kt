package com.example.mobilesoftware.view.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.example.mobilesoftware.view.database.ImageEntity
import com.example.mobilesoftware.view.database.LocationEntity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.Location
import com.example.mobilesoftware.view.model.TripElement
import com.example.mobilesoftware.view.respository.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.coroutines.coroutineContext

/**
 * ImageListViewModel stores and manage UI-related data in a lifecycle aware way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * tasks can continue through configuration changes and deliver results after Fragment or Activity
 * is available.
 *
 * @param imgrepository - data access is through the image repository.
 * @param tripRepository - data access is through the trip repository.
 * @param applicationContext - provides context of application to points of the app that require it
 */
class ImageListViewModel(private val imgrepository: ImageRepository,private val tripRepository: TripRepository,private val applicationContext: Application) : ViewModel() {

    var trips: LiveData<List<TripElement>> = Transformations.map(tripRepository.trips){
        it.asDomainModels()
    } as MutableLiveData<List<TripElement>>

    // Receive the Flow of LocationEntity data from the repository, but transform to the LiveData of Locations
    // that will be observed fom the view
    var locations: LiveData<List<Location>> = Transformations.map(imgrepository.locations){
        it.asLocDatabaseEntities()
    } as MutableLiveData<List<Location>>

    /**
     * Retrieves a single Trip Element object for the specified id
     */
    fun getTrip(id: Int) : LiveData<TripElement> = Transformations.map(tripRepository.getTrip(id)){
        it.asDomainModel()
    }
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
     * Update the locations being used so it corresponds to the locations of the input trip id
     * @return Locations of a trip
     */
    fun getLocationsByTripID(tripID : Int) = viewModelScope.launch{
        imgrepository.getLocationsByTripID(tripID)
        locations = Transformations.map(imgrepository.locations){
            it.asLocDatabaseEntities()
        } as MutableLiveData<List<Location>>
    }

    /**
     * Launching a new coroutine to INSERT an Image object in a non-blocking way
     *
     * Using the viewModelScope means this function (and others below), don't need to be
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
        title: String = "Title here",
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
class ImageViewModelFactory(private val repository: ImageRepository, private val tripRepository: TripRepository,  private val applicationContext: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageListViewModel::class.java)) {
            return ImageListViewModel(repository, tripRepository, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
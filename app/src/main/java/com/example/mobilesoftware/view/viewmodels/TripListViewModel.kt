package com.example.mobilesoftware.view.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.constraintlayout.widget.ConstraintSet.Transform
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.example.mobilesoftware.R
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.TripElement
import com.example.mobilesoftware.view.respository.ImageRepository
import com.example.mobilesoftware.view.respository.TripRepository
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
class TripListViewModel(private val triprepository: TripRepository, private val applicationContext: Application) : ViewModel() {

    // Receive the Flow of ImageEntity data from the repository, but transform to the LiveData of Images
    // that will be observed fom the view
    val trips: LiveData<List<TripElement>> = Transformations.map(triprepository.trips){
        it.asDomainModels()
    } as MutableLiveData<List<TripElement>>
    /**
     * Retrieves a single Image object for the specified id
     */
    fun getImage(id: Int) : LiveData<TripElement> = Transformations.map(triprepository.getTrip(id)){
        it.asDomainModel()
    }

    /**
     * Launching a new coroutine to INSERT an Image object in a non-blocking way
     *
     * Usinh the viewModelScope means this function (and others below), don't need to be
     * suspending. Allowing the function to be directly consumable
     * from the view classes without declaring a coroutine scope in the view.
     */
    fun insert(trip: TripElement) = viewModelScope.launch {
        triprepository.insert(trip)
    }

    /**
     * Inserts an Image into the database providing property values of an Image object
     */
    fun insert(
        title: String,
        date: LocalDate
    ) = viewModelScope.launch {
        triprepository.insert(
            title = title,
            date = date)
    }

    /**
     * Launching a new coroutine to UPDATE an Image object in a non-blocking way
     */
    fun update(trip: TripElement) = viewModelScope.launch {
        triprepository.update(trip)
    }

    /**
     * Launching a new coroutine to DELETE an Image object in a non-blocking way
     */
    fun delete(trip: TripElement) = viewModelScope.launch {
        triprepository.delete(trip)
    }

}

// Extends the ViewModelProvider.Factory allowing us to control the viewmodel creation
// and provide the right parameters
class TripListViewModelFactory(private val repository: TripRepository,  private val applicationContext: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripListViewModel::class.java)) {
            return TripListViewModel(repository, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
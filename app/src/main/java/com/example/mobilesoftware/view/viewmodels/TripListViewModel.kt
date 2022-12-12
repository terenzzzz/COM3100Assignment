package com.example.mobilesoftware.view.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import com.example.mobilesoftware.view.model.TripElement
import com.example.mobilesoftware.view.respository.TripRepository
import com.example.mobilesoftware.view.respository.asDomainModels
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ImageViewModel stores and manage UI-related data in a lifecycle aware way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * tasks can continue through configuration changes and deliver results after Fragment or Activity
 * is available.
 *
 * @param triprepository - data access is through the repository.
 */
class TripListViewModel(private val triprepository: TripRepository, private val applicationContext: Application) : ViewModel() {

    // Receive the Flow of ImageEntity data from the repository, but transform to the LiveData of Images
    // that will be observed fom the view
    var trips: LiveData<List<TripElement>> = Transformations.map(triprepository.trips){
        it.asDomainModels(applicationContext)
    } as MutableLiveData<List<TripElement>>

    /**
     * Sorting the images to be in correct order
     */
    fun sorting(setting: Int){
        triprepository.sorting(setting)
        trips = Transformations.map(triprepository.trips){
            it.asDomainModels(applicationContext)
        } as MutableLiveData<List<TripElement>>
    }

    /**
     * Launching a new coroutine to INSERT an TripElement object in a non-blocking way
     *
     * Using the viewModelScope means this function (and others below), don't need to be
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
        date: LocalDate,
        time: String
    ) = viewModelScope.launch {
        triprepository.insert(
            title = title,
            date = date,
            time = time)
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
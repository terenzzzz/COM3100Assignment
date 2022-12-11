package com.example.mobilesoftware.view.respository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mobilesoftware.view.database.LocationDao
import com.example.mobilesoftware.view.database.LocationEntity
import com.example.mobilesoftware.view.database.TripDao
import com.example.mobilesoftware.view.database.TripEntity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.Location
import com.example.mobilesoftware.view.model.TripElement
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * The repository is instantiated in the ImageApplication class
 * and passed an instance of a DAO obtained returned by the room database
 * instance, so we only need this private tripDao property in the
 * default constructor in this implementation
 *
 * This handles the Trip table and the location table since they are
 * both very related
 */
class TripRepository(private val tripDao: TripDao,private val locationDao: LocationDao) {
    // The ViewModel will observe this Flow
    // Room will handle executing this on a thread
    var trips: LiveData<List<TripEntity>> = tripDao.getTrips().asLiveData()

    fun sorting(setting : Int){
        trips = if(setting == 1){
            tripDao.getTripsDesc().asLiveData()
        }else{
            tripDao.getTrips().asLiveData()
        }
    }

    @WorkerThread
    suspend fun insert(trip: TripElement){
        tripDao.insert(trip.asDatabaseEntity())
    }

    /**
     * The insert version used when a user selects an image using the photo picker
     * or from camera. This is better than using the initNewImageData() function
     * in the previous implementation as there is no need to write boiler plate
     * code to retrieve teh data again after saving it.
     */
    suspend fun insert(
        title: String,
        date: LocalDate,
        time: String
    ) : Int{
        val trip =
            TripElement(
                title = title,
                date = date,
                time = time
            )
        return tripDao.insert(trip.asDatabaseEntity()).toInt()
    }

    suspend fun update(trip: TripElement){
        tripDao.update(trip.asDatabaseEntity())
    }

    suspend fun delete(trip: TripElement){
        tripDao.delete(trip.asDatabaseEntity())
    }

    /**
     * This portion will handle the LocaitonDAO interactions and
     * provide all the functions for handling locations
     */

    @WorkerThread
    suspend fun insertLocation(location: Location): Int {
        return locationDao.insert(location.asDatabaseEntity()).toInt()
    }

    suspend fun updateLocationTripID(lid: Int, tid: Int){
        locationDao.updateLocationsTripID(lid,tid)
    }

    suspend fun getLocationsByTripID(tid: Int): Flow<List<LocationEntity>> {
        return locationDao.getLocationsByTripID(tid)
    }


}


/***
 * Function to map Trip database entities to the domain model
 */
fun TripEntity.asDomainModel(): TripElement {
    return TripElement(
        id = id,
        title = title,
        date = LocalDate.parse(date),
        time = time
    )
}


fun List<TripEntity>.asDomainModels(): List<TripElement>{
    return map{
        TripElement(
            id = it.id,
            title = it.title,
            date = LocalDate.parse(it.date),
            time = it.time
        )
    }
}

/**
 * The versions of the above functions that handles a collection
 */
fun TripElement.asDatabaseEntity(): TripEntity{
    return TripEntity(
        id = id,
        title = title,
        date = date.toString(),
        time = time
    )
}

fun List<TripElement>.asDatabaseEntities(): List<TripEntity>{
    return map{
        TripEntity(
            id = it.id,
            title = it.title,
            date = it.date.toString(),
            time = it.time
        )
    }
}

/***
 * Function to map Location database entities to the domain model
 */
fun LocationEntity.asDomainModel(): Location {
    return Location(
        id = id,
        longitude = longitude,
        latitude = latitude,
        tripID = tripID
    )
}

/**
 * The versions of the above functions that handles a collection
 */
fun Location.asDatabaseEntity(): LocationEntity{
    return LocationEntity(
        id = id,
        longitude = longitude,
        latitude = latitude,
        tripID = tripID
    )
}



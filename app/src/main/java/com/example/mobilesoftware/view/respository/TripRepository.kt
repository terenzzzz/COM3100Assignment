package com.example.mobilesoftware.view.respository

import android.app.Application
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

    // FLow of trip data
    var trips: LiveData<List<TripEntity>> = tripDao.getTrips().asLiveData()

    // Retrieve specific trip
    fun getTrip(id: Int) : LiveData<TripEntity>{
        return tripDao.getTrip(id).asLiveData()
    }

    // Sort based on users preference given by viewmodel/view
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

}


/***
 * Functions to map database entities to the domain model and vice verse
 * for Trips and Locations
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

fun Location.asDatabaseEntity(): LocationEntity{
    return LocationEntity(
        id = id,
        longitude = longitude,
        latitude = latitude,
        tripID = tripID
    )
}



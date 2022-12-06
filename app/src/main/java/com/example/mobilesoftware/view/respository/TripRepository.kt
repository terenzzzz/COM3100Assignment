package com.example.mobilesoftware.view.respository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mobilesoftware.view.database.TripDao
import com.example.mobilesoftware.view.database.TripEntity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.TripElement
import com.example.mobilesoftware.view.utils.getOrMakeThumbNail
import java.sql.Time
import java.time.LocalDate

class TripRepository(private val tripDao: TripDao) {
    // The ViewModel will observe this Flow
    // Room will handle executing this on a thread
    var trips: LiveData<List<TripEntity>> = tripDao.getTrips().asLiveData()

    fun getTrip(id: Int) = tripDao.getTrip(id).asLiveData()

    fun sorting(setting : Int){
        if(setting == 1){
            trips = tripDao.getTripsDesc().asLiveData()
        }else{
            trips = tripDao.getTrips().asLiveData()
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
        time: Time
    ) : Int{
        var trip =
            TripElement(
                title = title,
                date = date,
                time = time
            )
        return tripDao.insert(trip!!.asDatabaseEntity()).toInt()
    }

    suspend fun update(trip: TripElement){
        tripDao.update(trip.asDatabaseEntity())
    }

    suspend fun delete(trip: TripElement){
        tripDao.delete(trip.asDatabaseEntity())
    }
}

/***
 * Function to map database entities to the domain model
 *
 *  Easier to appropriate the importance of this when you consider the fact
 * that the domain model may need to represent other properties and methods
 * which do not need to be present in the entity representation. In this particular implementation
 * they match exactly, but the implementation is ready if they start to diverge
 */
fun TripEntity.asDomainModel(): TripElement {
    val trip = TripElement(
        id = id,
        title = title,
        date = LocalDate.parse(date),
        time = Time.valueOf(time)
    )
    return trip
}


fun List<TripEntity>.asDomainModels(): List<TripElement>{
    return map{
        TripElement(
            id = it.id,
            title = it.title,
            date = LocalDate.parse(it.date),
            time = Time.valueOf(it.time)
        )
    }
}

/**
 * The version of the above function that handles a collection
 */
fun TripElement.asDatabaseEntity(): TripEntity{
    return TripEntity(
        id = id,
        title = title,
        date = date.toString(),
        time = time.toString()
    )
}

/**
 * The version of the above function that handles a collection
 */
fun List<TripElement>.asDatabaseEntities(): List<TripEntity>{
    return map{
        TripEntity(
            id = it.id,
            title = it.title,
            date = it.date.toString(),
            time = it.time.toString()
        )
    }
}

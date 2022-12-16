package com.example.mobilesoftware.view.respository

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.asLiveData
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.database.ImageDao
import com.example.mobilesoftware.view.database.ImageEntity
import com.example.mobilesoftware.view.database.LocationDao
import com.example.mobilesoftware.view.database.LocationEntity
import com.example.mobilesoftware.view.model.Location
import com.example.mobilesoftware.view.utils.getOrMakeThumbNail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.time.LocalDate
import kotlin.coroutines.coroutineContext


/**
 * The repository is instantiated in the ImageApplication class
 * and passed an instance of a DAO obtained returned by the room database
 * instance, so we only need this private imageDao property in the
 * default constructor in this implementation
 */
class ImageRepository(private val imageDao: ImageDao,private val locationDao: LocationDao) {

    //Flow of Images and locations data to be used by viewmodels
    var images: LiveData<List<ImageEntity>> = imageDao.getImages().asLiveData()

    var locations: LiveData<List<LocationEntity>> = locationDao.getLocationsByTripID(-1).asLiveData()

    //Retrieve a single image from databse
    fun getImage(id: Int) : LiveData<ImageEntity>{
        return imageDao.getImage(id).asLiveData()
    }

    /**
     * Filters the flow of the data to the preference specificed by the user
     */
    fun filter(tripID : Int,setting: Int){
        if(tripID == -1){
            if(setting == 0){
                images = imageDao.getImages().asLiveData()
            }else{
                images = imageDao.getImagesDesc().asLiveData()
            }
        }else{
            if(setting == 0){
                images = imageDao.getImagesByIDAsc(tripID).asLiveData()
            }else{
                images = imageDao.getImagesByIDDesc(tripID).asLiveData()
            }
        }
    }

    /**
     * Function to get all locations relating to the tripID entered for the show image activity
     */
    fun getLocationsByTripID(tid: Int){
        locations = locationDao.getLocationsByTripID(tid).asLiveData()
    }

    @WorkerThread
    suspend fun insert(image: Image): Int{
        return imageDao.insert(image.asDatabaseEntity()).toInt()
    }

    /**
     * The insert version used when a user selects an image using the photo picker
     * or from camera. This is better than using the initNewImageData() function
     * in the previous implementation as there is no need to write boiler plate
     * code to retrieve teh data again after saving it.
     */
    suspend fun insert(
        image_uri: Uri,
        title: String,
        description: String? = null,
        date: LocalDate,
        context: Context){
        var image =
            Image(
                imagePath = image_uri,
                title = title,
                description = description,
                date = date
            )
        image.getOrMakeThumbNail(context)
        imageDao.insert(image.asDatabaseEntity())
    }

    // Update TRIPID of images
    suspend fun updateTripID(iid: Int, tid: Int){
        imageDao.updateTripID(iid,tid)
    }

    // Update whole image
    suspend fun update(image: Image){
        imageDao.update(image.asDatabaseEntity())
    }

    // Delete image
    suspend fun delete(image: Image){
        imageDao.delete(image.asDatabaseEntity())
    }

}



/***
 * Functions to map database entities to the domain model and vice verse
 * for Images and Locations
 */
fun ImageEntity.asDomainModel(context: Context): Image {
    val img = Image(
        id = id,
        imagePath = Uri.parse(imagePath),
        title = title,
        description = description,
        tags = tags,
        tripID = tripId,
        latitude = lati,
        longitude = longi,
        pressure = pressure,
        temperature = temp,
        date = LocalDate.parse(date))
    img.getOrMakeThumbNail(context)
    return img
}


fun List<ImageEntity>.asDomainModels(context: Context): List<Image>{
    return map{
        Image(
            id = it.id,
            imagePath = Uri.parse(it.imagePath),
            title = it.title,
            description = it.description,
            thumbnail = getOrMakeThumbNail(it.thumbnail!!, it.imagePath, context)!!,
            tags = it.tags,
            tripID = it.tripId,
            latitude = it.lati,
            longitude = it.longi,
            pressure = it.pressure,
            temperature = it.temp,
            date = LocalDate.parse(it.date)
        )
    }
}

fun Image.asDatabaseEntity(): ImageEntity{
    return ImageEntity(
        id = id,
        imagePath = imagePath.toString(),
        title = title,
        description = description,
        thumbnail = thumbnail.toString(),
        tags = tags,
        tripId = tripID,
        lati = latitude,
        longi = longitude,
        pressure = pressure,
        temp = temperature,
        date = date.toString()
    )
}

fun List<Image>.asDatabaseEntities(): List<ImageEntity>{
    return map{
        ImageEntity(
            id = it.id,
            imagePath = it.imagePath.toString(),
            title = it.title,
            description = it.description,
            thumbnail = it.thumbnail.toString(),
            tags = it.tags,
            tripId = it.tripID,
            lati = it.latitude,
            longi = it.longitude,
            pressure = it.pressure,
            temp = it.temperature,
            date = it.date.toString()
        )
    }
}



fun LocationEntity.asLocDatabaseEntity(): Location{
    return Location(
            id = id,
            longitude = longitude,
            latitude = latitude,
            tripID = tripID
        )
}

fun List<LocationEntity>.asLocDatabaseEntities(): List<Location>{
    return map{
        Location(
            id = it.id,
            longitude = it.longitude,
            latitude = it.latitude,
            tripID = it.tripID
        )
    }
}

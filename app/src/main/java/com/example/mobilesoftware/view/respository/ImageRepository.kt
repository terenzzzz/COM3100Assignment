package com.example.mobilesoftware.view.respository

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.database.ImageDao
import com.example.mobilesoftware.view.database.ImageEntity
import com.example.mobilesoftware.view.utils.getOrMakeThumbNail
import java.time.LocalDate

/**
 * The repository is instantiated in the ImageApplication class
 * and passed an instance of a DAO obtained returned by the room database
 * instance, so we only need this private imageDao property in the
 * default constructor in this implementation
 */
class ImageRepository(private val imageDao: ImageDao) {

    // The ViewModel will observe this Flow
    // Room will handle executing this on a thread
    var images: LiveData<List<ImageEntity>> = imageDao.getImages().asLiveData()

    fun getImage(id: Int) = imageDao.getImage(id).asLiveData()

    fun filter(tripID : Int){
        if(tripID == -1){
            images = imageDao.getImages().asLiveData()
        }else{
            images = imageDao.getImagesByID(tripID).asLiveData()
        }
    }

    @WorkerThread
    suspend fun insert(image: Image){
        imageDao.insert(image.asDatabaseEntity())
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
        imageDao.insert(image!!.asDatabaseEntity())
    }

    suspend fun update(image: Image){
        imageDao.update(image.asDatabaseEntity())
    }

    suspend fun delete(image: Image){
        imageDao.delete(image.asDatabaseEntity())
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
    img.getOrMakeThumbNail(context)!!
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

/**
 * The version of the above function that handles a collection
 */
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

/**
 * The version of the above function that handles a collection
 */
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

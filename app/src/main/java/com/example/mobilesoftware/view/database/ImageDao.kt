package com.example.mobilesoftware.view.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO to handles interations with the image room table
 */

@Dao
interface ImageDao {

    //Get all images ordered by most recent
    @Query("Select * from image ORDER by id DESC")
    fun getImages(): Flow<List<ImageEntity>>

    //Get all images ordered by oldest image
    @Query("Select * from image ORDER by id ASC")
    fun getImagesDesc(): Flow<List<ImageEntity>>

    //Get specific image to display its attributes
    @Query("Select * from image Where id = :id")
    fun getImage(id: Int): Flow<ImageEntity>

    //Get images ordered by newest but only those with specific trip ID
    @Query("Select * from image Where trip_id = :id ORDER by id DESC")
    fun getImagesByIDDesc(id: Int): Flow<List<ImageEntity>>

    //Get images ordered by oldest but only those with specific trip ID
    @Query("Select * from image Where trip_id = :id ORDER by id ASC")
    fun getImagesByIDAsc(id: Int): Flow<List<ImageEntity>>

    //Set specified images tripID's to the chosen one
    @Query("UPDATE image SET trip_id=:tid WHERE id = :iid")
    suspend fun updateTripID(iid : Int,tid : Int)

    // Insert image to table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(imageEntity: ImageEntity): Long

    // Update item
    @Update
    suspend fun update(imageEntity: ImageEntity)

    // Not used but added for potential use
    @Delete
    suspend fun delete(imageEntity: ImageEntity)

}
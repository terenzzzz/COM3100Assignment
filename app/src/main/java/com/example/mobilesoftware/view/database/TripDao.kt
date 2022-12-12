package com.example.mobilesoftware.view.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO to handles interactions with the trip room table
 */


@Dao
interface TripDao {

    //Get all trips by most recent
    @Query("Select * from trips ORDER by id DESC")
    fun getTrips(): Flow<List<TripEntity>>

    //Get all trips by oldest
    @Query("Select * from trips ORDER by id ASC")
    fun getTripsDesc(): Flow<List<TripEntity>>

    //Get a specific trips by ID
    @Query("Select * from trips Where id = :id")
    fun getTrip(id: Int): Flow<TripEntity>

    //Insert trip into table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tripEntity: TripEntity): Long

}
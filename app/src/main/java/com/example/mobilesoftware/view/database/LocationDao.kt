package com.example.mobilesoftware.view.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobilesoftware.view.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * DAO to handles interations with the locations room table
 */


@Dao
interface LocationDao {

    // Get all locations relating to specific tripID
    @Query("Select * from locations Where tripID=:tid")
    fun getLocationsByTripID(tid : Int): Flow<List<LocationEntity>>

    // Insert a location into table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tripEntity: LocationEntity): Long

    //Set all locations from a spficif trip to its tripID
    @Query("UPDATE locations SET tripid=:tid Where id = :iid")
    suspend fun updateLocationsTripID(iid : Int,tid : Int)

}
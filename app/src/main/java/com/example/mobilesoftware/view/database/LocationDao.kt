package com.example.mobilesoftware.view.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    // suspend keyword not needed with the use of Flow
    @Query("Select * from locations WHERE tripID=:tid ORDER by id ASC")
    fun getLocationsByTripID(tid : Int): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tripEntity: LocationEntity): Long

    @Query("UPDATE image SET trip_id=:tid WHERE id = :iid")
    suspend fun updateLocationsTripID(iid : Int,tid : Int)

}
package com.example.mobilesoftware.view.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    // suspend keyword not needed with the use of Flow
    @Query("Select * from trips ORDER by id ASC")
    fun getTrips(): Flow<List<TripEntity>>

    // Useful for tracking Entities
    @Query("Select * from trips Where id = :id")
    fun getTrip(id: Int): Flow<TripEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tripEntity: TripEntity): Long

    @Update
    suspend fun update(tripEntity: TripEntity)

    @Delete
    suspend fun delete(tripEntity: TripEntity)

}
package com.example.mobilesoftware.view.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity used to handle the locations in the system
 */

@Entity(tableName = "locations", indices=[Index(value=["id","tripID"])])
data class LocationEntity (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="longitude") var longitude: String,
    @ColumnInfo(name="latitude") var latitude: String,
    @ColumnInfo(name="tripID") var tripID: Int
)

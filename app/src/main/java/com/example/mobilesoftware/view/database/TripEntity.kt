package com.example.mobilesoftware.view.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trips", indices=[Index(value=["id", "trip_title"])])
data class TripEntity (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="trip_title") val title: String,
    @ColumnInfo(name="trip_date") var date: String,
    @ColumnInfo(name="trip_time") var time: String
)

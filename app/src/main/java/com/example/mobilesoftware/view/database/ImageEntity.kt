package com.example.mobilesoftware.view.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity used to handle the images in the image table
 * All columns are pretty self-explanatory
 */

@Entity(tableName = "image", indices=[Index(value=["id", "image_title"])])
data class ImageEntity (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name="image_path") val imagePath: String,
    @ColumnInfo(name="image_title") var title: String,
    @ColumnInfo(name="image_description") var description: String?,
    @ColumnInfo(name="image_date") var date: String?,
    @ColumnInfo(name="thumbnail_filename") var thumbnail: String?,
    @ColumnInfo(name="tags") var tags: String?,
    @ColumnInfo(name="trip_id") var tripId: Int?,
    @ColumnInfo(name="latitude") var lati: String?,
    @ColumnInfo(name="longitude") var longi: String?,
    @ColumnInfo(name="pressure") var pressure: String?,
    @ColumnInfo(name="temperature") var temp: String?
 )

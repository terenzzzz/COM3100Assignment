package com.example.mobilesoftware.view.model

/**
 * Data class that LocationEntities from database are
 * transformed into to be used in the app
 */

data class Location(
    val id : Int = 0,
    val longitude : String,
    val latitude: String,
    val tripID: Int
) {

    override fun equals(other: Any?): Boolean {
        val is_equal = super.equals(other)

        if (!is_equal){ return false}
        val other_trip = other as TripElement
        return this.id == other_trip.id
    }
}
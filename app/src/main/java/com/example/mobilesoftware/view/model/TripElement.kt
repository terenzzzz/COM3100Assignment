package com.example.mobilesoftware.view.model

import java.time.LocalDate

data class TripElement(
    val id : Int = 0,
    val title : String,
    val date: LocalDate,
    val time: String
) {

    override fun equals(other: Any?): Boolean {
        val is_equal = super.equals(other)

        if (!is_equal){ return false}
        val other_trip = other as TripElement
        return this.id == other_trip.id &&
                this.title == other_trip.title &&
                this.date == other_trip.date
    }
}
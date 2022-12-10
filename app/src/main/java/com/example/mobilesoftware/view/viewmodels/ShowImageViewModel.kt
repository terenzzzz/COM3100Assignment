package com.example.mobilesoftware.view.viewmodels

import android.net.Uri
import android.os.Build
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilesoftware.view.ImageApplication
import com.example.mobilesoftware.view.database.ImageEntity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.Trip
import com.example.mobilesoftware.view.respository.ImageRepository
import com.example.mobilesoftware.view.respository.TripRepository
import kotlinx.coroutines.launch
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ShowImageViewModel : ViewModel() {

    private var tripRepository: TripRepository = ImageApplication().triprepository
    private var imageRepository: ImageRepository = ImageApplication().imgrepository

    var title: ObservableField<String> = ObservableField()
    var description: ObservableField<String> = ObservableField()
    var tripTitle: ObservableField<String> = ObservableField()
    var date: ObservableField<String> = ObservableField()
    var temperature: ObservableField<String> = ObservableField()
    var pressure: ObservableField<String> = ObservableField()
    var latitude: ObservableField<String> = ObservableField()
    var longitude: ObservableField<String> = ObservableField()
    var tripID: Int = -1
    var imgIDs: MutableList<Int> = arrayListOf()

    fun init(title:String?,time:String?){
        this.title.set(title)
        this.startTime.set(time)
    }

    fun setCurrentImageLocation(latitude:String,longitude:String){
        this.latitude.set(latitude)
        this.longitude.set(longitude)
    }



    fun setDate(date: LocalDate){
        this.date.set(date.toString())
    }

    fun setTemperature(temperature:String){
        this.temperature.set(temperature)
    }

    fun setPressure(Pressure:String){
        this.pressure.set(Pressure)
    }



    fun returnStartTime(): LocalDate {
        var startTimeString: String? = this.startTime.get()
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val startTime = LocalDateTime.parse(startTimeString, pattern)
        return startTime.toLocalDate()
    }


    fun returnDuration(): Time {
        val durationString: String? = this.duration.get()
        return Time.valueOf(durationString)
    }

    fun returnTitle(): String? {
        return this.title.get()
    }

}
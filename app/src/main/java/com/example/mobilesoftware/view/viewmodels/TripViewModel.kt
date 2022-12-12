package com.example.mobilesoftware.view.viewmodels

import android.net.Uri
import android.os.Build
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilesoftware.view.ImageApplication
import com.example.mobilesoftware.view.database.ImageEntity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.Location
import com.example.mobilesoftware.view.model.Trip
import com.example.mobilesoftware.view.respository.ImageRepository
import com.example.mobilesoftware.view.respository.TripRepository
import kotlinx.coroutines.launch
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TripViewModel : ViewModel() {
    private var model: Trip = Trip()
    private var tripRepository : TripRepository = ImageApplication().triprepository
    private var imageRepository : ImageRepository = ImageApplication().imgrepository

    var weatherIcon: ObservableField<String> = ObservableField()
    var weather: ObservableField<String> = ObservableField()
    var temp: ObservableField<String> = ObservableField()

    var title: ObservableField<String> = ObservableField()
    var startTime: ObservableField<String> = ObservableField()
    var currentTime: ObservableField<String> = ObservableField()
    var duration: ObservableField<String> = ObservableField()
    var temperature: ObservableField<String> = ObservableField()
    var pressure: ObservableField<String> = ObservableField()
    var latitude: ObservableField<String> = ObservableField()
    var longitude: ObservableField<String> = ObservableField()
    var tripID : Int = -1
    var imgIDs : MutableList<Int> = arrayListOf()
    var locIDs : MutableList<Int> = arrayListOf()

    fun init(title:String?,time:String?){
        this.title.set(title)
        this.startTime.set(time)
    }

    fun setLocation(latitude:String,longitude:String){
        this.latitude.set(latitude)
        this.longitude.set(longitude)
    }

    fun setCurrentTime(){
        if(Build.VERSION.SDK_INT>=26) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatted = current.format(formatter)
            this.currentTime.set(formatted)
        }
    }

    fun setDuration(){
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val startTime = LocalDateTime.parse(this.startTime.get(), pattern)
        val currentTime = LocalDateTime.now()
        var diff = java.time.Duration.between(startTime,currentTime );

        var seconds = diff.seconds
        val HH: Long = seconds / 3600
        val MM: Long = seconds % 3600 / 60
        val SS: Long = seconds % 60

        val hms = String.format(
            "%02d:%02d:%02d",
            HH,MM,SS
        )
        this.duration.set(hms)
    }

    fun setTemperature(temperature:String){
        this.temperature.set(temperature)
    }

    fun setPressure(Pressure:String){
        this.pressure.set(Pressure)
    }

    fun setWeather(weatherIcon:String, weather:String, temp:String){
        this.weatherIcon.set(weatherIcon)
        this.weather.set(weather)
        this.temp.set(temp)
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


    fun insertimage(image: Uri){
        viewModelScope.launch {
            val id = imageRepository.insert(
                Image(
                    imagePath = image,
                    title = "Default",
                    date = LocalDate.now(),
                    tripID = tripID,
                    latitude = latitude.get(),
                    longitude = longitude.get(),
                    pressure = pressure.get(),
                    temperature = temperature.get())
            )
            imgIDs.add(id)
        }
    }

    fun insertLocation(longitude: String,latitude: String){
        viewModelScope.launch {
            val id = tripRepository.insertLocation(
                Location(
                    tripID = tripID,
                    latitude = latitude,
                    longitude = longitude)
            )
            locIDs.add(id)
        }
    }

    suspend fun assignTripId(tID : Int){
        for (v in imgIDs){
            imageRepository.updateTripID(v,tID)
        }
        for (v in locIDs){
            tripRepository.updateLocationTripID(v,tID)
        }
    }

    fun insertTrip(title: String,date: LocalDate, time: String){
        viewModelScope.launch {
            assignTripId(tripRepository.insert(title,date, time))
        }
    }



}
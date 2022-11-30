package com.example.mobilesoftware.view.viewmodels

import android.os.Build
import androidx.databinding.ObservableField
import com.example.mobilesoftware.view.model.Trip
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TripViewModel : ViewModel {
    private var model: Trip = Trip()

    var title: ObservableField<String> = ObservableField()
    var startTime: ObservableField<String> = ObservableField()
    var currentTime: ObservableField<String> = ObservableField()
    var duration: ObservableField<String> = ObservableField()
    var temperature: ObservableField<String> = ObservableField()
    var pressure: ObservableField<String> = ObservableField()
    var latitude: ObservableField<String> = ObservableField()
    var longitude: ObservableField<String> = ObservableField()

    override fun onCreate() {}
    override fun onPause() {}
    override fun onResume() {}
    override fun onDestroy() {}

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

    fun setDuration(start:String){
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val startTime = LocalDateTime.parse(start, pattern)
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



}
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

        val hms = String.format(
            "%d:%02d:%02d",
            diff.toHours(),
            diff.toMinutes(),
            diff.seconds
        )
        this.duration.set(hms)

    }



}
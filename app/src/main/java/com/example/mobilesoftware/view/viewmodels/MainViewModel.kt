package com.example.mobilesoftware.view.viewmodels

import android.os.Build
import androidx.databinding.ObservableField
import com.example.mobilesoftware.view.model.Trip
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainViewModel : ViewModel {


    var time: ObservableField<String> = ObservableField()

    override fun onCreate() {}
    override fun onPause() {}
    override fun onResume() {}
    override fun onDestroy() {}

    fun init(){
        if(Build.VERSION.SDK_INT>=26) {
            val current = LocalDateTime.now()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatted = current.format(formatter)
            this.time.set(formatted)
        }

    }

}
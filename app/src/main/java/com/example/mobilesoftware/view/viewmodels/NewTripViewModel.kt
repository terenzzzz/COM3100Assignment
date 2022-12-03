package com.example.mobilesoftware.view.viewmodels

import android.os.Build
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NewTripViewModel : ViewModel() {


    var time: ObservableField<String> = ObservableField()


    fun init(){
        if(Build.VERSION.SDK_INT>=26) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formatted = current.format(formatter)
            this.time.set(formatted)
        }
    }


}
package com.example.mobilesoftware.view.viewmodels

import androidx.databinding.ObservableField
import com.example.mobilesoftware.view.model.Trip



class TripViewModel : ViewModel {
    private var model: Trip = Trip()

    var title: ObservableField<String> = ObservableField()

    override fun onCreate() {}
    override fun onPause() {}
    override fun onResume() {}
    override fun onDestroy() {}

    fun init(title:String?){
        this.title.set(title)
    }

}
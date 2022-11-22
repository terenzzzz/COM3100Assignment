package com.example.mobilesoftware.view.viewmodels


interface ViewModel {
    fun onCreate()
    fun onPause()
    fun onResume()
    fun onDestroy()
}
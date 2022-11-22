package com.example.mobilesoftware.view.model

class Trip {
    private lateinit var title: String
    private lateinit var time: String

    fun setTitle(title:String){
        this.title = title
    }

    fun getTitle(): String? {
        return "Testing"
    }
}
package com.example.mobilesoftware.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.mobilesoftware.R
import com.google.android.material.snackbar.Snackbar

class TripActivity : AppCompatActivity() {

    companion object {
        fun startFn(context: Context) {
            val intent =
                Intent(context, TripActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        val backBtn = findViewById<ImageView>(R.id.backIcon)
        val stopBtn = findViewById<Button>(R.id.stop)

        backBtn.setOnClickListener(View.OnClickListener { view ->
            // Do some work here
            MainActivity.startFn(this)
        })

        stopBtn.setOnClickListener(View.OnClickListener { view ->
            // Do some work here
            val mySnackbar = Snackbar.make(view, "Stop Clicked", Snackbar.LENGTH_SHORT)
            mySnackbar.show()
        })
    }
}
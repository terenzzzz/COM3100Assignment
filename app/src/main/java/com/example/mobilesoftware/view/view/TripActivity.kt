package com.example.mobilesoftware.view.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityTripBinding
import com.example.mobilesoftware.view.viewmodels.TripViewModel
import com.google.android.material.snackbar.Snackbar

class TripActivity : AppCompatActivity() {
    var myViewModel = TripViewModel()

    companion object {
        fun startFn(context: Context) {
            val intent =
                Intent(context, TripActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = ActivityTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = myViewModel
        myViewModel.onCreate()

        val title = intent.getStringExtra("title")
        myViewModel.init(title)

        binding.backIcon.setOnClickListener { view ->
            // Do some work here
            MainActivity.startFn(this)
            this.finish()
        }

        binding.stop.setOnClickListener { view ->
            // Do some work here
            MainActivity.startFn(this)
            this.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        myViewModel.onPause()
    }

    override fun onResume() {
        super.onResume()
        myViewModel.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        myViewModel.onDestroy()
    }
}
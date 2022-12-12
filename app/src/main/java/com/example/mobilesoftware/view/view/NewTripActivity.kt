package com.example.mobilesoftware.view.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.mobilesoftware.databinding.ActivityNewTripBinding
import com.example.mobilesoftware.view.service.SensorService
import com.example.mobilesoftware.view.viewmodels.NewTripViewModel
import java.util.*

class NewTripActivity : AppCompatActivity() {
    var myViewModel = NewTripViewModel()
    val PERMISSION_LOCATION_GPS:Int = 1

    companion object {
        fun startFn(context: Context) {
            val intent =
                Intent(context, NewTripActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = ActivityNewTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = myViewModel

        // Permissiont request
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_GPS
            )
        }


        Timer().schedule(object : TimerTask() {
            override fun run() {
                myViewModel.init()
            }
        }, 0,1000)

        binding.start.setOnClickListener(View.OnClickListener { view ->
            // Do some work here
            val title = binding.etTitle.text.toString()
            val time = binding.time.text.toString()

            val intent = Intent(this,TripActivity::class.java);
            intent.putExtra("title", title);
            intent.putExtra("time", time)
            startActivity(intent);

            this.finish()
        })
    }

}
package com.example.mobilesoftware.view.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityTripBinding
import com.example.mobilesoftware.view.viewmodels.TripViewModel
import com.google.android.gms.location.*



class TripActivity : AppCompatActivity() {

    var myViewModel = TripViewModel()

    private lateinit var map: MapsFragment

//    Location
    val PERMISSION_LOCATION_GPS:Int = 1
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

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

//        Add map
        map = MapsFragment()
        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.add(R.id.fl_map, map);
        transaction.commit();



//        GetLocation
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?:return
                for (location in p0.locations){
                    lastLocation = location
                    myViewModel.setLocation(lastLocation.latitude.toString(),lastLocation.longitude.toString())
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //        Set data
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
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        myViewModel.onResume()
        refreshLatLon()
    }

    override fun onDestroy() {
        super.onDestroy()
        myViewModel.onDestroy()
    }


    @SuppressLint("MissingPermission")
    private fun refreshLatLon(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_LOCATION_GPS)
        }

        var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, Long.MAX_VALUE
        ).build()
//        var locationRequest = LocationRequest.create()
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
    }

//    override fun onMapReady(p0: GoogleMap) {
//        val Sheffield = LatLng(53.3847433, -1.4741)
//        p0.addMarker(MarkerOptions().position(Sheffield).title("Marker in Sheffield"))
//        p0.moveCamera(CameraUpdateFactory.newLatLngZoom(Sheffield, 15f))
//    }


}
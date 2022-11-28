package com.example.mobilesoftware.view.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityTripBinding
import com.example.mobilesoftware.view.viewmodels.TripViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class TripActivity : AppCompatActivity(), OnMapReadyCallback{

    var myViewModel = TripViewModel()

    private lateinit var mMap: GoogleMap

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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        GetLocation
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    lastLocation = location
                }
                myViewModel.setLocation(lastLocation.latitude.toString(),lastLocation.longitude.toString())
                addMarker(lastLocation.latitude,lastLocation.longitude)
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
        refreshLatLon()
    }

    override fun onDestroy() {
        super.onDestroy()
        myViewModel.onDestroy()
    }


    private fun refreshLatLon(){
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),PERMISSION_LOCATION_GPS)
        }else{
            var locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000
            ).build()
//        var locationRequest = LocationRequest.create()
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun addMarker(latitude:Double,longitude:Double){
        val point = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(point))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }


}
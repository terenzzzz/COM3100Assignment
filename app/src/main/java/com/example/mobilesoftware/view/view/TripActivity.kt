package com.example.mobilesoftware.view.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.icu.number.NumberFormatter.with
import android.hardware.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.VISIBLE
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityTripBinding
import com.example.mobilesoftware.view.dataParse.Weather
import com.example.mobilesoftware.view.viewmodels.TripViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException
import java.util.*


class TripActivity : AppCompatActivity(), OnMapReadyCallback{

    var myViewModel = TripViewModel()
    private lateinit var mMap: GoogleMap
    private val client = OkHttpClient()
    private lateinit var binding: ActivityTripBinding
    private var weatherIconShow = false


    //    Location
    val PERMISSION_LOCATION_GPS:Int = 1
    private var startLocation: Location? =null
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var headMarker:Marker? = null
//    temperature & pressure
    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor?=null
    private lateinit var temperatureCallback: SensorEventCallback
    private var pressureSensor: Sensor?=null
    private lateinit var pressureCallback: SensorEventCallback
    private var tripID: Int = -1



    companion object {
        fun startFn(context: Context) {
            val intent =
                Intent(context, TripActivity::class.java)
            context.startActivity(intent)
        }
    }

    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let{ uri ->
            // https://developer.android.com/training/data-storage/shared/photopicker#persist-media-file-access
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this@TripActivity.contentResolver.takePersistableUriPermission(uri, flag)

            myViewModel.insertimage(uri)
            this.addPicMarker(lastLocation.latitude,lastLocation.longitude)
        }
    }

    val pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        val photo_uri = result.data?.extras?.getString("uri")

        photo_uri?.let{
            val uri = Uri.parse(photo_uri)
            myViewModel.insertimage(uri)
            this.addPicMarker(lastLocation.latitude,lastLocation.longitude)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = myViewModel

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
                    if (startLocation==null){
                        startLocation = location
                        lastLocation = location
                        addMarker(location.latitude,location.longitude)
                        getWeather(location.latitude,location.longitude)
                    }

                    drawLine(lastLocation,location)
                    addDot(location.latitude,location.longitude)
                    lastLocation = location
                }
                myViewModel.setLocation(lastLocation.latitude.toString(),lastLocation.longitude.toString())
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        Get Temperature
        temperatureCallback = object : SensorEventCallback(){
            override fun onSensorChanged(event: SensorEvent?) {
                val temperature = event?.values?.get(0)
                myViewModel.setTemperature(temperature.toString())
            }
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        sensorManager.registerListener(temperatureCallback,temperatureSensor,20000)


//        Get Pressure
        pressureCallback = object : SensorEventCallback(){
            override fun onSensorChanged(event: SensorEvent?) {
                val pressure = event?.values?.get(0)
                myViewModel.setPressure(pressure.toString())
            }
        }
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(pressureCallback,pressureSensor,20000)


        //        Set data
        val title = intent.getStringExtra("title")
        val startTime = intent.getStringExtra("time")
        myViewModel.init(title,startTime)


        Timer().schedule(object : TimerTask() {
            override fun run() {
                myViewModel.setCurrentTime()
                if (startTime != null) {
                    myViewModel.setDuration()
                }
            }
        }, 0,1000)


        binding.backIcon.setOnClickListener { view ->
            // Do some work here
            NewTripActivity.startFn(this)
            this.finish()
        }

        // Removes take picture button if no cameras are avaliable
        if(Camera.getNumberOfCameras() > 0) {
            binding.takePic.setOnClickListener {
                val intent = Intent(this, CameraActivity::class.java)
                pickFromCamera.launch(intent)
            }
        }else{
            binding.takePic.visibility = View.INVISIBLE
        }

        binding.uploadPic.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }


        binding.stop.setOnClickListener {
            // Do some work here
            myViewModel.returnTitle()
                ?.let { myViewModel.insertTrip(it,myViewModel.returnStartTime(),myViewModel.returnDuration().toString()) }
            TripListActivity.startFn(this)
            this.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(temperatureCallback)
        sensorManager.unregisterListener(pressureCallback)
    }

    override fun onResume() {
        super.onResume()
        refreshLatLon()
        sensorManager.registerListener(temperatureCallback,temperatureSensor,20000)
        sensorManager.registerListener(pressureCallback,temperatureSensor,20000)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    @SuppressLint("MissingPermission")
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

    private fun addPicMarker(latitude:Double,longitude:Double){
        val point = LatLng(latitude, longitude)

        mMap.addMarker(MarkerOptions()
            .position(point)
            .icon(BitmapDescriptorFactory.defaultMarker(180f))
            .alpha(0.5f))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    private fun addDot(latitude:Double,longitude:Double){
        headMarker?.remove()

        val point = LatLng(latitude, longitude)
        headMarker = mMap.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource((R.drawable.blue_dot)))
            .position(point)
            .title("head"))!!
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    private fun drawLine(startLocation:Location,endLocation:Location){
        mMap.addPolyline(
        PolylineOptions()
            .add(LatLng(startLocation.latitude, startLocation.longitude), LatLng(endLocation.latitude, endLocation.longitude))
            .addSpan(StyleSpan(Color.BLUE))
        )
    }

    private fun getWeather(latitude:Double,longitude:Double){
        var url = "https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&appid=bb01585d3dafe1d3b04332150c924d32&units=metric"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                    for ((name, value) in response.headers) {
//                        println("$name: $value")
//                    }
                    val gson = Gson() // Or use new GsonBuilder().create();
                    val parsed: Weather = gson.fromJson(response.body!!.string(), Weather::class.java)
                    val weatherIcon = "https://openweathermap.org/img/wn/${parsed.weather?.get(0)?.icon}@2x.png"
                    val weather = parsed.weather?.get(0)?.main
                    val temp = "${parsed.main?.temp_min}(℃) - ${parsed.main?.temp_max}(℃)"
                    if (weather != null) {
                        myViewModel.setWeather(weatherIcon,weather,temp)
                    }
                    val uiHandler = Handler(Looper.getMainLooper())
                    uiHandler.post(Runnable {
                        Picasso.get()
                            .load(weatherIcon)
                            .fit()
                            .into(binding.weatherIcon)
                    })

                }
            }
        })
    }




}
package com.example.mobilesoftware.view.view

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityTripBinding
import com.example.mobilesoftware.view.dataParse.Weather
import com.example.mobilesoftware.view.service.SensorService
import com.example.mobilesoftware.view.viewmodels.TripViewModel
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

/**
 * An Activity class to handle the view during the trip
 */
class TripActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var myViewModel:TripViewModel
    private lateinit var mMap: GoogleMap
    private val client = OkHttpClient()
    private lateinit var binding: ActivityTripBinding

    //    Location
    private val PERMISSION_LOCATION_GPS:Int = 1
    private var lastLocation: Location? = null
    private var headMarker:Marker? = null

    // Broadcast
    private lateinit var receiver: BroadcastReceiver

    // Temperature & Pressure
    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor?=null
    private lateinit var temperatureCallback: SensorEventCallback
    private var pressureSensor: Sensor?=null
    private lateinit var pressureCallback: SensorEventCallback
    private var tripID: Int = -1


    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let{ uri ->
            // https://developer.android.com/training/data-storage/shared/photopicker#persist-media-file-access
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this@TripActivity.contentResolver.takePersistableUriPermission(uri, flag)

            myViewModel.insertimage(uri)
            lastLocation?.let { it1 -> this.addPicMarker(it1.latitude, it1.longitude) }
        }
    }

    val pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        val photo_uri = result.data?.extras?.getString("uri")

        photo_uri?.let{
            val uri = Uri.parse(photo_uri)
            myViewModel.insertimage(uri)
            lastLocation?.let { it1 -> this.addPicMarker(it1.latitude, it1.longitude) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TripActivity", "onCreate: ")
        super.onCreate(savedInstanceState)

        binding = ActivityTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myViewModel = ViewModelProvider(this)[TripViewModel::class.java]
        binding.viewModel = myViewModel


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            callService()
        }

        // Get data from newTrip activity and update viewModel
        val title = intent.getStringExtra("title")
        val startTime = intent.getStringExtra("time")
        myViewModel.init(title,startTime)


        // Adding map to the view
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Init sensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Handle Temperature update
        temperatureCallback = object : SensorEventCallback(){
            override fun onSensorChanged(event: SensorEvent?) {
                val temperature = event?.values?.get(0)
                myViewModel.setTemperature(temperature.toString())
            }
        }
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        sensorManager.registerListener(temperatureCallback,temperatureSensor,SensorManager.SENSOR_DELAY_NORMAL)

        // Handle Pressure update
        pressureCallback = object : SensorEventCallback(){
            override fun onSensorChanged(event: SensorEvent?) {
                val pressure = event?.values?.get(0)
                myViewModel.setPressure(pressure.toString())
            }
        }
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorManager.registerListener(pressureCallback,pressureSensor,SensorManager.SENSOR_DELAY_NORMAL)

        // Receive Location value from Service and update UI
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Get location from service
                val latitude = intent.getStringExtra("latitude")
                val longitude = intent.getStringExtra("longitude")
                // Instantiate Location
                val location = Location("Location")
                if (latitude != null) {
                    location.latitude = latitude.toDouble()
                }
                if (longitude != null) {
                    location.longitude = longitude.toDouble()
                }

                // Update Path and Current Location
                if (lastLocation != null){
                    drawLine(lastLocation!!,location)
                    addDot(location.latitude,location.longitude)
                }else{
                    // Init Start Point
                    myViewModel.startLocation.set(location)
                    getWeather(location!!.latitude, location!!.longitude)
                    addMarker(location!!.latitude, location!!.longitude)
                    addDot(location!!.latitude, location!!.longitude)
                }
                lastLocation = location
                myViewModel.setLocation(lastLocation!!.latitude.toString(), lastLocation!!.longitude.toString())
                myViewModel.insertLocation(lastLocation!!.latitude.toString(), lastLocation!!.longitude.toString())

                Log.d("TripActivity", "latitude: $latitude, longitude: $longitude")
            }
        }
        val filter = IntentFilter("update-ui")
        registerReceiver(receiver, filter)

        // Keep update the timer every second
        Timer().schedule(object : TimerTask() {
            override fun run() {
                myViewModel.setCurrentTime()
                if (startTime != null) {
                    myViewModel.setDuration()
                }
            }
        }, 0,1000)

        // Handle backIcon
        binding.backIcon.setOnClickListener { view ->
//            stopService()
//            unregisterReceiver(receiver)
            sensorManager.unregisterListener(temperatureCallback)
            sensorManager.unregisterListener(pressureCallback)
            NewTripActivity.startFn(this)
            finish()
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

        // Handle uploadPic button
        binding.uploadPic.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Handle stop button
        binding.stop.setOnClickListener {
//            stopService()
//            unregisterReceiver(receiver)
            myViewModel.returnTitle()
                ?.let { myViewModel.insertTrip(it,myViewModel.returnStartTime(),myViewModel.returnDuration().toString()) }
            TripListActivity.startFn(this)
            finish()
        }
    }

    override fun onPause() {
        Log.d("TripActivity", "onPause: ")
        super.onPause()
    }

    override fun onResume() {
        Log.d("TripActivity", "onResume: ")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("TripActivity", "onDestroy: ")
        super.onDestroy()
        stopService()
        unregisterReceiver(receiver)
        sensorManager.unregisterListener(temperatureCallback)
        sensorManager.unregisterListener(pressureCallback)
    }

    /**
     * called when a map has been successfully loaded and is ready to be used
     *
     * @param googleMap
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    /**
     * function to call the Location Service to start
     */
    private fun callService(){
        Intent(this, SensorService::class.java).apply {
            startService(this)
        }
    }

    /**
     * function to stop Service when is no longer needed
     */
    private fun stopService(){
        Intent(this, SensorService::class.java).apply {
            stopService(this)
        }

    }

    /**
     * function to add marker for start point in the map
     *
     * @param latitude of the location
     * @param longitude of the location
     */
    private fun addMarker(latitude:Double,longitude:Double){
        val point = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(point))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    /**
     * function to add marker for pictures in the map
     *
     * @param latitude of the location
     * @param longitude of the location
     */
    private fun addPicMarker(latitude:Double,longitude:Double){
        val point = LatLng(latitude, longitude)

        mMap.addMarker(MarkerOptions()
            .position(point)
            .icon(BitmapDescriptorFactory.defaultMarker(180f))
            .alpha(0.5f))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    /**
     * function to update current location representation
     *
     * @param latitude of the location
     * @param longitude of the location
     */
    private fun addDot(latitude:Double,longitude:Double){
        headMarker?.remove()

        val point = LatLng(latitude, longitude)
        headMarker = mMap.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource((R.drawable.blue_dot)))
            .position(point)
            .title("head"))!!
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    /**
     * function to draw line between two location
     *
     * @param startLocation
     * @param endLocation
     */
    private fun drawLine(startLocation:Location,endLocation:Location){
        mMap.addPolyline(
        PolylineOptions()
            .add(LatLng(startLocation.latitude, startLocation.longitude), LatLng(endLocation.latitude, endLocation.longitude))
            .addSpan(StyleSpan(Color.BLUE))
        )
    }

    /**
     * An Asynchronous http Request function for weather
     *
     * @param startLocation
     * @param endLocation
     */
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
                    val gson = Gson()
                    val parsed: Weather = gson.fromJson(response.body!!.string(), Weather::class.java)
                    val weatherIcon = "https://openweathermap.org/img/wn/${parsed.weather?.get(0)?.icon}@2x.png"
                    val weather = parsed.weather?.get(0)?.main
                    val temp = "${parsed.main?.temp_min}(℃)   -   ${parsed.main?.temp_max}(℃)"
                    val avgTemp = (parsed.main?.temp_max?.minus(parsed.main?.temp_min!!))?.div(2)
                    if (weather != null) {
                        myViewModel.setWeather(weatherIcon,weather,temp)
                        myViewModel.temperature.set(String.format("%.2f", avgTemp))
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
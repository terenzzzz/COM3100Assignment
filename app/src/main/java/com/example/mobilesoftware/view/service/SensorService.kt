package com.example.mobilesoftware.view.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.mobilesoftware.R
import com.example.mobilesoftware.view.view.TripActivity
import com.google.android.gms.location.*
import java.util.*

class SensorService : LifecycleService() {
    private val CHANNEL_ID = "notification channel id"
    private var number = 0

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        Log.d("service", "onCreate: ")
        super.onCreate()
        createChannel()
        val pendingIntent = createPendingIntent()
        val notification = pendingIntent?.let { createNotification(it) }
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("service", "onStartCommand: Called")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0?:return
                for (location in p0.locations){
                    lastLocation = location
                    Log.d("service", "lastLocation: ${location.latitude},${location.longitude} ")
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d("service", "fusedLocationClient: init")
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            var locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
                10000
            ).build()
            Log.d("service", "fusedLocationClient: requestLocationUpdates")
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }


    private fun createPendingIntent(): PendingIntent? {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, TripActivity::class.java),
            0
        )
        return pendingIntent
    }

    private fun createNotification(pendingIntent: PendingIntent): Notification {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("We are keep tracking your location...")
            .setContentText("Click here to come back")
            .setContentIntent(pendingIntent)
            .build()
        return notification
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "Notification Channel Name"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}

//        Timer().schedule(object : TimerTask() {
//            override fun run() {
//                number++
//                Log.d("service", "run: ${number}")
//            }
//        }, 0, 1000)
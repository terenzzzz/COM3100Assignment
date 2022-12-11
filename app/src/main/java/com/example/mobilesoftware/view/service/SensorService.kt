package com.example.mobilesoftware.view.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.mobilesoftware.R
import com.example.mobilesoftware.view.view.TripActivity
import java.util.*

class SensorService : LifecycleService() {
    private val CHANNEL_ID = "notification channel id"
    private var number = 0

    override fun onCreate() {
        super.onCreate()
        Timer().schedule(object : TimerTask(){
            override fun run() {
                number++
                Log.d("Testing", "run: ${number}")
            }
        },0,1000)
        createChannel()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this,TripActivity::class.java),
        0
        )
        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("This is a Title")
            .setContentText("This is a notification")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1,notification)
    }
    
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        TODO("Return the communication channel to the service.")
    }

    private fun createChannel(){
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
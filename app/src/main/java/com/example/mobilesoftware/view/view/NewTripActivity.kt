package com.example.mobilesoftware.view.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.mobilesoftware.databinding.ActivityNewTripBinding
import com.example.mobilesoftware.view.service.SensorService
import com.example.mobilesoftware.view.viewmodels.NewTripViewModel
import java.util.*

class NewTripActivity : AppCompatActivity() {
    var myViewModel = NewTripViewModel()

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

        Intent(this, SensorService::class.java).apply {
            startService(this)
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
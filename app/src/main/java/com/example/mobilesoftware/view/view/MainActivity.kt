package com.example.mobilesoftware.view.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.mobilesoftware.databinding.ActivityMainBinding
import com.example.mobilesoftware.view.viewmodels.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    var myViewModel = MainViewModel()

    companion object {
        fun startFn(context: Context) {
            val intent =
                Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = myViewModel
        myViewModel.onCreate()

        Timer().schedule(object : TimerTask() {
            override fun run() {
                myViewModel.init()
            }
        }, 0,1000)

        binding.start.setOnClickListener(View.OnClickListener { view ->
            // Do some work here
            val title = binding.etTitle.text.toString()

            val intent = Intent(this,TripActivity::class.java);
            intent.putExtra("title", title);
            startActivity(intent);

            this.finish()
        })
    }
}
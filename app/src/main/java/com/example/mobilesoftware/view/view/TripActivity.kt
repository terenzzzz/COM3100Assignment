package com.example.mobilesoftware.view.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.mobilesoftware.R
import com.example.mobilesoftware.databinding.ActivityTripBinding
import com.example.mobilesoftware.view.viewmodels.TripViewModel

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

        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()

        val map = MapsFragment()
        transaction.add(R.id.fl_map, map);
        transaction.commit();

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
package com.example.mobilesoftware.view.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilesoftware.R
import com.example.mobilesoftware.view.TripAppCompatActivity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.model.TripElement
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * TripListAcitvity uses TripAppCompatAcitvity in order to inherit the functionality of its
 * related repistory and conects the acitivty to its related ViewModel in a simpler manner
 *
 * This portion of code handles the opening page which displays the Trips held in the
 * database to be inspected.
 */

class TripListActivity : TripAppCompatActivity() {
    private val NUMBER_OF_COLUMNS = 1
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripAdapter
    private var adapterData: MutableList<Image>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_triplist)

        // Check needed permissions are granted, otherwise request them
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Checks the preferences for sorting trips
        val sharedPref = this@TripListActivity.getPreferences(Context.MODE_PRIVATE)
        val sortByDateSwitch : SwitchCompat = findViewById(R.id.switch1)
        if(sharedPref.getInt("sort",0) == 1){
            sortByDateSwitch.isChecked = true
            sortByDateSwitch.text = "Sorted by oldest"
            changeSort(sharedPref.getInt("sort",0))
        }


        // Set up the adapter - easier with ListAdapter and observing of the data from the ViewModel
        recyclerView = findViewById(R.id.rctriplist)
        adapter = TripAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, NUMBER_OF_COLUMNS)

        // start observing the date from the ViewModel
        tripListViewModel.trips.observe(this) {
            // update the dataset used by the Adapter
            it?.let {
                adapter.submitList(it)
            }
        }

        // Assigns listener to FAB to start a new trip
        val newTripFab : FloatingActionButton = findViewById(R.id.newTripFab)
        newTripFab.setOnClickListener {
            val int = Intent(this, NewTripActivity::class.java)
            startActivity(int)
        }

        // Button that leads to browsing all images
        val browseAllBut : Button = findViewById(R.id.allPics)
        browseAllBut.setOnClickListener {
            val intent = Intent(this, ImageListActivity::class.java)
            intent.putExtra("id", -1)
            startActivity(this, intent, null)
        }

        // listens to sort switch
        sortByDateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sorting(1, sharedPref)
                sortByDateSwitch.text = "Sorted by oldest"
            } else {
                sortByDateSwitch.text = "Sorted by latest"
                sorting(0, sharedPref)
            }
            recreate()
        }
    }


    /**
     * Changes the preferences for TripList and then begins the function of
     * telling the view model to change how the values are sorted
     */
    private fun sorting(setting : Int, sharedPref: SharedPreferences){
        val editor = sharedPref.edit()
        editor.putInt("sort",setting)
        editor.apply()
        changeSort(setting)
    }

    // Informs view model of change of sort and changes the LiveData
    private fun changeSort(setting : Int){
        tripListViewModel.sorting(setting)
    }

    // Called in onCreate to check if permissions have been granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // called to request permissions
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(this,
                    "All permissions granted by the user.",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "Not all permissions granted by the user.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val TAG = R.string.app_name.toString()
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSIONS = 10

        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(Manifest.permission.ACCESS_MEDIA_LOCATION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }.toTypedArray()

        fun startFn(context: Context) {
            val intent =
                Intent(context, TripListActivity::class.java)
            context.startActivity(intent)
        }
    }

    /**
     * Unlike the previous implementation, this extends a ListAdapter (instead of a
     * RecycleView.Adapter. A ListAdapter is better suited for dynamic list (with LiveData).
     * Extending a ListAdapter also makes it easier to sort the items in the list,
     * though you will need to implement a suitable comparator.
     *
     * See: https://stackoverflow.com/questions/66485821/list-adapter-vs-recycle-view-adapter
     * https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
     *
     * Declaring the ImageAdapter as an inter class in this implementation keeps
     * closely (and naturally) related code together. The ImageAdapter class after all is
     * a class that is part of the view. If the Adapter class is big, you may want to reconsider this.
     * In this implementation, such practice also makes it easier to handle the onClick for each of
     * the Holder items, because an inter class can access the outer class' members. This helps
     * even further to logically organize the code better.
     */
    class TripAdapter: ListAdapter<TripElement, TripAdapter.TripViewHolder>(TripViewHolder.TripComparator()) {
        // Notice we no longer are passing the context in the constructor and we don't need a
        // constructor either because all that was all part of setting up the boiler
        // plate coding needed to ensure we can notify of data changes.
        lateinit var context: Context


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            context = parent.context
            return TripViewHolder.create(parent)
        }

        override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
            // All the logic in the onBindViewHolder that is handling
            // thumbnail processing are not suitable inside a view related class and have
            // been moved into the repository, making the view related classes much cleaner.
            holder.bind(getItem(position), position, context)
        }

        class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleView: TextView = itemView.findViewById<View>(R.id.titleTrip) as TextView
            private val dateView: TextView = itemView.findViewById<View>(R.id.dateTrip) as TextView
            private val timeView: TextView = itemView.findViewById<View>(R.id.timeTrip) as TextView

            @SuppressLint("SetTextI18n")
            fun bind(trip: TripElement, position: Int, context: Context){

                titleView.text = trip.title
                dateView.text = trip.date.toString()
                timeView.text = "Duration: ${trip.time}"

                itemView.setOnClickListener {
                    onViewHolderItemClick(trip.id, position, context)
                }
            }

            companion object{
                internal fun create(parent: ViewGroup): TripViewHolder {
                    //Inflate the layout, initialize the View Holder
                    val view: View = LayoutInflater.from(parent.context).inflate(
                        R.layout.list_item_trip,
                        parent, false
                    )
                    return TripViewHolder(view)
                }

                /**
                 * onClick listener for the Adapter's ImageViewHolder item click
                 * No need to have an ActivityResultContract because that was
                 * all part of the boiler plate to handle data changes
                 */
                protected fun onViewHolderItemClick(id: Int, position: Int, context: Context) {
                    val intent = Intent(context, ImageListActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("position", position)
                    startActivity(context,intent,null)
                }
            }

            /**
             * Comparator class used by the ListAdapter.
             */
            class TripComparator : DiffUtil.ItemCallback<TripElement>() {
                override fun areItemsTheSame(oldTrip: TripElement, trip: TripElement): Boolean {
                    return oldTrip.id == trip.id
                }

                override fun areContentsTheSame(oldTrip: TripElement, trip: TripElement): Boolean {
                    return oldTrip == trip
                }
            }
        }

    }
}
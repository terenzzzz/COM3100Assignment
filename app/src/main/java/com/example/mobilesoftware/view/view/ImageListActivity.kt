package com.example.mobilesoftware.view.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilesoftware.R
import com.example.mobilesoftware.view.ImageAppCompatActivity
import com.example.mobilesoftware.view.model.Image
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text

// Note the use of ImageAppCompatActivity - which is a custom class that simply inherits
// the Android AppCompatActivity class and provides the ImageViewModel as a property (DRY)
class ImageListActivity : ImageAppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var tripID = -1
    private var adapterData: MutableList<Image>? = null

    val showImageActivityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        result?.let{
            val update_op = it.data?.extras?.getBoolean("updated")
            update_op?.apply {
                if(update_op == true){
                    Snackbar.make(/* view = */ recyclerView,
                        /* text = */ "Image detail updated.",
                        /* duration = */ Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_images)

        val display = windowManager.defaultDisplay.width
        // set number of cols to how many can fit depending on width of
        val numberOfCols = (display / 250).toInt()

        // Checks for sorting preference
        val sharedPref = this@ImageListActivity.getPreferences(Context.MODE_PRIVATE)
        val sortByDateSwitch : SwitchCompat = findViewById(R.id.switch1)
        if(sharedPref.getInt("sort",0) == 1){
            sortByDateSwitch.isChecked = true
            sortByDateSwitch.text = "Sorted by Descending"
            changeSort(sharedPref.getInt("sort",0))
        }

        // Set up the adapter - easier with ListAdapter and observing of the data from the ViewModel
        recyclerView = findViewById<RecyclerView>(R.id.my_list)
        adapter = ImageAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, numberOfCols)

        // Checks if is being loaded with TripID intent
        val bundle: Bundle? = intent.extras
        tripID = bundle?.getInt("id")!!
        if (tripID != null) {
            imageViewModel.filter(tripID,sharedPref.getInt("sort",0))
        }

        // start observing the date from the ViewModel
        imageViewModel.images.observe(this) {
            // update the dataset used by the Adapter
            it?.let {
                adapter.submitList(it)
            }
        }

        // Listener for sorting switch
        sortByDateSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                sorting(1,sharedPref)
                sortByDateSwitch.text = "Sorted by Descending"
            }else {
                sortByDateSwitch.text = "Sorted by Ascending"
                sorting(0, sharedPref)
            }
            recreate()
        })
    }

    /**
     * Changes the prefrences for ImageList and then begins the function of
     * telling the viewmodel to change how the values are sorted
     **/
    fun sorting(setting : Int, sharedPref: SharedPreferences){
        val editor = sharedPref.edit()
        editor.putInt("sort",setting)
        editor.commit()
        changeSort(setting)
    }

    // Informs viewmodel of change of sort and changes the LiveData
    private fun changeSort(setting : Int){
        imageViewModel.filter(tripID,setting)
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
    class ImageAdapter: ListAdapter<Image, ImageAdapter.ImageViewHolder>(ImageAdapter.ImageViewHolder.ImageComparator()) {
        // Notice we no longer are passing the context in the constructor and we don't need a
        // constructor either because all that was all part of setting up the boiler
        // plate coding needed to ensure we can notify of data changes.
        lateinit var context: Context


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            context = parent.context
            return ImageViewHolder.create(parent)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            // All the logic in the onBindViewHolder that is handling
            // thumbnail processing are not suitable inside a view related class and have
            // been moved into the repository, making the view related classes much cleaner.
            holder.bind(getItem(position), position, context)
        }

        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById<View>(R.id.image_item) as ImageView
            private val imgTView: TextView = itemView.findViewById<View>(R.id.imgitmtit) as TextView

            fun bind(image: Image, position: Int, context: Context){

                imageView.setImageURI(image.thumbnail)
                imgTView.text = image.title

                itemView.setOnClickListener(View.OnClickListener {
                    // the listener is implemented in MainActivity
                    // but this function delegate allows invocation
                    onViewHolderItemClick(image.id, position, context)
                })
            }

            companion object{
                internal fun create(parent: ViewGroup): ImageViewHolder {
                    //Inflate the layout, initialize the View Holder
                    val view: View = LayoutInflater.from(parent.context).inflate(
                        R.layout.list_item_image,
                        parent, false
                    )
                    return ImageViewHolder(view)
                }

                /**
                 * onClick listener for the Adapter's ImageViewHolder item click
                 * No need to have an ActivityResultContract because that was
                 * all part of the boiler plate to handle data changes
                 */
                protected fun onViewHolderItemClick(id: Int, position: Int, context: Context) {
                    val intent = Intent(context, ShowImageActivity::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("position", position)
                    (context as ImageListActivity).showImageActivityResultContract.launch(intent)
                }
            }

            /**
             * Comparator class used by the ListAdapter.
             */
            class ImageComparator : DiffUtil.ItemCallback<Image>() {
                override fun areItemsTheSame(oldImage: Image, newImage: Image): Boolean {
                    return oldImage.id === newImage.id
                }

                override fun areContentsTheSame(oldImage: Image, newImage: Image): Boolean {
                    return oldImage.equals(newImage)
                }
            }
        }

    }
}
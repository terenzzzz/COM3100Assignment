package com.example.mobilesoftware.view.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Transformations
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Note the use of ImageAppCompatActivity - which is a custom class that simply inherits
// the Android AppCompatActivity class and provides the ImageViewModel as a property (DRY)
class ImageListActivity : ImageAppCompatActivity() {
    val NUMBER_OF_COLOMNS = 3
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private var adapterData: MutableList<Image>? = null

    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let{ uri ->
            // https://developer.android.com/training/data-storage/shared/photopicker#persist-media-file-access
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            this@ImageListActivity.contentResolver.takePersistableUriPermission(uri, flag)

            imageViewModel.insert(
                image_uri = uri)
        }
    }

    val pickFromCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        val photo_uri = result.data?.extras?.getString("uri")

        photo_uri?.let{
            val uri = Uri.parse(photo_uri)

            imageViewModel.insert(
                image_uri = uri)
        }
    }

    val showImageActivityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        result?.let{
            val position = it.data?.extras?.getInt("position") ?: -1 // position not used, but may be useful in some cases
            val delete_op = it.data?.extras?.getBoolean("deletion")
            val update_op = it.data?.extras?.getBoolean("updated")
            delete_op?.apply {
                if(delete_op == true){
                    Snackbar.make(/* view = */ recyclerView,
                        /* text = */ "Image deleted.",
                        /* duration = */ Snackbar.LENGTH_LONG)
                        .show()
                }
            }
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

        // Check needed permissions are granted, otherwise request them
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the adapter - easier with ListAdapter and observing of the data from the ViewModel
        recyclerView = findViewById<RecyclerView>(R.id.my_list)
        adapter = ImageAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, NUMBER_OF_COLOMNS)

        val bundle: Bundle? = intent.extras
        val tripID = bundle?.getInt("id")

        // start observing the date from the ViewModel
        imageViewModel.images.observe(this) {
            // update the dataset used by the Adapter
            it?.let {
                if (tripID != null) {
                    imageViewModel.filter(tripID)
                }
                adapter.submitList(it)
            }
        }

        // Setup a photo picker Activity to be started when the openGalleryFab button is clicked
        // The ActivityResultContract, photoPicker, will handle the result when the photo picker Activity returns
        val photoPickerFab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.openGalleryFab)
        photoPickerFab.setOnClickListener(View.OnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        })

        // Setup the CameraActivity to be started when the openCamFab button is clicked
        // The ActivityResultContract, pickFromCamera, will handle the result when the CameraActivity returns
        val cameraPickerFab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.openCamFab)
        cameraPickerFab.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            pickFromCamera.launch(intent)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
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

            fun bind(image: Image, position: Int, context: Context){

                imageView.setImageURI(image.thumbnail)

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
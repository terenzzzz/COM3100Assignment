package com.example.mobilesoftware.view.view



import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.example.mobilesoftware.databinding.ActivityShowImageBinding
import com.example.mobilesoftware.view.ImageAppCompatActivity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.utils.deleteThumbnail
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobilesoftware.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.concurrent.schedule

class   ShowImageActivity  : ImageAppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityShowImageBinding
    private lateinit var mMap: GoogleMap
    //var myViewModel: ShowImageViewModel()

    // This class didn't change so much as the other classes serve to show the examples intended well enough
    // Still, you should pay attention to the relevant changes.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent is a property of the activity. intent.extras returns any data that was pass
        // along with the intent.
        val bundle: Bundle? = intent.extras

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fl_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (bundle!= null) {
            val imageId = bundle.getInt("id")
            val position = bundle.getInt("position")

            if (imageId > 0) {
                // Observe the image data, only one will be received

                imageViewModel.getImage(imageId).observe(this){
                    if(it != null){
                        val image = it
                        // Display the model's data in the view. This is a lot of back and forth!
                        loadImageView(image.imagePath.toString())

                        binding.editTextTitle.setText(image.title)
                        binding.editTextDescription.setHint("Enter Description")
                        image.description?.isNotEmpty().apply {
                            binding.editTextDescription.setText(image.description)
                        }
                        // onClick listener for the update button
                        binding.buttonSave.setOnClickListener {
                            onUpdateButtonClickListener(it, image, position)
                        }
                        val trip=imageViewModel.getTrip(image.tripID!!)
                        trip.observe(this){
                            if(it !=null){
                                val theTrip= it
                                println (theTrip.title)
                                binding.tripTitle.text=theTrip.title
                            }

                        }
                        binding.pressure.text = image.pressure+ " hpa"
                        binding.temperature.text=image.temperature
                        binding.date.text=image.date.toString()

                        println(image.latitude)
                        println(image.longitude)


                        //addMarker(image.latitude!!.toDouble(),image.longitude!!.toDouble())
                        imageViewModel.filter(image.tripID!!,0)

                        // start observing the date from the ViewModel
                        imageViewModel.images.observe(this) {


                            val theImages= it
                            println("helloooooooo"+theImages[0].pressure)
                            for (i in theImages){
                                if (i.id==imageId){
                                addMarkerCurrent(i.latitude!!.toDouble(),i.longitude!!.toDouble())
                                println(i.title)}
                                else{
                                    addMarker(i.latitude!!.toDouble(),i.longitude!!.toDouble(),i.title)
                                }
                            }
                        }
                        imageViewModel.getLocationsByTripID(image.tripID!!)
                        imageViewModel.locations.observe(this){
                            val locations=it
                            for (location in locations){
                                println("location dddddddddd"+location.latitude)


                        }

                        }




                    }
                }
            }
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
    private fun addMarkerLocation(latitude:Double,longitude:Double,imageTitle:String){
        val point = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource((R.drawable.blue_dot))))
    }
    private fun addMarker(latitude:Double,longitude:Double,imageTitle:String){
        val point = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(imageTitle))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    private fun addMarkerCurrent(latitude:Double,longitude:Double){
        val point = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(point).title("This Image"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }
    /**
     * This function will either use the file path to load the image by default
     * Or can load from MediaStore when defaultToPath is false and the
     * path host contains "com.android".
     */
    private fun loadImageView(image_path: String, defaultToPath: Boolean = true){
        if(defaultToPath){
            loadImageViewWithPath(image_path)
        }else{
            val uri = Uri.parse(image_path)
            val host = uri.host ?: "media"
            val id = uri.lastPathSegment?.split(":")?.get(1) ?: ""

            if(host.startsWith("com.android")){
                runBlocking {
                    loadImageViewWithMediaStore(id)
                }
            }else{
                loadImageViewWithPath(image_path)
            }
        }
    }

    /**
     * function that loads images based on the image's file path only
     */
    private fun loadImageViewWithPath(path: String){
        binding.image.setImageURI(Uri.parse(path))
    }

    /**
     * function that loads images from media store. Queries for the image
     * using the id. Note, image may no longer exists in storage, or
     * might have been backed up to Google Photos, which might take a while to
     * retrieve. Retrieval will fail if there is no Internet connection.
     *
     * This is a basic media store access implementation. Media store query works
     * similar to running an SQL query
     */
    private suspend fun loadImageViewWithMediaStore(id: String){
        if(id.isEmpty() or !id.isDigitsOnly()){
            Snackbar.make(binding.image, "Unable to load image. Image not found.", Snackbar.LENGTH_LONG)
                .show()
            return
        }

        var current_access_uri: Uri? = null
        // Specify the columns that should be returned in the media store query result
        val projection = arrayOf(MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        withContext(Dispatchers.IO){
            contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Media._ID + " = $id",
                null,
                null)?.use {cursor ->
                if(cursor.moveToFirst()){
                    // Gets a current URI for the media store item
                    current_access_uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
                }
            }
        }
        current_access_uri?.let{
            binding.image.setImageURI(current_access_uri)
        }
    }

    private fun onUpdateButtonClickListener(view: View, image: Image, position: Int){
        // Update the data in the model back. This is a lot of work, back and forth!
        image.title = binding.editTextTitle.text.toString()
        image.description = binding.editTextDescription.text.toString()

        imageViewModel.update(image)

        // Start an intent to let the calling activity know an update has happened.
        val intent = Intent(this@ShowImageActivity, TripListActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("updated",true)
        setResult(RESULT_OK,intent)
        finish()
    }

    private fun onDeleteButtonClickListener(view: View, image: Image, position: Int){
        image.deleteThumbnail(this@ShowImageActivity)
        imageViewModel.delete(image)

        // Start an intent to let the calling activity know a delete has happened.
        val intent = Intent(this@ShowImageActivity, TripListActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("deletion",true)
        setResult(RESULT_OK,intent)
        finish()

        // Start an intent to let the calling activity know a delete has happened.
    }
}
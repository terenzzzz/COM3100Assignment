package com.example.mobilesoftware.view.view

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.text.isDigitsOnly
import com.example.mobilesoftware.databinding.ActivityShowImageBinding
import com.example.mobilesoftware.view.ImageAppCompatActivity
import com.example.mobilesoftware.view.model.Image
import com.example.mobilesoftware.view.utils.deleteThumbnail
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.example.mobilesoftware.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class   ShowImageActivity  : ImageAppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityShowImageBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent is a property of the activity. intent.extras returns any data that was pass
        // along with the intent.
        val bundle: Bundle? = intent.extras

        //add the map when it is ready to add
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fl_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (bundle!= null) {
            val imageId = bundle.getInt("id")
            val position = bundle.getInt("position")
            val tripId = bundle.getInt("tripID")

            if (imageId > 0) {
                // Observe the image data, only one will be receive
                imageViewModel.getLocationsByTripID(tripId) //filtering locations for the current trip only
                //Observe image meta data
                imageViewModel.getImage(imageId).observe(this){
                    if(it != null){
                        val image = it
                        loadImageView(image.imagePath.toString())
                        //bind image title and description to view
                        binding.editTextTitle.setText(image.title)
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
                                //bind the trip title to the view
                                binding.tripTitle.text=theTrip.title
                            }

                        }
                        //bind date,pressure, temperature to view
                        binding.pressure.text = image.pressure+ " hPa"
                        binding.temperature.text=image.temperature+ " \u2103"
                        binding.date.text=image.date.toString()
                        imageViewModel.filter(image.tripID!!,0)
                        // start observing the other images on the trip from the ViewModel
                        imageViewModel.images.observe(this) {
                            if(it != null){
                                val theImages= it
                                for (image in theImages){
                                    if (image.id==imageId){
                                        //add marker for current image
                                    addMarkerCurrent(image.latitude!!.toDouble(),image.longitude!!.toDouble())
                                    }
                                    else{
                                        //add marker of different colour for other images on the trip
                                        addMarker(image.latitude!!.toDouble(),image.longitude!!.toDouble(),image.title)
                                    }
                                }
                            }
                        }
                        // start observing the locations on the trip from the ViewModel
                        imageViewModel.locations.observe(this) {
                            if(it != null) {
                                val locations = it
                                //add green dot marker to start of the path
                                addMarkerLocation(
                                    locations[0].longitude.toDouble(),
                                    locations[0].latitude.toDouble(),
                                    "start"
                                )
                                //add red dot marker to the end of the path
                                addMarkerLocation(
                                    locations[locations.size - 1].longitude.toDouble(),
                                    locations[locations.size - 1].latitude.toDouble(),
                                    "end"
                                )
                                //blue dot marker for all locations between the start and end
                                for (location in 1..locations.size - 2) {
                                    addMarkerLocation(
                                        locations[location].longitude.toDouble(),
                                        locations[location].latitude.toDouble(),
                                        "middle"
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Initialises the google map
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
    /**
     * Function that adds a dot marker to the map for the GPS location provided
     * A red, green or blue marker on google map depending on start, end or other type of location
     */
    private fun addMarkerLocation(latitude:Double,longitude:Double,type:String) {
        val point = LatLng(latitude, longitude)
        //green dot for start location of the trip
        if (type == "start") {
            mMap.addMarker(
                MarkerOptions().position(point)
                    .icon(BitmapDescriptorFactory.fromResource((R.drawable.green_dot)))
            )
        }
        //red dot for end location of the trip
        else if (type=="end"){
            mMap.addMarker(
                MarkerOptions().position(point)
                    .icon(BitmapDescriptorFactory.fromResource((R.drawable.red_dot)))
            )
        }
        //blue dot for any location in between the start and end location
        else{
            mMap.addMarker(
                MarkerOptions().position(point)
                    .icon(BitmapDescriptorFactory.fromResource((R.drawable.blue_dot)))
            )

        }

    }

    /**
     * Function that adds a green location marker to the map for the GPS location provided.
     * The marker is currently used for other images on the same path when displaying image details
     */
    private fun addMarker(latitude:Double,longitude:Double,imageTitle:String){
        val point = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(imageTitle))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15f))
    }

    /**
     * Function that adds a red location marker to the map for the GPS location provided.
     * The marker is currently used to mark the location of the selected image on the path when displaying image details
     */
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
    //Listener for the save changes button, saves the updated title and/or description.
    private fun onUpdateButtonClickListener(view: View, image: Image, position: Int){
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
    //delete button has since been removed, code is useful below if the delete button is added to the app in the future.
    private fun onDeleteButtonClickListener(view: View, image: Image, position: Int){
        image.deleteThumbnail(this@ShowImageActivity)
        imageViewModel.delete(image)

        // Start an intent to let the calling activity know a delete has happened.
        val intent = Intent(this@ShowImageActivity, TripListActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("deletion",true)
        setResult(RESULT_OK,intent)
        finish()

    }
}
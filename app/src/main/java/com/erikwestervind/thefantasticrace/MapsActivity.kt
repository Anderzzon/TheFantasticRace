package com.erikwestervind.thefantasticrace

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.JobIntentService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    lateinit var geofencingClient: GeofencingClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    var geofences = listOf<Geofence>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                //placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                //println("!!! " + lastLocation.latitude + " " + lastLocation.longitude)

            }
        }
        createLocationRequest()

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        // Add a marker in Luma and move the camera
//        val luma = LatLng(59.304568, 18.094541)
//        map.addMarker(MarkerOptions().position(luma).title("Marker in Luma"))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(luma, 12.0f))

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)

        geofencingClient = LocationServices.getGeofencingClient(this)

        geofences = DataManager.locations.map {
            println("!!! Geonfece created: " + it.name)
            Geofence.Builder()
                .setRequestId(it.name)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(it.coord.latitude, it.coord.longitude, 200.0f)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        }
        geofences.forEach {
            println("!!! Geofence in geofences: " + it.requestId)
        }

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofences)
            println("!!! Geofence Request")
        }.build()

        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this, GeofenceReceiver::class.java)
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        geofencingClient = LocationServices.getGeofencingClient(this)

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnFailureListener {
                println("!!! Error adding intent")
            }
            addOnSuccessListener {
                //map.addMarker(MarkerOptions().position(luma).title("Marker in Luma"))
                //println("!!! Intent added")
            }
            println("!!! Request and Intent added")
        }

        setUpMap()

    }

        override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {

        // Required if your app targets Android 10 or higher.
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            if (true) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                // Show an explanation to the user as to why your app needs the
                // permission. Display the explanation *asynchronously* -- don't block
                // this thread waiting for the user's response!
            }
        } else {
            // Background location runtime permission already granted.
            // You can now call geofencingClient.addGeofences().
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        map.addMarker(markerOptions)
    }

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
        println("!!! " + fusedLocationClient.lastLocation)

    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 5000
        // 3
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceReceiver::class.java)
        PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }



    private fun handleEvent(event: GeofencingEvent) {
        // 1
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // 2
            println("!!! You entered!")
//            val reminder = getFirstReminder(event.triggeringGeofences)
//            val message = reminder?.message
//            val latLng = reminder?.latLng
//            if (message != null && latLng != null) {
//                // 3
//                sendNotification(this, message, latLng)
//            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }
}




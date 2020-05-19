package com.erikwestervind.thefantasticrace

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

const val SHOW_NEXT_STOP_DIRECT = 0
const val SHOW_NEXT_STOP_WITH_DELAY = 1
const val GAME_ID_KEY = "GAME_ID"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    lateinit var geofencingClient: GeofencingClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var gameInfo: GameInfo //Remove
    lateinit var gameId: String
    var gameLocations = mutableListOf<GameLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                lastLocation = locationResult.lastLocation

                val user = auth.currentUser
                val locationRef = db.collection("users").document(user!!.uid)
                locationRef
                    .update("latitude", lastLocation.latitude)
                locationRef
                    .update("longitude", lastLocation.longitude)
                //placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
                //println("!!! " + lastLocation.latitude + " " + lastLocation.longitude)
            }
        }

        //gameId = "q6ou5AIikGUM5tSOY1Bw" // Later, create function that changes this dynamically to the game you are in
        gameId = intent.getStringExtra(GAME_ID_KEY)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        DataManager.locations
        DataManager.markerOptions
        DataManager.markers
        DataManager.circlesOptions
        DataManager.circles

        //Will be moved to create game activity later:
        //addToFirebase()

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

        getGameInfo()
        //getLocations()

        setUpMap()
    }

    private fun getGameInfo() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("races_invited")
            .whereEqualTo("parent_race", gameId)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    val game = document.toObject(GameInfo::class.java)
                    if (game != null) {
                        gameInfo =game //Remove
                        DataManager.gameInfo = game
                        setTitle(gameInfo.name!!.capitalize())
                        println("!!! Game info: ${game}")
                }
                    //Get and update locations when the game info is collected
                    getLocations()

            }
        }.addOnFailureListener { exception ->
                println("!!! get failed with  ${exception}")
            }
    }

    private fun crateGame() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("races").document(gameId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val game = document.toObject(GameInfo::class.java)
                    if (game != null) {
                        //Do something
                    }
                }
            }.addOnFailureListener { exception ->
                println("!!! get failed with  ${exception}")
            }
    }

    private fun getLocations() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("places")
            .whereEqualTo("race", gameId)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                gameLocations.clear()
                DataManager.locations.clear()
                DataManager.markerOptions.clear()
                DataManager.markers.clear()
                DataManager.circlesOptions.clear()
                DataManager.circles.clear()
                map.clear() //Testing
                for(document in documents) {
                    val newStop = document.toObject(GameLocation::class.java)

                    if(newStop != null) {
                        newStop.id = document.id

                        gameLocations.add(newStop)
                        DataManager.locations.add(newStop)

                        val location = LatLng(newStop.latitude!!, newStop.longitude!!)
                        val markerOption = MarkerOptions()
                            .position(location)
                            .visible(false)
                            .snippet(newStop.id)
                        DataManager.markerOptions.add(markerOption)

                        val radius = gameInfo.radius
                        val circleOption = CircleOptions()
                            .center(location)
                            //.radius(100.0)
                            .radius(radius!!)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(0.0f)
                            .fillColor(0x220000FF)
                            .visible(false)

                        DataManager.circlesOptions.add(circleOption)
                        println("!!! ${newStop}")
                        //placeMarkerOnMap(LatLng(newStop.latitude!!, newStop.longitude!!))
                    }
                }
                println("!!! Antal locations: ${DataManager.locations.size}")
                addMarkersToMap()
                addGeofenceCircle()

                //StartGame and then listen for updates:
                //runGame()
                updateLocations()
            }
            .addOnFailureListener { exception ->
                println("!!! Error ${exception}")
            }
    }

    private fun updateLocations() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("places")
            .whereEqualTo("race", gameId)
            .orderBy("order")
            .addSnapshotListener { snapshot, e ->
                if(e != null) {
                    println("!!! Listen failed ${e}")
                }
                if (snapshot != null) {
                    for(document in snapshot.documents) {
                        val stop = document.toObject(GameLocation::class.java)
                        if(stop != null) {
                            stop.id = document.id
                            val index = stop.order!!
                            DataManager.locations[index].id = stop.id
                            DataManager.locations[index].visited = stop.visited
                            DataManager.locations[index].timestamp = stop.timestamp
                            //Set start time of first stop:
                            if (stop.order == 0 && stop.timestamp == null) {
                                stop.timestamp = gameInfo.start_time
                            }
                            println("!!! Updated: ${stop}")
                            println("!!! ID Updated: ${stop.id}")

                        }
                    }
                    //When all locations are loaded or updated, Start game logic:
                    runGame()
                }
            }
    }

    private fun addMarkersToMap() {
        for(markerOption in DataManager.markerOptions) {

            val marker = map.addMarker(markerOption)
            marker.tag = marker.snippet
            DataManager.markers.add(marker)
            println("!!! marker added Position: ${marker.position}")
        }
    }

    private fun addGeofenceCircle() {
        for(circleOption in DataManager.circlesOptions) {
            val circle = map.addCircle(circleOption)
            DataManager.circles.add(circle)

            println("!!! circleOption added Position: ${circleOption.center}")
        }
    }

    private fun handleMarker(marker: Int) {
        val timestamp = Timestamp.now()
        val date = timestamp.toDate()
        if (gameInfo.show_next_stop == SHOW_NEXT_STOP_DIRECT) {
            DataManager.markers[marker].isVisible = true
        } else if (gameInfo.show_next_stop == SHOW_NEXT_STOP_WITH_DELAY) {

            val currentTime = Timestamp.now().toDate().time
            val endTime = DataManager.locations[marker].timestamp!!.time + 30000//600000
            val diff = endTime - currentTime
            if (DataManager.locations[marker].hint != null) {
                Snackbar.make(findViewById(R.id.map), DataManager.locations[marker].hint!!, Snackbar.LENGTH_INDEFINITE).show()
            }

            Handler().postDelayed({
                DataManager.markers[marker].isVisible = true
            }, diff)
        }
    }

    private fun runGame() {
        val timestamp = Timestamp.now()
        val nextStopSetting = gameInfo.show_next_stop
        val radius: Float = (gameInfo.radius)!!.toFloat()

        for (i in 0..DataManager.locations.size) {
            //Change visited stops if any:
            if (DataManager.locations[i].visited == true) {
                DataManager.circles[i].isVisible = false
                //DataManager.markers[i].remove()
                DataManager.markers[i].isVisible = true
                DataManager.markers[i].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            }
            //Set up current stop
            else if (DataManager.locations[i].visited == false) {
                if (DataManager.locations[i].timestamp == null) {
                    val timestamp = Timestamp.now()
                    val date = timestamp.toDate()
                    DataManager.locations[i].timestamp = date
                    val user = auth.currentUser
                    val locationRef = db.collection("users").document(user!!.uid).
                    collection("places").document(DataManager.locations[i].id!!)
                    locationRef
                        .update("timestamp", timestamp)
                }

                createGeofence(i, radius)
                handleMarker(i)
                return
            }
        }
    }

    override fun onMarkerClick(marker: Marker?) : Boolean {

        val GAME_STRING = "GAMEID"
        val MARKER_STRING = "MARKER"

        if (marker != null) {
            val intent = Intent(this, AnswerQuestionActivity::class.java).apply {
                putExtra(GAME_STRING, gameId)
                putExtra(MARKER_STRING, marker.tag.toString())
            }
            startActivity(intent)
            println("!!! Marker snippet ${marker.snippet}")
        }
        return true
    }

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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

            }
        }
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

    private fun createGeofence(stopIndex: Int, radius: Float) {
        geofencingClient = LocationServices.getGeofencingClient(this)
        println("!!! stopIndex: ${stopIndex}")

        var geofences = mutableListOf<Geofence>()
        val coords = LatLng(DataManager.locations[stopIndex].latitude!!, DataManager.locations[stopIndex].longitude!!)
        val geofence = Geofence.Builder()
            .setRequestId(DataManager.locations[stopIndex].id)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setCircularRegion(coords.latitude, coords.longitude, radius)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        geofences.add(geofence)

//        geofences = DataManager.locations.map {
//
//            val coords = LatLng(it.latitude!!, it.longitude!!)
//            println("!!! Geonfece created: " + it.name)
//            Geofence.Builder()
//                .setRequestId(it.name)
//                .setExpirationDuration(Geofence.NEVER_EXPIRE)
//                .setCircularRegion(coords.latitude, coords.longitude, 200.0f)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//                .build()
//
//        }
        geofences.forEach {
            println("!!! Geofence in geofences added: " + it.requestId)
            println("!!! Geofence list size: ${geofences.size}")
        }

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
            //addGeofences(geofences)
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

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }
}




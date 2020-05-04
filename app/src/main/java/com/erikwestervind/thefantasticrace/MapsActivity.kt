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
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
    lateinit var gameInfo: GameInfo
    lateinit var gameId: String
    var geofences = mutableListOf<Geofence>()
    var locationsFirebase = mutableListOf<GameLocation>()
    var gameLocations = mutableListOf<GameLocation>()

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

        gameId = "q6ou5AIikGUM5tSOY1Bw" // Later, create function that changes this dynamically to the game you are in
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        DataManager.locations
        DataManager.markers

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


        //updateLocations()

        val user = auth.currentUser

        val locationsRef = db.collection("users").document(user!!.uid).collection("races")
            .document("q6ou5AIikGUM5tSOY1Bw").collection("stops")

        //Get data from Firestore - old, will be replaced:
        locationsRef.addSnapshotListener { snapshot, e ->
            if (snapshot!= null) {
                for(document in snapshot.documents) {
                    val newItem = document.toObject(GameLocation::class.java)
                    if (newItem != null)
                        DataManager.locations.add(newItem!!)
                    println("!!! From old function: ${DataManager.locations[0]}")
                }
                createGeofence()
            }
        }

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
                        gameInfo =game
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
                DataManager.markers.clear()
                DataManager.circles.clear()
                for(document in documents) {
                    val newStop = document.toObject(GameLocation::class.java)
                    if(newStop != null) {
                        newStop.id = document.id
                        gameLocations.add(newStop)
                        DataManager.locations.add(newStop)

                        val location = LatLng(newStop.latitude!!, newStop.longitude!!)
                        val marker = MarkerOptions().position(location)
                        DataManager.markers.add(marker)
                        //val radius: Double = (gameInfo.geofence_radius)!!.toDouble()

                        val circle = CircleOptions()
                            .center(location)
                            .radius(100.0)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(5.0f)
                            .fillColor(0x220000FF)

                        DataManager.circles.add(circle)
                        println("!!! ${newStop}")
                        //placeMarkerOnMap(LatLng(newStop.latitude!!, newStop.longitude!!))
                    }
                }
                addMarkersToMap()
                addGeofenceCircle(0)
                //StartGame and then listen for updates
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
                    //DataManager.locations.clear()
                    //DataManager.markers.clear()
                    //gameLocations.clear()
                    for(document in snapshot.documents) {
                        val stop = document.toObject(GameLocation::class.java)
                        if(stop != null) {
                            stop.id = document.id
                            val index = stop.order!!
                            DataManager.locations[index].id = stop.id
                            DataManager.locations[index].timestamp = stop.timestamp
                            //DataManager.locations.add(newStop)
                            //val location = LatLng(newStop.latitude!!, newStop.longitude!!)
                            //val marker = MarkerOptions().position(location)
                            //DataManager.markers.add(marker)
                            //gameLocations.add(newStop)
                            println("!!! ${stop}")
                            println("!!! Id: ${stop.id}")

                            //placeMarkerOnMap(LatLng(newStop.latitude!!, newStop.longitude!!))
                        }

                    }
                    //When all locations are loaded or updated, Start game logic

                }
            }
    }

    private fun addMarkersToMap() {
        for(marker in DataManager.markers) {
            map.addMarker(marker)
            println("!!! marker added Position: ${marker.position}")
        }
    }

//    mMap!!.addCircle(CircleOptions().apply {
//        center(latLng)
//        radius(200.0)
//        strokeColor(Color.BLUE)
//        strokeWidth(5.0f)
//        fillColor(0x220000FF)
//    })

    private fun addGeofenceCircle(circle: Int) {
        val circle = DataManager.circles[circle]
        map.addCircle(circle)
    }

    private fun runGame() {

    }

    //Move this function to other activity:
    private fun startGame() {
        val timestamp = Timestamp.now()
        val date = timestamp.toDate()

        if (gameInfo.start_time!! < date) {
            println("!!! Game started")
        }
        println("!!! Dagens datum och tid: ${date}")

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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

            }
        }
    }

//    private fun addToFirebase() {
//        val place = GameLocation("Sjöstan",59.304596, 18.094637)
//
//        val user = auth.currentUser
//        db.collection("users").document(user!!.uid).collection("places").add(place)
//            .addOnSuccessListener {
//                println("!!! write")
//            }
//            .addOnFailureListener {
//                println("!!! Didn't write")
//            }
//    }

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

    private fun createGeofence() {
        geofencingClient = LocationServices.getGeofencingClient(this)

        val coords = LatLng(DataManager.locations[0].latitude!!, DataManager.locations[0].longitude!!)
        val geofence = Geofence.Builder()
            .setRequestId(DataManager.locations[0].name)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setCircularRegion(coords.latitude, coords.longitude, 200.0f)
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




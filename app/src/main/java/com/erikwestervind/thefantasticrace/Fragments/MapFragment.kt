package com.erikwestervind.thefantasticrace.Fragments

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import com.erikwestervind.thefantasticrace.*
import com.erikwestervind.thefantasticrace.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.*


/**
 * A simple [Fragment] subclass.
 */
class MapFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var map : GoogleMap
    private var mapIsReady = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    var geofencingClient: GeofencingClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var gameInfo: GameInfo //Remove
    lateinit var gameId: String
    lateinit var mapHintTextView: TextView
    var gameLocations = mutableListOf<GameLocation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap -> map = googleMap
            map.isMyLocationEnabled = true

            mapIsReady = true

            mapHintTextView = view!!.findViewById(R.id.mapHintTextView)

            googleMap.setOnMarkerClickListener { // Triggered when user click any marker on the map

                val GAME_STRING = "GAMEID"
                val MARKER_STRING = "MARKER"

                println("!!! Marker clicked!")

                if (it != null) {
                    val intent = Intent(context, AnswerQuestionActivity::class.java).apply {
                        putExtra(GAME_STRING, gameId)
                        putExtra(MARKER_STRING, it.tag.toString())
                    }
                    startActivity(intent)
                    println("!!! Marker snippet ${it.snippet}")
                }
                true
            }



        }
        //gameId = intent.getStringExtra(GAME_ID_KEY)
        //gameId = "q6ou5AIikGUM5tSOY1Bw"
        gameId = (activity as ActiveGameActivity).gameId
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        DataManager.locations
        DataManager.markerOptions
        DataManager.markers
        DataManager.circlesOptions
        DataManager.circles

        getGameInfo()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //updateMap()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.let {

        }
    }

    private fun updateMap() {
//        val luma = LatLng(59.304568, 18.094541)
//        map.addMarker(MarkerOptions().position(luma).title("Marker in Luma"))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(luma, 12.0f))
        lastLocation = (activity as ActiveGameActivity).lastLocation

            val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            //placeMarkerOnMap(currentLatLng)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))


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
                        //setTitle(gameInfo.name!!.capitalize())
                        println("!!! Game info: ${game}")

                    }
                    //Get and update locations when the game info is collected
                    getLocations()


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
                mapHintTextView.text = DataManager.locations[marker].hint!!
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

//                DataManager.markers[i].setIcon(
//                    BitmapDescriptorFactory.defaultMarker(
//                        BitmapDescriptorFactory.HUE_GREEN))

                DataManager.markers[i].setIcon(BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_marker_checked)))


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

    private fun createGeofence(stopIndex: Int, radius: Float) {
        if (context != null) {
            geofencingClient = LocationServices.getGeofencingClient(context!!)
        }

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
            val intent = Intent(context, GeofenceReceiver::class.java)
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        }

        geofencingClient = LocationServices.getGeofencingClient(context!!)

        geofencingClient!!.addGeofences(geofencingRequest, geofencePendingIntent).run {
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

    override fun onMarkerClick(marker: Marker?) : Boolean {

        val GAME_STRING = "GAMEID"
        val MARKER_STRING = "MARKER"

        println("!!! Marker clicked!")

        if (marker != null) {
            val intent = Intent(context, AnswerQuestionActivity::class.java).apply {
                putExtra(GAME_STRING, gameId)
                putExtra(MARKER_STRING, marker.tag.toString())
            }
            startActivity(intent)
            println("!!! Marker snippet ${marker.snippet}")
        }
        return true
    }

}

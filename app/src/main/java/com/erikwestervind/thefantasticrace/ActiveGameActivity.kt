package com.erikwestervind.thefantasticrace

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import com.erikwestervind.thefantasticrace.Adapter.PagerViewAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

const val SHOW_NEXT_STOP_DIRECT = 0
const val SHOW_NEXT_STOP_WITH_DELAY = 1
const val GAME_ID_KEY = "GAME_ID"

class ActiveGameActivity : AppCompatActivity() {

    private lateinit var stopsBtn: ImageButton
    private lateinit var mapBtn: ImageButton
    private lateinit var playersBtn: ImageButton

    private lateinit var mViewPager: ViewPager
    private lateinit var mPagerViewAdapter: PagerViewAdapter

    lateinit var gameId: String
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    lateinit var currentGame: GameInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_game)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        createLocationRequest()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                lastLocation = locationResult.lastLocation
            }
        }
        createLocationRequest()

        gameId = intent.getStringExtra(GAME_ID_KEY)
        updateTitle()

        //init views
        mViewPager = findViewById(R.id.mViewPager)

        stopsBtn = findViewById(R.id.stopsBtn)
        mapBtn = findViewById(R.id.mapsBtn)
        playersBtn = findViewById(R.id.playersBtn)

        stopsBtn.setOnClickListener {
            mViewPager.currentItem = 0
        }
        mapBtn.setOnClickListener {
            mViewPager.currentItem = 1
        }
        playersBtn.setOnClickListener {
            mViewPager.currentItem = 2
        }

        mPagerViewAdapter = PagerViewAdapter(supportFragmentManager, gameId)
        mViewPager.adapter = mPagerViewAdapter
        mViewPager.offscreenPageLimit = 3

        //add page change listener
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                changingTabs(position)
            }
        })
        //default tab
        mViewPager.currentItem = 1
        mapBtn.setImageResource(R.drawable.ic_map_white_24dp)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun changingTabs(position: Int) {
        if (position == 0) {
            stopsBtn.setImageResource(R.drawable.ic_list_white_24dp)
            mapBtn.setImageResource(R.drawable.ic_map_purple_24dp)
            playersBtn.setImageResource(R.drawable.ic_group_purple_24dp)
        }
        if (position == 1) {
            stopsBtn.setImageResource(R.drawable.ic_list_purple_24dp)
            mapBtn.setImageResource(R.drawable.ic_map_white_24dp)
            playersBtn.setImageResource(R.drawable.ic_group_purple_24dp)
        }
        if (position == 2) {
            stopsBtn.setImageResource(R.drawable.ic_list_purple_24dp)
            mapBtn.setImageResource(R.drawable.ic_map_purple_24dp)
            playersBtn.setImageResource(R.drawable.ic_group_white_24dp)
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
                    e.startResolutionForResult(
                        this@ActiveGameActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
                ActiveGameActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
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

    private fun updateTitle() {
        val user = auth.currentUser

        db.collection("users").document(user!!.uid).collection("races_invited")
            .whereEqualTo("parent_race", gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                }
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        if (document != null) {
                            currentGame = document.toObject(GameInfo::class.java)!!
                            if (currentGame != null) {
                                title = currentGame.name!!.capitalize()

                                updatePlayer()
                            }
                            if (e != null) {
                            }
                        }
                    }
                }
            }
    }

    private fun updatePlayer() {
        val user = auth.currentUser

        db.collection("races").document(gameId).collection("users").document(user!!.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                }
                if (snapshot != null) {
                    val player = snapshot.toObject(Player::class.java)
                    if (player != null) {
                        if (player.gameFinished == true) {
                            title = "Finished: ${currentGame.name!!.capitalize()}"
                            finishedMessage()
                        }
                    }
                    if (e != null) {
                    }
                }
            }
    }

    private fun finishedMessage() {
        val builder = AlertDialog.Builder(this@ActiveGameActivity)
        builder.setTitle("Game finished!")
        builder.setMessage("You have finished this race! Well done! ")
        builder.setPositiveButton("OK") { dialog, which ->
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }
}

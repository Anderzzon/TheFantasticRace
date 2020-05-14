package com.erikwestervind.thefantasticrace

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.erikwestervind.thefantasticrace.Adapter.PagerViewAdapter



class ActiveGameActivity : AppCompatActivity() {

    private lateinit var stopsBtn:ImageButton
    private lateinit var mapBtn:ImageButton
    private lateinit var playersBtn:ImageButton

    private lateinit var mViewPager: ViewPager
    private lateinit var mPagerViewAdapter: PagerViewAdapter

    lateinit var gameId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_game)

        gameId = intent.getStringExtra(GAME_ID_KEY)

        //init views
        mViewPager = findViewById(R.id.mViewPager)

        stopsBtn = findViewById<ImageButton>(R.id.stopsBtn)
        mapBtn = findViewById<ImageButton>(R.id.mapsBtn)
        playersBtn = findViewById<ImageButton>(R.id.playersBtn)

        stopsBtn.setOnClickListener {
            mViewPager.currentItem = 0
        }
        mapBtn.setOnClickListener {
            mViewPager.currentItem = 1
        }
        playersBtn.setOnClickListener {
            mViewPager.currentItem = 2
        }

        mPagerViewAdapter = PagerViewAdapter(supportFragmentManager)
        mViewPager.adapter = mPagerViewAdapter
        mViewPager.offscreenPageLimit = 3

        //add page change listener
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{

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


}

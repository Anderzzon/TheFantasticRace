package com.erikwestervind.thefantasticrace.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.erikwestervind.thefantasticrace.Fragments.MapFragment
import com.erikwestervind.thefantasticrace.Fragments.PlayersFragment
import com.erikwestervind.thefantasticrace.Fragments.StopsFragment

internal class PagerViewAdapter(fm: FragmentManager?): FragmentPagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {

        return when(position) {
            0 -> {
                StopsFragment()
            }
            1 -> {
                MapFragment()
            }
            2 -> {
                PlayersFragment()
            }
            else -> MapFragment()

        }

    }

    override fun getCount(): Int {
        return 3
    }
}
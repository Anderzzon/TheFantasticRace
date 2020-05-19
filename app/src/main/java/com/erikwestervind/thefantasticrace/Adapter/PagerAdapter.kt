package com.erikwestervind.thefantasticrace.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.erikwestervind.thefantasticrace.Fragments.MapFragment
import com.erikwestervind.thefantasticrace.Fragments.PlayersFragment
import com.erikwestervind.thefantasticrace.Fragments.StopsFragment

internal class PagerViewAdapter(fm: FragmentManager?, id: String): FragmentPagerAdapter(fm!!) {

    private val id = id

    override fun getItem(position: Int): Fragment {

        return when(position) {
            0 -> {
                //val stopsFragment = StopsFragment.newInstance(id)
                println("!!! id: ${id}")

                return StopsFragment.newInstance("q6ou5AIikGUM5tSOY1Bw")
                //StopsFragment()
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
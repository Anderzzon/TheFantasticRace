package com.erikwestervind.thefantasticrace.Adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.erikwestervind.thefantasticrace.Fragments.MapFragment
import com.erikwestervind.thefantasticrace.Fragments.PlayersFragment
import com.erikwestervind.thefantasticrace.Fragments.StopsFragment

internal class PagerViewAdapter(fm: FragmentManager?, id: String): FragmentPagerAdapter(fm!!) {

    private val id = id
    private val GAME_ID = "gameId"

    override fun getItem(position: Int): Fragment {

        return when(position) {
            0 -> {
                //val stopsFragment = StopsFragment.newInstance(id)
                println("!!! id in PagerAdapter: ${id}")

                //return StopsFragment.newInstance("q6ou5AIikGUM5tSOY1Bw")
                val args = Bundle()
            args.putString(GAME_ID, id)
            var fragment = StopsFragment()
                fragment.arguments = args
                fragment

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
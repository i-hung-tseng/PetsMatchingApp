package com.example.petsmatchingapp.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentHomeBinding
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeFragment : Fragment() {

  private lateinit var binding: FragmentHomeBinding
  private lateinit var nav: NavController

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {


    showActionBarAndBottomNavigation()
    binding = FragmentHomeBinding.inflate(inflater)
    nav = findNavController()
    setHasOptionsMenu(true)
    return binding.root
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.home_menu,menu)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when(item.itemId){
      R.id.navigation_profile -> {
        nav.navigate(R.id.action_navigation_home_to_profileFragment)
      }

    }
    return super.onOptionsItemSelected(item)
  }

  private fun showActionBarAndBottomNavigation(){

    if (requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility == View.GONE){
      requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
    }

    val activityInstance = this.activity as MatchingActivity
    activityInstance.supportActionBar?.show()

  }
}
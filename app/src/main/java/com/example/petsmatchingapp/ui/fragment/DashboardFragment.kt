package com.example.petsmatchingapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager

import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentDashboardBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.ui.adapter.DashboardAdapter
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class DashboardFragment : BaseFragment() {


  private val accountViewModel: AccountViewModel by sharedViewModel()
  private val matchingViewModel: MatchingViewModel by sharedViewModel()
  private lateinit var dashboardAdapter: DashboardAdapter
  private lateinit var binding: FragmentDashboardBinding
  private lateinit var nav: NavController

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    binding = FragmentDashboardBinding.inflate(inflater,container,false)


    accountViewModel.getUserDetail()

    matchingViewModel.allInvitationList.observe(viewLifecycleOwner, Observer {
      dashboardAdapter.submitList(it)
    })

    setAdapter()
    nav = findNavController()
    accountViewModel.getCurrentUID()?.let { matchingViewModel.getAllInvitation(it,this) }




    return binding.root
  }

  override fun onResume() {
    showActionBarAndBottomNavigation()
    super.onResume()
  }


  private fun setAdapter() {
    binding.rvDashboardFragment.layoutManager = GridLayoutManager(requireContext(), 2)
    dashboardAdapter = DashboardAdapter(this)
    binding.rvDashboardFragment.setHasFixedSize(true)
    binding.rvDashboardFragment.adapter = dashboardAdapter

  }



  fun addSelectedInvitationToViewModel(invitation: Invitation) {
    matchingViewModel.addSelectedInvitationToLiveData(invitation)
    nav.navigate(R.id.action_navigation_dashboard_to_invitationDetailFragment)

  }


  fun getAllInvitationFail(e: String) {
    showSnackBar(e, true)

  }


  private fun showActionBarAndBottomNavigation() {

    if (requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility == View.GONE) {
      requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility =
        View.VISIBLE
    }

    val activityInstance = this.activity as MatchingActivity
    activityInstance.supportActionBar?.show()

  }
}
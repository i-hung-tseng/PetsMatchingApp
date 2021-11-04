package com.example.petsmatchingapp.ui.fragment

import android.content.Context
import android.icu.text.RelativeDateTimeFormatter
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.WindowDecorActionBar
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager

import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentDashboardBinding
import com.example.petsmatchingapp.model.CurrentUser
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.ui.adapter.DashboardAdapter
import com.example.petsmatchingapp.utils.CheckInternetState
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf


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




    binding = FragmentDashboardBinding.inflate(inflater, container, false)

    setAdapter()
    nav = findNavController()


    accountViewModel.getCurrentUID()
    accountViewModel.getUserDetail()

    matchingViewModel.getAllInvitationState.observe(viewLifecycleOwner, {
      showSnackBar(it, true)
    })

    matchingViewModel.dashboardInvitationList.observe(viewLifecycleOwner, {
      Timber.d("Search測試進到 dashboardInvitation ${matchingViewModel.dashboardInvitationList.value?.size}")

      dashboardAdapter.submitList(it)

    })

    //觀察所有invitation，然後再判斷是否有搜尋了，如果沒有，就把所有的all丟進去，如果有就不做事
    matchingViewModel.allInvitationList.observe(viewLifecycleOwner, {

      Timber.d("測試搜尋 allInvitation ${matchingViewModel.allInvitationList.value?.size}")
      if (matchingViewModel.dashboardInvitationList.value.isNullOrEmpty()) {
        dashboardAdapter.submitList(it)
      }
    })

    binding.fabSearch.setOnClickListener {
      nav.navigate(R.id.action_navigation_dashboard_to_searchFragment)
    }

    dashboardAdapter.clickEvent = {
      matchingViewModel.addSelectedInvitationToLiveData(it)
      nav.navigate(R.id.action_navigation_dashboard_to_invitationDetailFragment)
    }


    return binding.root
  }



  private fun setAdapter() {
    binding.rvDashboardFragment.layoutManager = GridLayoutManager(requireContext(), 2)
    dashboardAdapter = DashboardAdapter()
    binding.rvDashboardFragment.setHasFixedSize(true)
    binding.rvDashboardFragment.adapter = dashboardAdapter

  }

}
package com.example.petsmatchingapp.ui.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentHomeBinding
import com.example.petsmatchingapp.model.CurrentUser
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.ui.adapter.HomeAdapter
import com.example.petsmatchingapp.ui.adapter.MultiplePhotoAdapter
import com.example.petsmatchingapp.utils.CheckInternetState
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var nav: NavController

    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
    ): View? {



//        showActionBarAndBottomNavigation()
        binding = FragmentHomeBinding.inflate(inflater)

//
//        matchingViewModel.deletedState.observe(viewLifecycleOwner,{
//            if(it == Constant.TRUE){
//                showSnackBar(resources.getString(R.string.delete_successful), false)
//                CurrentUser.currentUser?.uid?.let { matchingViewModel.getCurrentUserInvitation() }
//            }else{
//                showSnackBar(it,true)
//            }
//        })
        matchingViewModel.homeInvitationList.observe(viewLifecycleOwner, Observer {
          homeAdapter.submitList(it)
        })
        setAdapter()


        homeAdapter.clickItemViewEvent = {
            matchingViewModel.addSelectedInvitationToLiveData(it)
            nav.navigate(R.id.action_navigation_home_to_invitationDetailFragment)
        }

        homeAdapter.clickGarbageEvent = {
            setAndShowDeleteDialog(it)
        }

        matchingViewModel.getCurrentUserInvitationState.observe(viewLifecycleOwner,{
            showSnackBar(it,true)
        })

        nav = findNavController()
        setHasOptionsMenu(true)
        return binding.root
    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
          R.id.navigation_profile -> {
            nav.navigate(R.id.action_navigation_home_to_profileFragment)
          }
          R.id.navigation_add_invitation -> {
            nav.navigate(R.id.action_navigation_home_to_addInvitationFragment)
          }

        }
        return super.onOptionsItemSelected(item)
    }

//    private fun showActionBarAndBottomNavigation() {
//
//        if (requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility == View.GONE) {
//            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility =
//                View.VISIBLE
//        }
//
//        val activityInstance = this.activity as MatchingActivity
//        activityInstance.supportActionBar?.show()
//
//    }

    private fun setAdapter() {
        binding.rvHomeFragment.layoutManager = LinearLayoutManager(requireContext())
        homeAdapter = HomeAdapter()
        binding.rvHomeFragment.adapter = homeAdapter
    }

    fun deleteInvitation(id: String) {
        matchingViewModel.deleteInvitation(id)
    }


    fun setAndShowDeleteDialog(id: String){

      val builder = AlertDialog.Builder(requireContext())
      builder.apply {
        setTitle(resources.getString(R.string.alertDialog_title_do_you_want_delete))
        setMessage(resources.getString(R.string.alertDialog_message_delete_description))
        setPositiveButton(resources.getString(R.string.alertDialog_delete_positive_yes),object :DialogInterface.OnClickListener{
          override fun onClick(dialog: DialogInterface?, which: Int) {
            deleteInvitation(id)
            dialog?.dismiss()
          }
        })
        setNegativeButton(resources.getString(R.string.alertDialog_delete_negative_no),object :DialogInterface.OnClickListener{
          override fun onClick(dialog: DialogInterface?, which: Int) {
            dialog?.dismiss()
          }

        })

      }

      val alertDialog = builder.create()
      alertDialog.setCancelable(false)
      alertDialog.show()

    }


//
//
//    fun addSelectedInvitationToViewModel(invitation: Invitation){
//        matchingViewModel.addSelectedInvitationToLiveData(invitation)
//        nav.navigate(R.id.action_navigation_home_to_invitationDetailFragment)
//    }

}
package com.example.petsmatchingapp.ui.fragment

import android.accounts.Account
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentHomeBinding
import com.example.petsmatchingapp.databinding.FragmentInvitationDetailBinding
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.ChatViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.compat.SharedViewModelCompat.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class InvitationDetailFragment : Fragment() {


    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private val accountViewModel: AccountViewModel by sharedViewModel()
    private val chatViewModel: ChatViewModel by sharedViewModel()
    private lateinit var binding: FragmentInvitationDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentInvitationDetailBinding.inflate(inflater)


        if (matchingViewModel.selectedInvitation.value?.user_id == accountViewModel.userDetail.value?.id ){
            binding.btnInvitationDetailSubmit.visibility = View.GONE

        }
        matchingViewModel.selectedInvitation.observe(viewLifecycleOwner, Observer {
            val mFormat = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
            val displayTime = sdf.format(it.date_time?.toDate()?.time)
            Timber.d("it.date_time_toDate ${it.date_time?.toDate()}")
            Timber.d("it.date_time_toDate ${it.date_time?.toDate()?.time}")
            binding.tvInvitationDetailDateTime.text = displayTime
        })

        dismissActivityActionBarAndBottomNavigationView()
        binding.toolbarInvitationDetailFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarInvitationDetailFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        matchingViewModel.selectedInvitation.observe(viewLifecycleOwner, Observer {
            Constant.loadPetImage(it.pet_image,binding.ivInvitationDetailImage)
        })

        binding.btnInvitationDetailSubmit.setOnClickListener{
            findNavController().navigate(R.id.action_invitationDetailFragment_to_chatRoomFragment)

        }

        chatViewModel.setFromDetailOrNot(true)
        binding.viewModel = matchingViewModel
        return binding.root
    }

    private fun dismissActivityActionBarAndBottomNavigationView(){
        val activityInstance = this.activity as MatchingActivity
        activityInstance.supportActionBar?.hide()
        activityInstance.findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

    }


}
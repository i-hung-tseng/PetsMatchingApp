package com.example.petsmatchingapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentProfileBinding
import com.example.petsmatchingapp.ui.activity.AccountActivity
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class ProfileFragment : BaseFragment(),View.OnClickListener {



    private lateinit var binding: FragmentProfileBinding
    private val accountViewModel: AccountViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dismissActivityActionBarAndBottomNavigationView()

        binding = FragmentProfileBinding.inflate(inflater)
        binding.toolbarProfileFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarProfileFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        accountViewModel.userDetail.observe( viewLifecycleOwner, Observer {
            Timber.d("viewModel = $accountViewModel")
            Timber.d("accountId = ${accountViewModel.userDetail.value!!.id}")
            Constant.loadUserImage(it.image,binding.ivProfileFragmentImage)
            binding.tvProfileFragmentName.setText(it.name)
            binding.tvProfileFragmentEmail.setText(it.email)

        })


        binding.btnProfileFragmentSignout.setOnClickListener(this)
        binding.btnProfileFragmentGoEdit.setOnClickListener(this)
        return binding.root
    }


    private fun dismissActivityActionBarAndBottomNavigationView(){
        val activityInstance = this.activity as MatchingActivity
        activityInstance.supportActionBar?.hide()
        activityInstance.findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

    }

    override fun onClick(v: View?) {
        when(v){

            binding.btnProfileFragmentSignout ->{
                accountViewModel.signOut()
                startActivity(Intent(requireActivity(),AccountActivity::class.java))
                requireActivity().finish()
            }
            binding.btnProfileFragmentGoEdit ->{
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }


        }
    }


}
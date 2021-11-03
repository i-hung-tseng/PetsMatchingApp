package com.example.petsmatchingapp.ui.fragment

import android.accounts.Account
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentHomeBinding
import com.example.petsmatchingapp.databinding.FragmentInvitationDetailBinding
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.ui.adapter.DetailAdapter
import com.example.petsmatchingapp.ui.adapter.MultiplePhotoAdapter
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.ChatViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.io.Files.move
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import org.koin.androidx.viewmodel.compat.SharedViewModelCompat.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.nio.file.Files.move
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



//        val image  = arguments?.getString("detailImage")


        binding = FragmentInvitationDetailBinding.inflate(inflater)
//        binding.ivInvitationDetailImage.transitionName = "image"

//        Constant.loadPetImage(image!!,binding.ivInvitationDetailImage)

//        sharedElementEnterTransition =
//            TransitionInflater.from(context)
//                .inflateTransition(android.R.transition.move)


//        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.shared_image)
//        val changeBound = ChangeBounds().setDuration(5000)
//        sharedElementEnterTransition = changeBound

        if (matchingViewModel.selectedInvitation.value?.user_id == accountViewModel.userDetail.value?.id ){
            binding.btnInvitationDetailSubmit.visibility = View.GONE

        }




        matchingViewModel.selectedInvitation.observe(viewLifecycleOwner, Observer {
            val mFormat = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
            val displayTime = sdf.format(it.date_time?.toDate()?.time)
            binding.tvInvitationDetailDateTime.text = displayTime
            it.photoUriList?.let { it1 -> setAdapter(it1) }




        })

        binding.toolbarInvitationDetailFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarInvitationDetailFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        binding.btnInvitationDetailSubmit.setOnClickListener{
            findNavController().navigate(R.id.action_invitationDetailFragment_to_chatRoomFragment)

        }

        chatViewModel.setFromDetailOrNot(true)
        binding.viewModel = matchingViewModel
        return binding.root
    }



    private fun setAdapter(list: List<String>){

            binding.bannerImage.adapter = DetailAdapter(list)
            binding.indicatorAddInvitation.apply {
                setSliderColor(Color.GRAY, ContextCompat.getColor(requireContext(),R.color.pewter_blue))
                setSliderWidth(resources.getDimension(R.dimen.indicator_width))
                setSlideMode(IndicatorSlideMode.SCALE)
                setIndicatorStyle(IndicatorStyle.CIRCLE)
                    .setupWithViewPager(binding.bannerImage)

            }

        }
    }



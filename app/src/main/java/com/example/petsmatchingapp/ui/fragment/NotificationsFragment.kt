package com.example.petsmatchingapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentNotificationsBinding
import com.example.petsmatchingapp.model.LastMessage
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.ui.adapter.NotificationAdapter
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.ChatViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NotificationsFragment : Fragment() {

  private lateinit var notificationAdapter: NotificationAdapter
  private lateinit var binding: FragmentNotificationsBinding
  private val chatViewModel: ChatViewModel by sharedViewModel()
  private val accountViewModel: AccountViewModel by sharedViewModel()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {



    chatViewModel.lastMessageList.observe(viewLifecycleOwner, Observer {
      notificationAdapter.submitList(it)
    })


    binding = FragmentNotificationsBinding.inflate(inflater)
    chatViewModel.setFromDetailOrNot(false)
    setAdapter()
    return binding.root
  }

  override fun onResume() {
    showActionBarAndBottomNavigation()
    super.onResume()
  }


  private fun setAdapter(){
    notificationAdapter = NotificationAdapter(this)
    binding.rvAllMessage.adapter = notificationAdapter
    binding.rvAllMessage.layoutManager = LinearLayoutManager(requireContext())

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    chatViewModel.getLastMessage(accountViewModel.userDetail.value!!.id)
    super.onViewCreated(view, savedInstanceState)
  }

  fun goChatRoom(lastMessage: LastMessage){
    chatViewModel.saveSelectedChatRoomUserDetail(lastMessage)
    findNavController().navigate(R.id.action_navigation_notifications_to_chatRoomFragment)

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
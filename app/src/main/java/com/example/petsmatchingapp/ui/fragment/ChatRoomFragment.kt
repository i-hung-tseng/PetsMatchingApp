package com.example.petsmatchingapp.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentChatRoomBinding
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.ui.adapter.ChatRoomAdapter
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.ChatViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class ChatRoomFragment : BaseFragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private val accountViewModel: AccountViewModel by sharedViewModel()
    private val chatViewModel: ChatViewModel by sharedViewModel()
    private lateinit var chatAdapter: ChatRoomAdapter
    private var selectedUri: Uri? = null
    private lateinit var type: String



    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uri ->
        if (uri.resultCode == Activity.RESULT_OK) {
            selectedUri = uri.data?.data
            selectedUri?.let { it ->
                Constant.loadPetImage(it, binding.ivChatRoomSelectedPhoto)
                val filePath = selectedUri?.path
                filePath?.substring(filePath?.lastIndexOf(".") + 1) ?.let {
                    type = it
                }
                binding.btnCamera.visibility = View.GONE
                binding.ivChatRoomSelectedPhoto.visibility = View.VISIBLE
                binding.edChatRoomInputMessage.apply {
                    setText(resources.getString(R.string.already_selected_photo))
                    isEnabled = false
                }
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        chatViewModel.messageList.observe(viewLifecycleOwner, Observer {
            chatAdapter.submitList(it)

        })



        binding = FragmentChatRoomBinding.inflate(inflater)
        binding.btnSend.setOnClickListener {
            if (!TextUtils.isEmpty(
                    binding.edChatRoomInputMessage.text.toString().trim())
            ) {
                sendMessageAndSaveLastMessage()
            }
        }


        binding.btnCamera.setOnClickListener{
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .createIntent { intent ->
                resultLauncher.launch(intent)
                }
        }


        setAdapter()

        binding.ivChatRoomSelectedPhoto.setOnClickListener {
            setAlertDialog()
        }
        binding.btnSend.background.alpha = 0


        if(chatViewModel.fromDetail.value == false){
            accountViewModel.findUserDetailByID(chatViewModel.selectedChatRoomUserDetail.value!!.display_id)
        }
        binding.toolbarChatRoomFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarChatRoomFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        chatViewModel.imageFail.observe(viewLifecycleOwner, Observer {
            showSnackBar(it,true)
            chatViewModel.resetImageFail()
        })

        dismissActivityActionBarAndBottomNavigationView()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (chatViewModel.fromDetail.value == true) {
            chatViewModel.messageValueListener(

                accountViewModel.userDetail.value!!.id,
                matchingViewModel.selectedInvitation.value!!.user_id
            )
            matchingViewModel.selectedInvitation.value?.user_name?.let {
                binding.tvChatRoomAcceptUserName.text = it
            }
        } else {
            chatViewModel.messageValueListener(

                accountViewModel.userDetail.value!!.id,
                chatViewModel.selectedChatRoomUserDetail.value!!.display_id
            )
            binding.tvChatRoomAcceptUserName.text = chatViewModel.selectedChatRoomUserDetail.value!!.display_name
        }

        super.onViewCreated(view, savedInstanceState)
    }


    private fun dismissActivityActionBarAndBottomNavigationView() {
        val activityInstance = this.activity as MatchingActivity
        activityInstance.supportActionBar?.hide()
        activityInstance.findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

    }


    private fun sendMessageAndSaveLastMessage() {

        var message :Message

        if (chatViewModel.fromDetail.value == true){
            message = Message(
                user_name = accountViewModel.userDetail.value!!.name,
                message = binding.edChatRoomInputMessage.text.toString().trim(),
                send_user_id = accountViewModel.userDetail.value!!.id,
                accept_user_id = matchingViewModel.selectedInvitation.value!!.user_id,
                send_user_image = accountViewModel.userDetail.value!!.image,
                send_user_name = accountViewModel.userDetail.value!!.name,
                time = ServerValue.TIMESTAMP,
                accept_user_image = matchingViewModel.selectedInvitation.value?.user_image,
                accept_user_name = matchingViewModel.selectedInvitation.value?.user_name,
            )
        }else{
            message = Message(
                user_name = accountViewModel.userDetail.value!!.name,
                message = binding.edChatRoomInputMessage.text.toString().trim(),
                send_user_id = accountViewModel.userDetail.value!!.id,
                send_user_image = accountViewModel.userDetail.value!!.image,
                send_user_name = accountViewModel.userDetail.value!!.name,
                accept_user_name = accountViewModel.selectedUserDetail.value!!.name,
                accept_user_image = accountViewModel.selectedUserDetail.value!!.image,
                accept_user_id = accountViewModel.selectedUserDetail.value!!.id,
                time = ServerValue.TIMESTAMP,

            )
        }

        if (selectedUri == null){
            chatViewModel.sendMessage(message)
            chatViewModel.saveLastMessage(message)
        }else{
            chatViewModel.saveImageToFireStorage(type, selectedUri!!,message)
            selectedUri = null
            binding.btnCamera.visibility = View.VISIBLE
            binding.ivChatRoomSelectedPhoto.visibility = View.GONE
            binding.edChatRoomInputMessage.isEnabled = true
        }
        binding.edChatRoomInputMessage.setText("")

    }

    private fun setAdapter() {
        chatAdapter = ChatRoomAdapter(requireContext())
        binding.rvChatRoom.adapter = chatAdapter
        val linerLayout = LinearLayoutManager(requireContext())
        linerLayout.reverseLayout = true
        binding.rvChatRoom.layoutManager = linerLayout


    }



    private fun setAlertDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle("取消選取")
            setPositiveButton("取消選取",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    binding.btnCamera.visibility = View.VISIBLE
                    binding.ivChatRoomSelectedPhoto.visibility = View.GONE
                    binding.edChatRoomInputMessage.isEnabled = true
                    binding.edChatRoomInputMessage.setText("")
                    selectedUri = null
                    dialog?.dismiss()
                }

            })
            setNegativeButton("讓我再想一下",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.dismiss()
                }
            })
        }

      val alertDialog = builder.create()
      alertDialog.show()
    }

}
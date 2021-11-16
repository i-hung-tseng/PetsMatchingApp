package com.example.petsmatchingapp.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Space
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentChatRoomBinding
import com.example.petsmatchingapp.model.Message
import com.example.petsmatchingapp.model.SelectedUri
import com.example.petsmatchingapp.ui.adapter.ChatRoomAdapter
import com.example.petsmatchingapp.ui.adapter.GalleryAdapter
import com.example.petsmatchingapp.utils.CheckInternetState
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.utils.SpacesItemDecoration
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.ChatViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class ChatRoomFragment : BaseFragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private val accountViewModel: AccountViewModel by sharedViewModel()
    private val chatViewModel: ChatViewModel by sharedViewModel()
    private lateinit var chatAdapter: ChatRoomAdapter
    private lateinit var galleryAdapter: GalleryAdapter
    private var selectedUri: Uri? = null
    private var type: String? = null
    private var selectedImageFromGallery = mutableSetOf<SelectedUri>()
    private var selectedImageFromGalleryBeforeSend = mutableSetOf<SelectedUri>()

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uri ->
            if (uri.resultCode == Activity.RESULT_OK) {
                selectedUri = uri.data?.data
                selectedUri?.let { it ->
                    val filePath = selectedUri?.path
                    filePath?.substring(filePath?.lastIndexOf(".") + 1)?.let {
                        type = it
                    }
                }
                sendMessageAndSaveLastMessage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentChatRoomBinding.inflate(inflater)


//        if (chatViewModel.fromDetail.value == false) {
//            accountViewModel.findUserDetailByID(chatViewModel.selectedChatRoomUserDetail.value!!.display_id)
//        }

        onClick()
        setGalleryAdapter()
        setAdapter()
        setToolBar()
        observe()
        listener()


        galleryAdapter.clickEvent = {

            selectedImageFromGalleryBeforeSend = mutableSetOf<SelectedUri>()
            selectedImageFromGallery.add(it)
            for (item in selectedImageFromGallery) {
                if (item.selected) {
                    selectedImageFromGalleryBeforeSend.add(item)
                }
            }
            if (selectedImageFromGalleryBeforeSend.size > 0) {
                binding.edChatRoomInputMessage.setText(
                    resources.getString(
                        R.string.selected_count,
                        selectedImageFromGalleryBeforeSend.size
                    )
                )
                binding.ivChatRoomSend.visibility = View.VISIBLE

            } else {
                binding.edChatRoomInputMessage.setText("")
                binding.ivChatRoomSend.visibility = View.GONE
            }
        }

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
            binding.tvChatRoomAcceptUserName.text =
                chatViewModel.selectedChatRoomUserDetail.value!!.display_name
        }

        super.onViewCreated(view, savedInstanceState)
    }


    private fun sendMessageAndSaveLastMessage() {

        var message: Message

        if (!CheckInternetState(requireContext()).isInternetAvailable()) {
            showSnackBar("請先確認網路情況", true)
            return
        }
        if (chatViewModel.fromDetail.value == true) {
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
        } else {

            message = Message(
                user_name = accountViewModel.userDetail.value!!.name,
                message = binding.edChatRoomInputMessage.text.toString().trim(),
                send_user_id = accountViewModel.userDetail.value!!.id,
                send_user_image = accountViewModel.userDetail.value!!.image,
                send_user_name = accountViewModel.userDetail.value!!.name,
                accept_user_name = chatViewModel.selectedChatRoomUserDetail.value?.display_name,
                accept_user_image = chatViewModel.selectedChatRoomUserDetail.value?.display_image,
                accept_user_id = chatViewModel.selectedChatRoomUserDetail.value?.display_id,
                time = ServerValue.TIMESTAMP,

                )
        }

        when{
            selectedImageFromGalleryBeforeSend.isNotEmpty() -> {
                showDialog(resources.getString(R.string.please_wait))
                for (item in selectedImageFromGalleryBeforeSend) {
                    Constant.getFileExtension(requireActivity(), item.uri)?.let {
                            chatViewModel.saveImageToFireStorage(
                                it, item.uri, message
                            )
                    }
                }
                selectedImageFromGallery = mutableSetOf()
                selectedImageFromGalleryBeforeSend = mutableSetOf()
                binding.rvPhotoFromGallery.visibility = View.GONE
            }
            selectedUri != null -> {
                showDialog(resources.getString(R.string.please_wait))
                type?.let {
                    chatViewModel.saveImageToFireStorage(
                        it, selectedUri!!, message,
                    )
                }
                type = null
                selectedUri = null
            }
            else -> {
                chatViewModel.sendMessage(message)
            }
        }

        binding.edChatRoomInputMessage.setText("")

    }

    private fun setAdapter() {
        chatAdapter = ChatRoomAdapter()
        val linerLayout = LinearLayoutManager(requireContext())
        linerLayout.reverseLayout = true
        binding.rvChatRoom.apply {
            this.adapter = chatAdapter
            this.layoutManager = linerLayout
            this.addItemDecoration(SpacesItemDecoration(null, 5))
        }
    }

    private fun loadImagesFromSDCard(): MutableList<SelectedUri> {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val columnIndexID: Int
        val listOfAllImages: MutableList<SelectedUri> = mutableListOf()
        val project = arrayOf(MediaStore.Images.Media._ID)
        var imageId: Long
        cursor = requireActivity().contentResolver.query(uri, project, null, null, null)
        cursor?.let {
            columnIndexID = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                imageId = it.getLong(columnIndexID)
                val model = SelectedUri(Uri.withAppendedPath(uri, "" + imageId), false)
                listOfAllImages.add(model)
            }
            cursor.close()
        }
        return listOfAllImages
    }

    private fun setGalleryAdapter() {
        galleryAdapter = GalleryAdapter(requireContext())
        binding.rvPhotoFromGallery.apply {
            this.layoutManager = GridLayoutManager(requireContext(), 3)
            this.adapter = galleryAdapter
            this.addItemDecoration(SpacesItemDecoration(1, 1))
        }
    }

    private fun showPhotoFromGallery() {

        binding.rvPhotoFromGallery.visibility = View.VISIBLE

        binding.edChatRoomInputMessage.apply {
            this.clearFocus()
            setText("")
        }
        if (selectedImageFromGalleryBeforeSend.isEmpty()) galleryAdapter.submitList(
            loadImagesFromSDCard()
        )
        if (Build.VERSION.SDK_INT >= 30) {
            val controller = requireActivity().window.insetsController
            controller?.hide(WindowInsetsCompat.Type.ime())
        } else {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun onClick() {
        binding.ivChatRoomCamera.setOnClickListener {
            takePhoto()
        }

        binding.ivChatRoomPhoto.setOnClickListener {
            showPhotoFromGallery()
        }

        binding.ivChatRoomSend.setOnClickListener {
            if (!TextUtils.isEmpty(binding.edChatRoomInputMessage.text.toString().trim())) {
                sendMessageAndSaveLastMessage()
            }
        }

    }

    private fun setToolBar() {
        binding.toolbarChatRoomFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarChatRoomFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun observe() {
        chatViewModel.messageList.observe(viewLifecycleOwner, {
            chatAdapter.submitList(it)
        })

        chatViewModel.messageState.observe(viewLifecycleOwner, {
            if (it == true) hideDialog()
        })

        chatViewModel.imageFail.observe(viewLifecycleOwner, Observer {
            showSnackBar(it, true)
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listener() {

        //onTouch 回傳值，當今天事情已經做完的時候，不須繼續消費則回傳 true，否則回傳 false
        binding.rvChatRoom.setOnTouchListener { _, _ ->
            binding.edChatRoomInputMessage.clearFocus()
            binding.rvPhotoFromGallery.visibility = View.GONE
            false
        }
        binding.edChatRoomInputMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.ivChatRoomSend.visibility = View.VISIBLE
                binding.rvPhotoFromGallery.visibility = View.GONE
            } else {
                if (Build.VERSION.SDK_INT >= 30) {
                    val controller = requireActivity().window.insetsController
                    controller?.hide(WindowInsetsCompat.Type.ime())
                } else {
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view?.windowToken, 0)
                }
                binding.ivChatRoomSend.visibility = View.GONE

            }
        }
    }
    private fun takePhoto(){
        ImagePicker.with(this)
            .cameraOnly()
            .maxResultSize(1080,1080)
            .createIntent { intent: Intent ->
                resultLauncher.launch(intent)
            }
    }

    override fun onDestroy() {
        chatViewModel.resetMessageList()
        super.onDestroy()
    }

}
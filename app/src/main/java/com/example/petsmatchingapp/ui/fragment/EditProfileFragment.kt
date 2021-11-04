package com.example.petsmatchingapp.ui.fragment

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentEditProfileBinding
import com.example.petsmatchingapp.utils.CheckInternetState
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.w3c.dom.Text
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.util.jar.Manifest



class EditProfileFragment : BaseFragment(),View.OnClickListener {


    private val accountViewModel: AccountViewModel by sharedViewModel()
    private lateinit var binding: FragmentEditProfileBinding
    var selectedUri: Uri? = null



    private val startForProfileImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data
        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            selectedUri = fileUri
            if (fileUri != null) {
                Constant.loadUserImage(fileUri,binding.ivEditProfileImage)
                selectedUri = fileUri

            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentEditProfileBinding.inflate(inflater)

        accountViewModel.userDetail.observe(viewLifecycleOwner, {
            Constant.loadUserImage(it.image,binding.ivEditProfileImage)
            if (it.area != ""){
                binding.edEditArea.setText(it.area)
            }
            binding.edEditProfileName.setText(it.name)
            if (it.gender == Constant.MAN){
                binding.rbMan.isChecked = true
            }else{
                binding.rbFemale.isChecked = true
            }
        })

        accountViewModel.updateUserDetailSuccessful.observe(viewLifecycleOwner, {
            Timber.d("觀察 update")
            if (it == true){
                showSnackBar(resources.getString(R.string.update_user_profile_successful),false)
                hideDialog()
                accountViewModel.getUserDetail()
//                accountViewModel.resetUpdateSuccessful()
            }
        })

        accountViewModel.updateUserDetailFail.observe(viewLifecycleOwner,{
            hideDialog()

            showSnackBar(it,true)
            accountViewModel.resetUpdateFail()
        })



        binding.toolbarEditProfileFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarEditProfileFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        binding.btnEdit.setOnClickListener(this)
        binding.ivEditProfileCamera.setOnClickListener(this)

        return binding.root

    }


    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }else{
            requestPermission()
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),Constant.REQUEST_CODE_READ)
    }

    private fun validDataForm(): Boolean{
        return when{
            TextUtils.isEmpty(binding.edEditProfileName.text.toString().trim())  -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_name),true)
                return false
            }
            TextUtils.isEmpty(binding.edEditArea.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_area),true)
                return false
            }
            selectedUri == null && accountViewModel.userDetail.value?.image == "" -> {
                showSnackBar(resources.getString(R.string.hint_select_your_image),true)
                return false
            }
            else -> true

        }

    }

    override fun onClick(v: View?) {
       when(v){
           binding.ivEditProfileCamera ->{
               checkPermission()
           }
           binding.btnEdit ->{
               if(!CheckInternetState(requireContext()).isInternetAvailable()){
                   showSnackBar("請先確認網路情況",true)
                   return
               }
               if (validDataForm()){
                   showDialog(resources.getString(R.string.please_wait))
                   val mHashMap = HashMap<String,Any>()
                   mHashMap[Constant.NAME] = binding.edEditProfileName.text.toString().trim()
                   var gender = ""
                   gender = if (binding.rbMan.isChecked){
                       Constant.MAN
                   }else{
                       Constant.FEMALE
                   }
                   mHashMap[Constant.GENDER] = gender
                   mHashMap[Constant.PROFILE_COMPLETED] = true
                   mHashMap[Constant.AREA] = binding.edEditArea.text.toString().trim()
                   if (selectedUri != null){
                       accountViewModel.saveImageToFireStorage(mHashMap, selectedUri!!)
                   }else{
                       accountViewModel.updateUserDetailToFireStore(mHashMap)

                   }

               }
           }
       }
    }


}
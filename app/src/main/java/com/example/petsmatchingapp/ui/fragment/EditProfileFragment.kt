package com.example.petsmatchingapp.ui.fragment

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentEditProfileBinding
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.w3c.dom.Text
import timber.log.Timber
import java.lang.Exception
import java.util.jar.Manifest


class EditProfileFragment : BaseFragment(),View.OnClickListener {


    private val accountViewModel: AccountViewModel by sharedViewModel()
    private lateinit var binding: FragmentEditProfileBinding
    var mUri: String? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ uri ->
        if (uri.resultCode == Activity.RESULT_OK){
            val selectedUri = uri.data?.data
            if (selectedUri != null){

                Constant.loadUserImage(selectedUri,binding.ivEditProfileImage)
                accountViewModel.saveImageToFireStorage(requireActivity(),this,selectedUri)

            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = FragmentEditProfileBinding.inflate(inflater)

        accountViewModel.userDetail.observe(viewLifecycleOwner, Observer {
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

        binding.toolbarEditProfileFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarEditProfileFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        binding.btnEdit.setOnClickListener(this)
        binding.ivEditProfileCamera.setOnClickListener(this)

        return binding.root

    }

    fun editUserDetailSuccessful(){
        hideDialog()
        showSnackBar(resources.getString(R.string.edit_user_detail_successful),false)
        accountViewModel.getUserDetail()
    }

    fun editUserDetailFail(e: String){
        hideDialog()
        showSnackBar(e,false)
    }

    fun saveImageSuccessful(uri: Uri){
        showSnackBar(resources.getString(R.string.update_user_profile_successful),false)
        mUri = uri.toString()
        Timber.d("mUri = $mUri")

    }

    fun saveImageFail(e: String){
        showSnackBar(e,true)
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            resultLauncher.launch(
                    Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            )
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
            mUri.isNullOrBlank() && accountViewModel.userDetail.value?.image == "" -> {
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
               if (validDataForm()){
                   showDialog(resources.getString(R.string.please_wait))
                   val mHashMap = HashMap<String,Any>()
                   mHashMap[Constant.NAME] = binding.edEditProfileName.text.toString().trim()
                   mUri?.let {
                       mHashMap[Constant.IMAGE] = it
                   }
                   var gender = ""
                   if (binding.rbMan.isChecked){
                       gender = Constant.MAN
                   }else{
                       gender = Constant.FEMALE
                   }
                   mHashMap[Constant.GENDER] = gender
                   mHashMap[Constant.PROFILE_COMPLETED] = true
                   mHashMap[Constant.AREA] = binding.edEditArea.text.toString().trim()
                   accountViewModel.updateUserDetailToFireStore(mHashMap,this)

               }
           }
       }
    }


}
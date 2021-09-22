package com.example.petsmatchingapp.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentForgotaccountBinding
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ForgotAccountFragment : BaseFragment() {


    private lateinit var binding: FragmentForgotaccountBinding
    private val accountViewModel: AccountViewModel by sharedViewModel()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = FragmentForgotaccountBinding.inflate(inflater)

        binding.btnForgotSubmit.setOnClickListener{

            if (validDataForm()){
                showDialog(resources.getString(R.string.please_wait))
                val email = binding.edForgotEmail.text.toString().trim()
                accountViewModel.sendEmailToResetPassword(this,email)
            }
        }

        binding.toolbarForgotFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarForgotFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }



        return binding.root
    }


    private fun validDataForm(): Boolean{

       return when{
           TextUtils.isEmpty(binding.edForgotEmail.text.toString().trim()) -> {
               showSnackBar(resources.getString(R.string.hint_enter_your_email),true)
               false
           }
           else -> true
       }

    }

    fun sendEmailSuccessful(){
        hideDialog()
        showSnackBar(resources.getString(R.string.forgotAccount_already_send_email),false)
    }

    fun sendEmailFail(e: String){
        hideDialog()
        showSnackBar(e,true)
    }


}
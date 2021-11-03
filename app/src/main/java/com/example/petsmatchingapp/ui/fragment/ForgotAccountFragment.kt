package com.example.petsmatchingapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentForgotaccountBinding
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class ForgotAccountFragment : BaseFragment() {


    private lateinit var binding: FragmentForgotaccountBinding
    private val accountViewModel: AccountViewModel by sharedViewModel()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {



        binding = FragmentForgotaccountBinding.inflate(inflater)

        accountViewModel.sendEmailToReset.observe(viewLifecycleOwner,{
            if (it == Constant.TRUE){
                hideDialog()
                showSnackBar(resources.getString(R.string.forgotAccount_already_send_email),false)
            }else{
                hideDialog()
                showSnackBar(it,true)
            }
        })
        binding.btnForgotSubmit.setOnClickListener{

            if (validDataForm()){
                showDialog(resources.getString(R.string.please_wait))
                val email = binding.edForgotEmail.text.toString().trim()
                accountViewModel.sendEmailToResetPassword(email)
            }
        }

        binding.toolbarForgotFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarForgotFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }



        return binding.root
    }


    private fun validDataForm(): Boolean {

        return if (TextUtils.isEmpty(binding.edForgotEmail.text.toString().trim())) {
            showSnackBar(resources.getString(R.string.hint_enter_your_email), true)
            false
        } else {
            true
        }
    }
}
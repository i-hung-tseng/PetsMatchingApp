package com.example.petsmatchingapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentLoginBinding
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

class LoginFragment : BaseFragment() {


    private lateinit var binding: FragmentLoginBinding
    private val accountViewModel: AccountViewModel by sharedViewModel()
    private lateinit var nav: NavController


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater)


        setClickEvent()

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(requireActivity(), MatchingActivity::class.java))
            requireActivity().finish()
        }


        nav = findNavController()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = requireActivity().window.insetsController
            controller?.hide(WindowManager.LayoutParams.TYPE_STATUS_BAR)
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setClickEvent()

        accountViewModel.loginState.observe(viewLifecycleOwner, {
            if (it == Constant.TRUE) {
                hideDialog()
                showSnackBar(resources.getString(R.string.msg_login_successful), false)
                requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        MatchingActivity::class.java
                    )
                )
                requireActivity().finish()
            } else {
                hideDialog()
                showSnackBar(it, true)
            }
        })


        return binding.root
    }

    // TODO: 2021/10/29 全部點擊都在這邊處理
    private fun setClickEvent() {
        binding.btnLogin.setOnClickListener {
            if (validDataForm()) {
                showDialog(resources.getString(R.string.please_wait))
                val email = binding.edEmail.text.toString().trim()
                val paw = binding.edPassword.text.toString().trim()
                accountViewModel.loginWithEmailAndPassword(email, paw)
            }

        }
        binding.tvRegister.setOnClickListener {
            nav.navigate(R.id.action_loginFragment_to_registerFragment)


        }
        binding.tvForgotPassword.setOnClickListener {
            nav.navigate(R.id.action_loginFragment_to_forgotAccountFragment)

        }
    }


    private fun validDataForm(): Boolean {

        return when {

            TextUtils.isEmpty(binding.edEmail.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_email), true)
                return false
            }
            TextUtils.isEmpty(binding.edPassword.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_password), true)
                return false
            }
            else -> true
        }

    }

}
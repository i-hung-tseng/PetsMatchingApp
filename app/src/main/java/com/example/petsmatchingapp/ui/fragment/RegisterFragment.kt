package com.example.petsmatchingapp.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentRegisterBinding
import com.example.petsmatchingapp.model.User
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class RegisterFragment : BaseFragment() {


    private lateinit var binding: FragmentRegisterBinding
    private lateinit var nav: NavController
    private val accountViewModel: AccountViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentRegisterBinding.inflate(inflater)
        nav = findNavController()
        binding.toolbarRegisterFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarRegisterFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        setClickEvent()
        accountViewModel.registerState.observe(viewLifecycleOwner,{
            if (it == Constant.TRUE){
                hideDialog()
                showSnackBar(resources.getString(R.string.register_success), false)
                nav.navigate(R.id.action_registerFragment_to_loginFragment)
            }else{
                hideDialog()
                showSnackBar(it, true)

            }
        })


        return binding.root
    }

    private fun setClickEvent() {
        binding.btnRegister.setOnClickListener {

            if (validDataForm()) {
                showDialog(resources.getString(R.string.please_wait))
                val user = User(
                    name = binding.edRegisterName.text.toString().trim(),
                    email = binding.edRegisterEmail.text.toString().trim(),
                    password = binding.edRegisterPassword.text.toString().trim(),
                )

                accountViewModel.registerWithEmailAndPassword( user)

            }

        }
        binding.tvAlreadyHaveAccount.setOnClickListener {
            nav.navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }


    private fun validDataForm(): Boolean {
        val view = arrayOf(
            binding.edRegisterName,
            binding.edRegisterEmail,
            binding.edRegisterPassword,
            binding.edRegisterPasswordAgain
        )
        val snackContent = arrayOf(
            R.string.hint_enter_your_name,
            R.string.hint_enter_your_email,
            R.string.hint_enter_your_password,
            R.string.hint_enter_again_password
        )

        view.forEachIndexed { index, jfEditText ->

            if (jfEditText.text.isNullOrBlank()) {
                showSnackBar(resources.getString(snackContent[index]), true)
                return false
            }
        }

        return if (binding.edRegisterPassword.text.toString() != binding.edRegisterPasswordAgain.text.toString()) {
            showSnackBar(resources.getString(R.string.hint_do_not_enter_same_password), true)
            false
        } else {
            true
        }

    }

}
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
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class RegisterFragment : BaseFragment(),View.OnClickListener {


    private lateinit var binding: FragmentRegisterBinding
    private lateinit var nav: NavController
    private val accountViewModel: AccountViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {




        binding = FragmentRegisterBinding.inflate(inflater)
        nav = findNavController()
        binding.toolbarRegisterFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarRegisterFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnRegister.setOnClickListener(this)
        binding.tvRegisterLogin.setOnClickListener(this)
        return binding.root
    }


    private fun validDataForm(): Boolean{

        return when{

            TextUtils.isEmpty(binding.edRegisterName.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_name),true)
                return false
            }
            TextUtils.isEmpty(binding.edRegisterEmail.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_email),true)
                return false
            }
            TextUtils.isEmpty(binding.edRegisterPassword.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_password),true)
                return false
            }
            TextUtils.isEmpty(binding.edRegisterPasswordAgain.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_again_password),true)
                return false
            }

            binding.edRegisterPasswordAgain.text.toString().trim() != binding.edRegisterPassword.text.toString().trim() ->{
                showSnackBar(resources.getString(R.string.hint_do_not_enter_same_password),true)
                return false
            }

            else -> true

        }
    }

    override fun onClick(v: View?) {
        when(v){
            binding.btnRegister -> {
                if (validDataForm()){
                    showDialog(resources.getString(R.string.please_wait))
                    val user = User(
                        name = binding.edRegisterName.text.toString().trim(),
                        email = binding.edRegisterEmail.text.toString().trim(),
                        password = binding.edRegisterPassword.text.toString().trim(),
                    )

                    accountViewModel.registerWithEmailAndPassword(this,user)

                }
            }
            binding.tvRegisterLogin -> {
                nav.navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }


    fun registerSuccessful(){
        hideDialog()
        showSnackBar(resources.getString(R.string.register_success),false)
        nav.navigate(R.id.action_registerFragment_to_loginFragment)
    }

    fun registerFail(e: String){
        hideDialog()
        showSnackBar(e,true)
    }

}
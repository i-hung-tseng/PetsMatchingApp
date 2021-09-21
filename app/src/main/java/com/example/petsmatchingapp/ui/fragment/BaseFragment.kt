package com.example.petsmatchingapp.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.petsmatchingapp.R
import com.google.android.material.snackbar.Snackbar


open class BaseFragment : Fragment() {

    private lateinit var mProgressDialog: Dialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_base, container, false)
    }



    fun showDialog(text: String){
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.findViewById<TextView>(R.id.tv_progress).text = text

        mProgressDialog.show()

    }

    fun hideDialog(){
        mProgressDialog.dismiss()
    }

    fun showSnackBar(message: String, error: Boolean){
        val snackBar = Snackbar.make(requireActivity().findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        if (error){
            snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorSnackBarError))
        }else{
            snackBarView.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.colorSnackBarSuccess))
        }
        snackBar.show()
    }


}
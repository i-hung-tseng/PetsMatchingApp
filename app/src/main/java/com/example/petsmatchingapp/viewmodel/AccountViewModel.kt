package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsmatchingapp.model.User
import com.example.petsmatchingapp.ui.fragment.EditProfileFragment
import com.example.petsmatchingapp.ui.fragment.ForgotAccountFragment
import com.example.petsmatchingapp.ui.fragment.LoginFragment
import com.example.petsmatchingapp.ui.fragment.RegisterFragment
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

class AccountViewModel: ViewModel() {




    private val _userDetail = MutableLiveData<User>()
    val userDetail: LiveData<User>
    get() = _userDetail







    fun getCurrentUID(): String?{
        return FirebaseAuth.getInstance().currentUser?.uid
    }







    fun loginWithEmailAndPassword(fragment: LoginFragment, email: String, paw: String){


        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,paw)
                .addOnSuccessListener {
                    fragment.loginSuccessful()
                }
                .addOnFailureListener {
                    Timber.d("Error while logging cause $it")
                    fragment.loginFail(it.toString())
                }
    }

    fun registerWithEmailAndPassword(fragment: RegisterFragment, user: User){

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email,user.password)
                    .addOnSuccessListener {

                        val newUser = User(
                            name = user.name,
                            email = user.email,
                            password = user.password,
                            id = it.user!!.uid
                        )

                        addUserDetailsToFireStore(fragment,newUser)
                    }
                    .addOnFailureListener {
                        fragment.registerFail(it.toString())
                }
    }

    private fun addUserDetailsToFireStore(fragment: RegisterFragment, user: User){

           //這邊創立 Firestore的 instance，並且collection內指定集合為user，集合裡面填string，我們用Constant來確保每次呼叫都不會拼錯字

            FirebaseFirestore.getInstance().collection(Constant.USER)
            //這邊指定 document的id為剛剛auth回傳的 uid
            .document(user.id)
            .set(user, SetOptions.merge())
            .addOnSuccessListener{
                fragment.registerSuccessful()
            }
            .addOnFailureListener{
                fragment.registerFail(it.toString())
            }

    }

    fun sendEmailToResetPassword(fragment: ForgotAccountFragment,email: String){


                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        fragment.sendEmailSuccessful()
                    }
                    .addOnFailureListener {
                        fragment.sendEmailFail(it.toString())
                    }

        }


    fun getUserDetail(){

        getCurrentUID()?.let {
            FirebaseFirestore.getInstance().collection(Constant.USER)
                .document(it)
                .get()
                .addOnSuccessListener {
                    _userDetail.postValue(it.toObject(User::class.java))
                }
                .addOnFailureListener {
                    Timber.d("Error while getUserDetail cause$it")
                }
        }
    }

    fun signOut(){
        FirebaseAuth.getInstance().signOut()
    }

    fun updateUserDetailToFireStore(mHashMap: HashMap<String,Any>, fragment: EditProfileFragment ){

        getCurrentUID()?.let {
            FirebaseFirestore.getInstance().collection(Constant.USER)
                .document(it)
                    .update(mHashMap)
                    .addOnSuccessListener {
                        fragment.editUserDetailSuccessful()
                    }
                    .addOnFailureListener {
                        fragment.editUserDetailFail(it.toString())
                    }
        }

    }

    fun saveImageToFireStorage(activity: Activity, fragment: EditProfileFragment,uri: Uri){

        val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(Constant.USER_IMAGE + "_" + System.currentTimeMillis() + "_" + Constant.getFileExtension(activity, uri))
        sdf.putFile(uri)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener {
                            fragment.saveImageSuccessful(it)
                        }
                        ?.addOnFailureListener {
                            fragment.saveImageFail(it.toString())
                        }

                }
                .addOnFailureListener {
                    fragment.saveImageFail(it.toString())
                }

    }
}
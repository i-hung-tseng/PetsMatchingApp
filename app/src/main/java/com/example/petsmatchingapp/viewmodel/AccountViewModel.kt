package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.net.Uri
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsmatchingapp.model.CurrentUser
import com.example.petsmatchingapp.model.User
import com.example.petsmatchingapp.ui.fragment.EditProfileFragment
import com.example.petsmatchingapp.ui.fragment.ForgotAccountFragment
import com.example.petsmatchingapp.ui.fragment.LoginFragment
import com.example.petsmatchingapp.ui.fragment.RegisterFragment
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.utils.SingleLiveEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.time.zone.ZoneOffsetTransitionRule

class AccountViewModel: ViewModel() {




    private val _userDetail = MutableLiveData<User>()
    val userDetail: LiveData<User> = _userDetail

    private val _loginState = SingleLiveEvent<String>()
    val loginState: SingleLiveEvent<String> = _loginState

    private val _registerState = SingleLiveEvent<String>()
    val registerState: SingleLiveEvent<String> = _registerState

    private val _sendEmailToReset = SingleLiveEvent<String>()
    val sendEmailToReset: SingleLiveEvent<String> = _sendEmailToReset

    private val _selectedUserDetail = MutableLiveData<User>()
    val selectedUserDetail: LiveData<User> = _selectedUserDetail

    private val _updateUserDetailSuccessful = SingleLiveEvent<Boolean>()
    val updateUserDetailSuccessful: SingleLiveEvent<Boolean>
    get() = _updateUserDetailSuccessful

    private val _updateUserDetailFail = MutableLiveData<String>()
    val updateUserDetailFail: LiveData<String>
    get() = _updateUserDetailFail


    // TODO: 2021/10/29 丟不同的資料，return 不同的 REF
    private fun getFireStoreCollection(): CollectionReference {
            return Firebase.firestore.collection(Constant.USER)
    }



    fun getCurrentUID(){
        CurrentUser.currentUser = FirebaseAuth.getInstance().currentUser
    }



    fun loginWithEmailAndPassword( email: String, paw: String){


        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,paw)
                .addOnSuccessListener {
                    _loginState.postValue(Constant.TRUE)
                }
                .addOnFailureListener {
                    _loginState.postValue(it.toString())
                }
    }

    fun registerWithEmailAndPassword(user: User){

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.email,user.password)
                    .addOnSuccessListener {
                        val newUser = User(
                            name = user.name,
                            email = user.email,
                            password = user.password,
                            id = it.user!!.uid
                        )
                        addUserDetailsToFireStore(newUser)
                    }
                    .addOnFailureListener {
                        _registerState.postValue(it.toString())
                }
    }

    private fun addUserDetailsToFireStore(user: User){

           //這邊創立 Firestore的 instance，並且collection內指定集合為user，集合裡面填string，我們用Constant來確保每次呼叫都不會拼錯字
            getFireStoreCollection()
            //這邊指定 document的id為剛剛auth回傳的 uid
            .document(user.id)
            .set(user, SetOptions.merge())
            .addOnSuccessListener{
                _registerState.postValue(Constant.TRUE)
            }
            .addOnFailureListener{
                _registerState.postValue(it.toString())
            }

    }

    fun sendEmailToResetPassword(email: String){


                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        _sendEmailToReset.postValue(Constant.TRUE)
                    }
                    .addOnFailureListener {
                        _sendEmailToReset.postValue(it.toString())
                    }

        }


    // TODO: 2021/11/1 這個不太好，代表誰都可以讀別人的個人資料
    fun getUserDetail(){
        CurrentUser.currentUser?.uid?.let {
                 getFireStoreCollection()
                .document(it)
                .get()
                .addOnSuccessListener {
                    _userDetail.postValue(it.toObject(User::class.java))
                    Timber.d("測試 ${it.toObject(User::class.java)}")
                }
                .addOnFailureListener {
                    Timber.d("Error while getUserDetail cause$it")
                }
        }
    }

    fun signOut(){
        FirebaseAuth.getInstance().signOut()
    }

    fun updateUserDetailToFireStore(mHashMap: HashMap<String,Any>){

        CurrentUser.currentUser?.uid?.let {
            getFireStoreCollection()
                .document(it)
                    .update(mHashMap)
                    .addOnSuccessListener {
                        Timber.d("測試 updateUserDetailToFireStore")
                        _updateUserDetailSuccessful.postValue(true)
                    }
                    .addOnFailureListener {
                        _updateUserDetailFail.postValue(it.toString())
                        Timber.d("測試 updateUserDetailToFireStore fail $it")

                    }
        }

    }

    fun saveImageToFireStorage(mHashMap: HashMap<String,Any>,uri: Uri){

        val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(Constant.USER_IMAGE + "_" + System.currentTimeMillis())
        sdf.putFile(uri)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl
                        ?.addOnSuccessListener {
                            mHashMap[Constant.IMAGE] = it.toString()
                            updateUserDetailToFireStore(mHashMap)
                        }
                        ?.addOnFailureListener {
                            _updateUserDetailFail.postValue(it.toString())
                        }

                }
                .addOnFailureListener {
                    _updateUserDetailFail.postValue(it.toString())

                }

    }

    fun findUserDetailByID(user_id: String){

        getFireStoreCollection().document(user_id).get()
            .addOnSuccessListener {
                _selectedUserDetail.postValue(it.toObject(User::class.java))
            }
            .addOnFailureListener {

            }
    }

    fun resetUpdateSuccessful(){
        _updateUserDetailSuccessful.postValue(null)
    }
    fun resetUpdateFail(){
        _updateUserDetailFail.postValue(null)
    }
}
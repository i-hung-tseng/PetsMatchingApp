package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.AddInvitationFragment
import com.example.petsmatchingapp.ui.fragment.DashboardFragment
import com.example.petsmatchingapp.ui.fragment.EditProfileFragment
import com.example.petsmatchingapp.ui.fragment.HomeFragment
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ControllableTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber

class MatchingViewModel: ViewModel() {



    private val _selectedInvitation = MutableLiveData<Invitation>()
    val selectedInvitation: LiveData<Invitation>
    get() = _selectedInvitation



    private val _homeInvitationList =  MutableLiveData<List<Invitation>>()
    val homeInvitationList: LiveData<List<Invitation>>
    get() = _homeInvitationList


    private val _allInvitationList =  MutableLiveData<List<Invitation>>()
    val allInvitationList: LiveData<List<Invitation>>
    get() = _allInvitationList

    fun saveImageToFireStorage(activity: Activity, fragment: AddInvitationFragment, uri: Uri){

        val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(Constant.PET_IMAGE + "_" + System.currentTimeMillis() + "_" + Constant.getFileExtension(activity, uri))
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

    fun addInvitationToFireStore(fragment:AddInvitationFragment,invitation: Invitation){
        Firebase.firestore.collection(Constant.INVITATION)
            .add(invitation)
            .addOnSuccessListener {
                val mHashMap = HashMap<String,Any>()
                mHashMap[Constant.ID] = it.id
                it.update(mHashMap)
                    .addOnSuccessListener {
                        fragment.addInvitationSuccess()
                    }
                    .addOnFailureListener {
                        fragment.addInvitationFail(it.toString())
                    }
            }
            .addOnFailureListener {
                fragment.addInvitationFail(it.toString())
            }
    }

    fun getCurrentUserInvitation(fragment:HomeFragment,id: String){
        Firebase.firestore.collection(Constant.INVITATION)
            .get()
            .addOnSuccessListener {
                val currentInvitationList = mutableListOf<Invitation>()
                for (i in it.documents){
                    val model = i.toObject(Invitation::class.java)
                    if (model?.user_id == id){
                        currentInvitationList.add(model)
                    }
                }
                _homeInvitationList.postValue(currentInvitationList)

            }
            .addOnFailureListener {
                fragment.getCurrentUserInvitationListFail(it.toString())
            }
    }

    fun deleteInvitation(id: String,fragment: HomeFragment){
        Firebase.firestore.collection(Constant.INVITATION)
            .document(id)
            .delete()
            .addOnSuccessListener {
                fragment.deleteInvitationSuccess()
            }
            .addOnFailureListener {
                fragment.deleteInvitationFail(it.toString())
            }
    }

    fun addSelectedInvitationToLiveData(invitation: Invitation){
        _selectedInvitation.postValue(invitation)
    }

    fun queryAllInvitation(type: String, area: String){

        Timber.d("test type:$type area:$area")

        val sdf = Firebase.firestore.collection(Constant.INVITATION)
    when{
        type.isNotEmpty()&&area.isEmpty()-> {
            Timber.d("test type不空")
                sdf
                .whereEqualTo(Constant.PET_TYPE,type)
                .get()
                    .addOnSuccessListener {
                        val list = mutableListOf<Invitation>()
                        for (i in it.documents){
                            val model = i.toObject(Invitation::class.java)
                            Timber.d("test model = $model model.area ${model?.area}")
                            if (model != null) {
                                list.add(model)
                            }
                            _allInvitationList.postValue(list)
                            }
                        }
                    .addOnFailureListener {
                        Timber.d("getAll $it")
                    }
                    }
         type.isNotEmpty()&&area.isNotEmpty() ->{
             Timber.d("test 都不空 ")
              sdf
                  .whereEqualTo(Constant.PET_TYPE,type)
                  .whereEqualTo(Constant.AREA,area)
                  .get()
                  .addOnSuccessListener {
                      val list = mutableListOf<Invitation>()
                      for(i in it.documents){
                          val model = i.toObject(Invitation::class.java)
                          model?.let { list.add(it) }
                      }
                      Timber.d("test 都不空 succ")
                      _allInvitationList.postValue(list)

                  }
                  .addOnFailureListener {
                      Timber.d("getAll $it")
                  }
         }
          type.isEmpty()&&area.isEmpty() -> {
              Timber.d("test 都空")
              sdf
                  .get()
                  .addOnSuccessListener {
                      val list = mutableListOf<Invitation>()
                      for (i in it.documents){
                          val model = i.toObject(Invitation::class.java)
                          model?.let { list.add(model) }
                      }
                      Timber.d("test 都空succ")
                      _allInvitationList.postValue(list)
                  }
          }

        area.isNotEmpty()&&type.isEmpty() -> {
            Timber.d("test area不空")
            sdf
                .whereEqualTo(Constant.AREA,area)
                .get()
                .addOnSuccessListener {
                    val list = mutableListOf<Invitation>()
                    for (i in it.documents){
                        val model = i.toObject(Invitation::class.java)
                        model?.let {
                            list.add(model)
                        }
                    }
                    Timber.d("test area不空 succ")
                    _allInvitationList.postValue(list)

                }
        }
        else -> {
            Timber.d("test else")
        }



    }


    }

    fun getAllInvitation(userID: String,fragment: DashboardFragment){
        Firebase.firestore.collection(Constant.INVITATION)
            .get()
            .addOnSuccessListener {
                val list = mutableListOf<Invitation>()
                for (i in it.documents){
                    val model = i.toObject(Invitation::class.java)
                    if (model != null && model.user_id != userID){
                        list.add(model)
                    }
                }
                _allInvitationList.postValue(list)
            }
            .addOnFailureListener {
                fragment.getAllInvitationFail(it.toString())
            }
    }



}
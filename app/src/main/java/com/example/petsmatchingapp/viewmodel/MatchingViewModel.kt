package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.AddInvitationFragment
import com.example.petsmatchingapp.ui.fragment.DashboardFragment
import com.example.petsmatchingapp.ui.fragment.HomeFragment
import com.example.petsmatchingapp.ui.fragment.SearchFragment
import com.example.petsmatchingapp.utils.Constant
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class MatchingViewModel : ViewModel() {


    private val _selectedInvitation = MutableLiveData<Invitation>()
    val selectedInvitation: LiveData<Invitation>
        get() = _selectedInvitation


    private val _homeInvitationList = MutableLiveData<List<Invitation>>()
    val homeInvitationList: LiveData<List<Invitation>>
        get() = _homeInvitationList


    private val _dashboardInvitationList = MutableLiveData<List<Invitation>>()
    val dashboardInvitationList: LiveData<List<Invitation>>
        get() = _dashboardInvitationList

    private val _allInvitationList = MutableLiveData<List<Invitation>>()
    val allInvitationList: LiveData<List<Invitation>>
        get() = _allInvitationList

    private val _invitation_add_state = MutableLiveData<Boolean>()
    val invitation_add_state: LiveData<Boolean>
    get() = _invitation_add_state

    private val _saveImage_fail = MutableLiveData<String>()
    val saveImage_fail: LiveData<String>
    get() = _saveImage_fail


    fun saveImageToFireStorage( typeList: List<String>,uriList: List<Uri>,invitation: Invitation) {


        val newList = mutableListOf<String>()
        Timber.d("test uriList Size: ${uriList.size}")
       for (i in 0 until typeList.size){
           Timber.d("test enter saveImage in viewmodel i : $i")
           val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(
               Constant.PET_IMAGE + "_" + System.currentTimeMillis() + "_" + typeList[i]
           )
           sdf.putFile(uriList[i])
               .addOnSuccessListener { it ->
                   it.metadata?.reference?.downloadUrl
                       ?.addOnSuccessListener { uri ->
                       val uriString = uri.toString()
                       newList.add(uriString)
                           if (i == uriList.size-1){
                               val newInvitation = Invitation(
                                   user_id = invitation.user_id,
                                   user_name = invitation.user_name,
                                   user_image = invitation.user_image,
                                   pet_type = invitation.pet_type,
                                   pet_type_description = invitation.pet_type_description,
                                   area = invitation.area,
                                   date_place = invitation.date_place,
                                   date_time = invitation.date_time,
                                   note = invitation.note,
                                   update_time = invitation.update_time,
                                   photoUriList = newList
                               )
                               addInvitationToFireStore(newInvitation)
                           }

                       }
                       ?.addOnFailureListener {
                        _saveImage_fail.postValue(it.toString())

                       }


               }
               .addOnFailureListener {
                   _saveImage_fail.postValue(it.toString())


               }
       }

    }

    fun addInvitationToFireStore(invitation: Invitation) {
        Timber.d("test addinvitation ${invitation.photoUriList?.size}")
        Firebase.firestore.collection(Constant.INVITATION)
            .add(invitation)
            .addOnSuccessListener {
                val mHashMap = HashMap<String, Any>()
                mHashMap[Constant.ID] = it.id
                it.update(mHashMap)
                    .addOnSuccessListener {
                        _invitation_add_state.postValue(true)
                    }
                    .addOnFailureListener {
                        _invitation_add_state.postValue(false)

                    }
            }
            .addOnFailureListener {
                _invitation_add_state.postValue(false)

            }
    }

    fun getCurrentUserInvitation(fragment: HomeFragment, id: String) {
        Firebase.firestore.collection(Constant.INVITATION)
            .get()
            .addOnSuccessListener {
                val currentInvitationList = mutableListOf<Invitation>()
                for (i in it.documents) {
                    val model = i.toObject(Invitation::class.java)
                    if (model?.user_id == id) {
                        currentInvitationList.add(model)
                    }
                }
                _homeInvitationList.postValue(currentInvitationList)

            }
            .addOnFailureListener {
                fragment.getCurrentUserInvitationListFail(it.toString())

            }
    }

    fun deleteInvitation(id: String, fragment: HomeFragment) {
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


    fun getAllInvitation(userID: String, fragment: DashboardFragment) {
        Firebase.firestore.collection(Constant.INVITATION)
            .get()
            .addOnSuccessListener {
                Timber.d("enter get all")
                val list = mutableListOf<Invitation>()
                for (i in it.documents) {
                    val model = i.toObject(Invitation::class.java)
                    if (model != null && model.user_id != userID) {
                        list.add(model)
                    }

                }
                Timber.d("list ${list.size}")
                _allInvitationList.postValue(list)
            }
            .addOnFailureListener {
                fragment.getAllInvitationFail(it.toString())
                Timber.d("enter get all fail")
            }
    }

    fun searchInvitation(currentUserId: String,areaList: MutableList<String>,petTypeList: MutableList<String>,result_sort: String,fragment: SearchFragment){

        Timber.d("areaList:$areaList petTypeList:$petTypeList result_sort:$result_sort")
        if (result_sort == Constant.RESULT_SORT_UPDATE_DAY){

            Timber.d("enter 邀約更新")

            when{
                areaList.isNotEmpty() && petTypeList.isEmpty()  ->{
                    Timber.d("enter 地區搜尋")
                    Firebase.firestore.collection(Constant.INVITATION)
                        .whereIn(Constant.AREA,areaList)
                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (i in it.documents){
                                val model = i.toObject(Invitation::class.java)
                                if (model?.user_id != currentUserId){
                                    list.add(model!!)
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)
                        }
                        .addOnFailureListener {
                            Timber.d("搜尋fail $it")
                            fragment.searchInvitationFail(it.toString())
                        }
                }
                areaList.isEmpty() && petTypeList.isNotEmpty() -> {
                    Firebase.firestore.collection(Constant.INVITATION)
                        .whereIn(Constant.PET_TYPE,petTypeList)
                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (i in it.documents){
                                val model = i.toObject(Invitation::class.java)
                                if (model?.user_id != currentUserId){
                                    list.add(model!!)
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)
                        }
                        .addOnFailureListener {
                            fragment.searchInvitationFail(it.toString())
                        }
                }

                areaList.isNotEmpty() && petTypeList.isNotEmpty() ->{
                    val ref = Firebase.firestore.collection(Constant.INVITATION)
                    ref.whereIn(Constant.AREA,areaList)
                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (eachModel in it.documents){
                                val model = eachModel.toObject(Invitation::class.java)
                                for (type in petTypeList){
                                    model?.let {
                                        if (model.pet_type == type && model.user_id != currentUserId){
                                            list.add(model)
                                        }
                                    }
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)

                        }
                        .addOnFailureListener {
                            fragment.searchInvitationFail(it.toString())
                        }
                }
                else -> {
                    Firebase.firestore.collection(Constant.INVITATION)
                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (i in it.documents){
                                val model = i.toObject(Invitation::class.java)
                                model?.let {
                                    if (model.user_id != currentUserId){
                                        list.add(it)
                                    }
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)
                        }
                        .addOnFailureListener {
                            fragment.searchInvitationFail(it.toString())
                        }
                }


            }
        }else{
            Timber.d("enter 邀約日期")
            when{
                areaList.isNotEmpty() && petTypeList.isEmpty()  ->{
                    Timber.d("enter 邀約 地區")
                    Firebase.firestore.collection(Constant.INVITATION)
                        .whereIn(Constant.AREA,areaList)
                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (i in it.documents){
                                val model = i.toObject(Invitation::class.java)
                                model?.let {
                                    if (model.user_id != currentUserId){
                                        list.add(it)
                                    }
                                }
                                Timber.d("model: $model")
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)
                        }
                        .addOnFailureListener {
                            Timber.d("搜尋fail $it")
                            fragment.searchInvitationFail(it.toString())
                        }
                }
                areaList.isEmpty() && petTypeList.isNotEmpty() -> {
                    Firebase.firestore.collection(Constant.INVITATION)
                        .whereIn(Constant.PET_TYPE,petTypeList)
                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (i in it.documents){
                                val model = i.toObject(Invitation::class.java)
                                model?.let {
                                    if (model.user_id != currentUserId){
                                        list.add(it)
                                    }
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)
                        }
                        .addOnFailureListener {
                            fragment.searchInvitationFail(it.toString())
                        }
                }

                areaList.isNotEmpty() && petTypeList.isNotEmpty() ->{
                    val ref = Firebase.firestore.collection(Constant.INVITATION)
                    ref.whereIn(Constant.AREA,areaList)
                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (eachModel in it.documents){
                                val model = eachModel.toObject(Invitation::class.java)
                                for (type in petTypeList){
                                    model?.let {
                                        if (model.pet_type == type && model.user_id != currentUserId){
                                            list.add(model)
                                        }
                                    }
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)

                        }
                        .addOnFailureListener {
                            fragment.searchInvitationFail(it.toString())
                        }
                }
                else -> {
                    Firebase.firestore.collection(Constant.INVITATION)
                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener {
                            val list = mutableListOf<Invitation>()
                            for (i in it.documents){
                                val model = i.toObject(Invitation::class.java)
                                model?.let {
                                    if (model.user_id != currentUserId){
                                        list.add(it)
                                    }
                                }
                            }
                            _dashboardInvitationList.postValue(list)
                            fragment.searchInvitationSuccess(list.size)
                        }
                        .addOnFailureListener {
                            fragment.searchInvitationFail(it.toString())
                        }
                }


            }

        }

    }

    fun resetAddInvitationState(){
        _invitation_add_state.postValue(null)
    }

    fun resetSaveImageState(){
        _saveImage_fail.postValue(null)
    }
}
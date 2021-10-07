package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.app.DownloadManager
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber

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


    fun saveImageToFireStorage(activity: Activity, fragment: AddInvitationFragment, uri: Uri) {

        val sdf: StorageReference = FirebaseStorage.getInstance().reference.child(
            Constant.PET_IMAGE + "_" + System.currentTimeMillis() + "_" + Constant.getFileExtension(
                activity,
                uri
            )
        )
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

    fun addInvitationToFireStore(fragment: AddInvitationFragment, invitation: Invitation) {
        Firebase.firestore.collection(Constant.INVITATION)
            .add(invitation)
            .addOnSuccessListener {
                val mHashMap = HashMap<String, Any>()
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


}
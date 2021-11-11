package com.example.petsmatchingapp.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petsmatchingapp.model.CurrentUser
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.fragment.AddInvitationFragment
import com.example.petsmatchingapp.ui.fragment.DashboardFragment
import com.example.petsmatchingapp.ui.fragment.HomeFragment
import com.example.petsmatchingapp.ui.fragment.SearchFragment
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.utils.SingleLiveEvent
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
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

    private val _backToDashboard = MutableLiveData<Boolean>()
    val backToDashboard: LiveData<Boolean> = _backToDashboard


    private val _getAllInvitationState = SingleLiveEvent<String>()
    val getAllInvitationState: SingleLiveEvent<String> = _getAllInvitationState

    private val _deletedState = SingleLiveEvent<String>()
    val deletedState: SingleLiveEvent<String> = _deletedState

    private val _homeInvitationList = MutableLiveData<List<Invitation>>()
    val homeInvitationList: LiveData<List<Invitation>>
        get() = _homeInvitationList

    private val _getCurrentUserInvitationState = SingleLiveEvent<String>()
    val getCurrentUserInvitationState: SingleLiveEvent<String> = _getCurrentUserInvitationState


    // TODO: 2021/11/4 如果這邊改成 Single 則不會進到 Observe
    private val _dashboardInvitationList =  MutableLiveData<List<Invitation>>()
    val dashboardInvitationList: LiveData<List<Invitation>>
        get() = _dashboardInvitationList

    private val _allInvitationList = MutableLiveData<List<Invitation>>()
    val allInvitationList: MutableLiveData<List<Invitation>>
        get() = _allInvitationList

    private val _invitation_add_state = SingleLiveEvent<Boolean>()
    val invitation_add_state: LiveData<Boolean>
    get() = _invitation_add_state

    private val _saveImage_fail = MutableLiveData<String>()
    val saveImage_fail: LiveData<String>
    get() = _saveImage_fail



    init {
        firebaseDataChangeListener()
        currentUserInvitationOnDataChangeListener()
    }
    private fun getFirebaseCollection(s: String): CollectionReference {
        return Firebase.firestore.collection(s)
    }

    fun saveImageToFireStorage( typeList: List<String>,uriList: List<Uri>,invitation: Invitation) {


        val newList = mutableListOf<String>()
       for (i in typeList.indices){
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

    private fun addInvitationToFireStore(invitation: Invitation) {
        getFirebaseCollection(Constant.INVITATION)
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

//    fun getCurrentUserInvitation() {
//            getFirebaseCollection(Constant.INVITATION)
//            .get()
//            .addOnSuccessListener {
//                val currentInvitationList = mutableListOf<Invitation>()
//                for (i in it.documents) {
//                    val model = i.toObject(Invitation::class.java)
//                    if (model?.user_id == CurrentUser.currentUser?.uid) {
//                        if (model != null) {
//                            currentInvitationList.add(model)
//                        }
//                    }
//                }
//                _homeInvitationList.postValue(currentInvitationList)
//
//            }
//            .addOnFailureListener {
//                _getCurrentUserInvitationState.postValue(it.toString())
//            }
//    }


    fun currentUserInvitationOnDataChangeListener() {
        Timber.d("盡到觀察個人約散資訊")
        getFirebaseCollection(Constant.INVITATION)
            .addSnapshotListener { value, error ->
                if (error != null){
                    return@addSnapshotListener
                }
                //注意這邊不能用 Empty，因為當刪除掉資料時，就會變成 empty，所以要改用 null
                if (value != null){
                    Timber.d("value != null")
                    val list: MutableList<Invitation> = mutableListOf()
                    for (i in value.documents){
                        val model = i.toObject(Invitation::class.java)
                        if (model != null && model.user_id == CurrentUser.currentUser?.uid) {
                            list.add(model)
                        }
                    }
                    _homeInvitationList.postValue(list)
                }
            }
    }

    fun deleteInvitation(id: String) {
        Firebase.firestore.collection(Constant.INVITATION)
            .document(id)
            .delete()
            .addOnSuccessListener {
                _deletedState.postValue(Constant.TRUE)

            }

            .addOnFailureListener {
                _deletedState.postValue(it.toString())
            }
    }

    fun addSelectedInvitationToLiveData(invitation: Invitation){
        _selectedInvitation.postValue(invitation)
    }


//    fun getAllInvitation(userID: String ) {
//        Firebase.firestore.collection(Constant.INVITATION)
//            .get()
//            .addOnSuccessListener {
//                val list = mutableListOf<Invitation>()
//                for (i in it.documents) {
//                    val model = i.toObject(Invitation::class.java)
//                    if (model != null && model.user_id != userID) {
//                        list.add(model)
//                    }
//                }
//                _allInvitationList.postValue(list)
//            }
//            .addOnFailureListener {
//                _getAllInvitationState.postValue(it.toString())
//            }
//    }

//    fun allInvitationDataChangeListener(){
//        Timber.d("enter allInvitationDataChange")
//        getFirebaseCollection(Constant.INVITATION).addSnapshotListener{ snapshot, e ->
//            if (e != null){
//                Timber.d("測試 e:$e ")
//                return@addSnapshotListener
//            }
//            if (snapshot != null && !snapshot.isEmpty){
//                val list = mutableListOf<Invitation>()
//                for (i in snapshot.documents){
//                    val model = i.toObject(Invitation::class.java)
//                    if (model?.user_id != CurrentUser.currentUser?.uid && model != null){
//                        list.add(model)
//                    }
//                }
//                Timber.d("測試 list.size = ${list.size}")
//                _allInvitationList.postValue(list)
//            }else{
//                Timber.d("測試 current is null ")
//            }
//        }
//    }

    // TODO: 2021/10/29 基本上最開始的時候先判斷有沒有值，如果沒有值就先 return，有的話才繼續判斷 
    // TODO: 2021/10/29 把它額外包一個funtion，然後回傳list，先回傳都是list的資料
    // TODO: 2021/11/2 做 filter
//    fun searchInvitation(currentUserId: String,areaList: MutableList<String>,petTypeList: MutableList<String>,result_sort: String,fragment: SearchFragment){
//
//        Timber.d("areaList:$areaList petTypeList:$petTypeList result_sort:$result_sort")
//        if (result_sort == Constant.RESULT_SORT_UPDATE_DAY){
//
//            Timber.d("enter 邀約更新")
//
//            when{
//                areaList.isNotEmpty() && petTypeList.isEmpty()  ->{
//                    Timber.d("enter 地區搜尋")
//                    Firebase.firestore.collection(Constant.INVITATION)
//                        .whereIn(Constant.AREA,areaList)
//                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (i in it.documents){
//                                val model = i.toObject(Invitation::class.java)
//                                if (model?.user_id != currentUserId){
//                                    list.add(model!!)
//                                }
//                            }
//                            Timber.d("listSize ${list.size}")
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//                        }
//                        .addOnFailureListener {
//                            Timber.d("搜尋fail $it")
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//                areaList.isEmpty() && petTypeList.isNotEmpty() -> {
//                    Firebase.firestore.collection(Constant.INVITATION)
//                        .whereIn(Constant.PET_TYPE,petTypeList)
//                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (i in it.documents){
//                                val model = i.toObject(Invitation::class.java)
//                                if (model?.user_id != currentUserId){
//                                    list.add(model!!)
//                                }
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//                        }
//                        .addOnFailureListener {
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//
//                areaList.isNotEmpty() && petTypeList.isNotEmpty() ->{
//                    val ref = Firebase.firestore.collection(Constant.INVITATION)
//                    ref.whereIn(Constant.AREA,areaList)
//                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (eachModel in it.documents){
//                                val model = eachModel.toObject(Invitation::class.java)
//                                for (type in petTypeList){
//                                    model?.let {
//                                        if (model.pet_type == type && model.user_id != currentUserId){
//                                            list.add(model)
//                                        }
//                                    }
//                                }
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//
//                        }
//                        .addOnFailureListener {
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//                else -> {
//                    Firebase.firestore.collection(Constant.INVITATION)
//                        .orderBy(Constant.UPDATE_TIME,Query.Direction.DESCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (i in it.documents){
//                                val model = i.toObject(Invitation::class.java)
//                                model?.let {
//                                    if (model.user_id != currentUserId){
//                                        list.add(it)
//                                    }
//                                }
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//                        }
//                        .addOnFailureListener {
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//
//
//            }
//        }else{
//            Timber.d("enter 邀約日期")
//            when{
//                areaList.isNotEmpty() && petTypeList.isEmpty()  ->{
//                    Timber.d("enter 邀約 地區")
//                    Firebase.firestore.collection(Constant.INVITATION)
//                        .whereIn(Constant.AREA,areaList)
//                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (i in it.documents){
//                                val model = i.toObject(Invitation::class.java)
//                                model?.let {
//                                    if (model.user_id != currentUserId){
//                                        list.add(it)
//                                    }
//                                }
//                                Timber.d("model: $model")
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//                        }
//                        .addOnFailureListener {
//                            Timber.d("搜尋fail $it")
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//                areaList.isEmpty() && petTypeList.isNotEmpty() -> {
//                    Firebase.firestore.collection(Constant.INVITATION)
//                        .whereIn(Constant.PET_TYPE,petTypeList)
//                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (i in it.documents){
//                                val model = i.toObject(Invitation::class.java)
//                                model?.let {
//                                    if (model.user_id != currentUserId){
//                                        list.add(it)
//                                    }
//                                }
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//                        }
//                        .addOnFailureListener {
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//
//                areaList.isNotEmpty() && petTypeList.isNotEmpty() ->{
//                    val ref = Firebase.firestore.collection(Constant.INVITATION)
//                    ref.whereIn(Constant.AREA,areaList)
//                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (eachModel in it.documents){
//                                val model = eachModel.toObject(Invitation::class.java)
//                                for (type in petTypeList){
//                                    model?.let {
//                                        if (model.pet_type == type && model.user_id != currentUserId){
//                                            list.add(model)
//                                        }
//                                    }
//                                }
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//
//                        }
//                        .addOnFailureListener {
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//                else -> {
//                    Firebase.firestore.collection(Constant.INVITATION)
//                        .orderBy(Constant.DATE_TIME,Query.Direction.ASCENDING)
//                        .get()
//                        .addOnSuccessListener {
//                            val list = mutableListOf<Invitation>()
//                            for (i in it.documents){
//                                val model = i.toObject(Invitation::class.java)
//                                model?.let {
//                                    if (model.user_id != currentUserId){
//                                        list.add(it)
//                                    }
//                                }
//                            }
//                            _dashboardInvitationList.postValue(list)
//                            fragment.searchInvitationSuccess(list.size)
//                        }
//                        .addOnFailureListener {
//                            fragment.searchInvitationFail(it.toString())
//                        }
//                }
//
//
//            }
//
//        }
//
//    }

    fun searchByRequirement(
        currentUserId: String,
        areaList: MutableList<String>,
        petTypeList: MutableList<String>,
        result_sort: String
    ) {

        Timber.d("Search測試進到 searchByRequirement")
        if (areaList.isEmpty() && petTypeList.isEmpty()) {
            getRefByResultSort(result_sort)
                .get()
                .addOnSuccessListener {
                    val list: MutableList<Invitation> = mutableListOf()
                    for (i in it.documents){
                        val model = i.toObject(Invitation::class.java)
                        if (model?.user_id != CurrentUser.currentUser?.uid){
                            if (model != null) {
                                list.add(model)
                            }
                        }
                    }
                    _dashboardInvitationList.postValue(list)
                    _backToDashboard.value = true
                }
            Timber.d("Search測試進到 area & pet 都是 empty")

            return
        }

        when {
            petTypeList.isEmpty() -> {

                Timber.d("Search測試進到 pet.isEmpty")
                getRefByResultSort(result_sort)
                    .whereIn(Constant.AREA, areaList)
                    .get()
                    .addOnSuccessListener {
                        val list = mutableListOf<Invitation>()
                        for (i in it.documents) {
                            val model = i.toObject(Invitation::class.java)
                            if (model?.user_id != currentUserId && model != null) {
                                list.add(model)
                            }
                        }
                        Timber.d("Search測試進到 list.size ${list.size}")
                        _dashboardInvitationList.postValue(list)
                        _backToDashboard.value = true

                    }
                    .addOnFailureListener { Timber.d("Search測試進到fail $it") }
            }
            areaList.isEmpty() -> {

                Timber.d("Search測試進到 area.isEmpty")

                getRefByResultSort(result_sort)
                    .whereIn(Constant.PET_TYPE, petTypeList)
                    .get()
                    .addOnSuccessListener {
                        val list = mutableListOf<Invitation>()
                        for (i in it.documents) {
                            val model = i.toObject(Invitation::class.java)
                            if (model?.user_id != currentUserId) {
                                if (model != null) {
                                    list.add(model)
                                }
                            }
                        }
                        Timber.d("Search測試進到 list.size ${list.size}")
                        _dashboardInvitationList.postValue(list)
                        _backToDashboard.value = true

                    }
                    .addOnFailureListener { Timber.d("Search測試進到fail $it")}
            }
            else -> {
                Timber.d("Search測試進到 both not empty")

                getRefByResultSort(result_sort)
                    .whereIn(Constant.AREA, areaList)
                    .get()
                    .addOnSuccessListener {
                        val list = mutableListOf<Invitation>()
                        for (i in it.documents) {
                            val model = i.toObject(Invitation::class.java)
                            for (type in petTypeList) {
                                if (type == model?.pet_type && model.user_id != currentUserId) {
                                    list.add(model)
                                }
                            }
                        }
                        Timber.d("Search測試進到 list.size ${list.size}")
                        _dashboardInvitationList.postValue(list)
                        _backToDashboard.value = true

                    }
                    .addOnFailureListener { Timber.d("Search測試進到fail $it")}

            }

        }
    }

    fun getRefByResultSort(result_sort: String): Query {
        Timber.d("Search測試進到進到 getRefByResult result_sort: $result_sort")

        return if (result_sort == Constant.RESULT_SORT_UPDATE_DAY) {
            Timber.d("Search測試進到進到 getRefByResult Result_sort update: $result_sort")
            Firebase.firestore.collection(Constant.INVITATION)
                .orderBy(Constant.UPDATE_TIME, Query.Direction.DESCENDING)
        } else {
            Timber.d("Search測試進到進到 getRefByResult Result_sort invitation day: $result_sort")
            Firebase.firestore.collection(Constant.INVITATION)
                .orderBy(Constant.DATE_TIME, Query.Direction.ASCENDING)
        }
    }

    fun resetAddInvitationState(){
        _invitation_add_state.postValue(null)
    }

    fun resetSaveImageState(){
        _saveImage_fail.postValue(null)
    }

    fun resetBackToDashboard(){
        _backToDashboard.postValue(false)
    }

    private fun firebaseDataChangeListener(){
        Firebase.firestore.collection(Constant.INVITATION).addSnapshotListener{ snapshot, e ->
            Timber.d("測試 list 進入funtion")
            if (e != null){
                Timber.d("測試 e:$e ")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty){
                val list = mutableListOf<Invitation>()
                for (i in snapshot.documents){
                    val model = i.toObject(Invitation::class.java)
                    if (model?.user_id != FirebaseAuth.getInstance().currentUser?.uid && model != null){
                        list.add(model)
                    }
                }
                _allInvitationList.postValue(list)
            }else{
                Timber.d("測試 current is null ")
            }
        }
    }
}
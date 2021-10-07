package com.example.petsmatchingapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentSearchBinding
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_search.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber


class SearchFragment : BaseFragment() {


    private lateinit var binding: FragmentSearchBinding
    private lateinit var areaCheckedList: MutableList<String>
    private lateinit var petTypeCheckedList: MutableList<String>
    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private val accountViewModel: AccountViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        binding = FragmentSearchBinding.inflate(inflater)



        binding.toolbarSearchFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarSearchFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        dismissActivityActionBarAndBottomNavigationView()
        binding.btnSearchSubmit.setOnClickListener {

                    getAreaCheckedList()
                    getPetTypeCheckedList()
                if (validDataForm()){
                    var result_sort: String = ""
                    if (rb_result_sort_invitation_time.isChecked){
                        result_sort = Constant.RESULT_SORT_INVITATION_DAY
                    }else{
                        result_sort = Constant.RESULT_SORT_UPDATE_DAY
                    }
                    matchingViewModel.searchInvitation(accountViewModel.getCurrentUID()!!,areaCheckedList,petTypeCheckedList,result_sort,this)
                }

           }


        return binding.root
    }


    private fun dismissActivityActionBarAndBottomNavigationView(){
        val activityInstance = this.activity as MatchingActivity
        activityInstance.supportActionBar?.hide()
        activityInstance.findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

    }
    private fun getAreaCheckedList(){
        val list = listOf<CheckBox>(binding.cbCityKeelung,binding.cbCityTaipei,binding.cbCityNewTaipei,binding.cbCityTaoyuan,binding.cbCityXinzhu,binding.cbCityXinzhuCounty
        ,binding.cbCityMiaoli,binding.cbCityMiaoliCounty,binding.cbCityTaichung,binding.cbCityZhanghua,binding.cbCityZhanghuaCounty,binding.cbCityNantou,binding.cbCityNantouCounty
        ,binding.cbCityYunlinCounty,binding.cbCityJiayi,binding.cbCityJaiyiCounty,binding.cbCityTainan,binding.cbCityGaoxiong,binding.cbCityPingtung,binding.cbCityPingtungCounty
        ,binding.cbCityYilan,binding.cbCityYilanCounty,binding.cbCityHualien,binding.cbCityHualienCounty,binding.cbCityTaidong,binding.cbCityTaidongCounty,binding.cbCityPenghuCounty
        ,binding.cbCityLudao,binding.cbCityLanyu,binding.cbCityJinmen,binding.cbCityMatus,binding.cbCityLianjiang)
        areaCheckedList = mutableListOf<String>()
        for (i in list){
            if (i.isChecked){
                areaCheckedList.add(i.text.toString())
            }
        }
    }

    private fun getPetTypeCheckedList(){
        val list = listOf<CheckBox>(binding.cbPetTypeDog,binding.cbPetTypeCat,binding.cbPetTypeRabbit,binding.cbPetTypeBird,binding.cbPetTypePig,binding.cbPetTypeFish,binding.cbPetTypeOther)
        petTypeCheckedList = mutableListOf<String>()
        for (i in list){
            if (i.isChecked){
                petTypeCheckedList.add(i.text.toString())
            }
        }
    }

    fun searchInvitationSuccess(resultSize: Int){
            Timber.d("resultSize = $resultSize")
        if (resultSize != 0 && findNavController().currentDestination?.id == R.id.searchFragment){
            findNavController().navigate(R.id.action_searchFragment_to_navigation_dashboard)
        }else{
            showSnackBar("沒有找到符合的資料",false)
        }
    }

    fun searchInvitationFail(e: String){
        showSnackBar(e,true)
    }

    private fun validDataForm(): Boolean{
        return when{
            !binding.rbResultSortInvitationTime.isChecked && !binding.rbResultSortUpdateTime.isChecked -> {
                showSnackBar(resources.getString(R.string.msg_choose_your_result_sort),true)
                false
            }
            areaCheckedList.size >10 -> {
                showSnackBar("最多只能選擇10個地區",true)
                false
            }
            petTypeCheckedList.size > 10 -> {
                showSnackBar("最多只能選擇10種寵物",true)
                false
            }

            else -> true
        }
    }


}
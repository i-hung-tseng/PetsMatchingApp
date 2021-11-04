package com.example.petsmatchingapp.ui.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentAddInvitationBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.adapter.MultiplePhotoAdapter
import com.example.petsmatchingapp.utils.CheckInternetState
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.firebase.Timestamp
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*




class AddInvitationFragment : BaseFragment(),View.OnClickListener {


    private lateinit var binding: FragmentAddInvitationBinding
    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private val accountViewModel: AccountViewModel by sharedViewModel()
    private lateinit var petList: List<String>
    private lateinit var areaList: List<String>
    private var selectedPetType: String? = null
    private var selectedArea: String? = null
    private lateinit var datePicker: DatePickerDialog
    private var selectedDate: Timestamp? = null

    private var selectedUriList: List<Uri>? = null
    private var selectedUploadList: List<String> = listOf()
    private var selectedUriTypeList: List<String> = listOf()


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ uri ->
        if (uri.resultCode == Activity.RESULT_OK){
            val selectedUri = uri.data?.clipData
            if (selectedUri != null){

                val list = mutableListOf<Uri>()
                val account = selectedUri.itemCount

                for (i in 0 until account){
                    val model = selectedUri.getItemAt(i).uri
                    list.add(model)
                    Timber.d("enter selectedUri $model")
                }
                selectedUriList = list
                getTypeFromSelectedUri(list)
                setAdapterWitIndicator(list)


            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {





        binding = FragmentAddInvitationBinding.inflate(inflater)


        binding.toolbarAddInvitationFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarAddInvitationFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        matchingViewModel.invitation_add_state.observe( viewLifecycleOwner, {
            Timber.d("觀察 invitation_add_state it: $it")
            if (it == true){
                hideDialog()
                showSnackBar(resources.getString(R.string.add_invitation_successful),false)
                findNavController().navigate(R.id.action_addInvitationFragment_to_navigation_home)

            }else{
                hideDialog()
                showSnackBar(resources.getString(R.string.add_invitation_fail),true)
            }
//            matchingViewModel.resetAddInvitationState()
        })

        matchingViewModel.saveImage_fail.observe(viewLifecycleOwner, {
            showSnackBar(it,true)
            matchingViewModel.resetSaveImageState()
        })



        petList = listOf("狗", "貓", "兔子", "鳥","豬","魚","其它")
        areaList = listOf("基隆市","台北市","新北市","桃園市","新竹市","新竹縣","苗栗縣","彰化縣","雲林縣","南投縣","台中市","嘉義市","嘉義縣","台南市",
            "高雄市","屏東縣","宜蘭縣","花蓮縣","台東縣","澎湖縣","金門縣","連江縣","其它")

        binding.ivAddInvitationCamera.setOnClickListener(this)

        binding.spinnerPetType.setOnItemClickListener { _, _, position, _ ->

            selectedPetType = petList[position]
                Timber.d("selectedPet $selectedPetType")
        }

        binding.spinnerArea.setOnItemClickListener { _, _, position, _ ->

            selectedArea = areaList[position]
            Timber.d("selectedArea: $selectedArea")
        }

        setUpDatePickerDialog()

        binding.edAddInvitationDateTime.setOnClickListener(this)
        binding.tipAddInvitationFragmentDateTime.setOnClickListener(this)
        binding.btnAddInvitationFragmentSubmit.setOnClickListener(this)
        setSpinner()
        return binding.root
    }



    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            resultLauncher.launch(intent)

        }else{
            requestPermission()
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),Constant.REQUEST_CODE_READ)
    }

//
//    fun saveImageSuccessful(uriList: List<Uri>){
//        showSnackBar(resources.getString(R.string.update_pet_image_successful),false)
//
//
//        val list = mutableListOf<String>()
//        for (i in uriList){
//            list.add(i.toString())
//        }
//        Timber.d("saveImage uriList: ${uriList.size}")
//        selectedUploadList = list
//
//    }

//    fun saveImageFail(e:String){
//        showSnackBar(e,true)
//    }

    override fun onClick(v: View?) {
        when(v){
            binding.ivAddInvitationCamera -> {
                checkPermission()
            }
            binding.edAddInvitationDateTime -> {
                datePicker.show()

            }
            binding.btnAddInvitationFragmentSubmit ->{


                if(!CheckInternetState(requireContext()).isInternetAvailable()){
                    showSnackBar("請先確認網路情況",true)
                    return
                }

               if (validDataFormAndSaveImage()){
                   showDialog(resources.getString(R.string.please_wait))

                   val invitation = Invitation(
                       user_id = accountViewModel.userDetail.value!!.id,
                       user_name = accountViewModel.userDetail.value?.name,
                       user_image = accountViewModel.userDetail.value?.image,
                       pet_type = selectedPetType!!,
                       pet_type_description = binding.edAddInvitationPetTypeDescription.text.toString().trim(),
                       area = selectedArea!!,
                       date_place = binding.edAddInvitationDatePlace.text.toString().trim(),
                       date_time = selectedDate!!,
                       note = binding.edAddInvitationNote.text.toString().trim(),
                       update_time = Timestamp(Date(System.currentTimeMillis())),
                   )
                   selectedUriList?.let {
                       matchingViewModel.saveImageToFireStorage(selectedUriTypeList,
                           it,invitation)
                   }
               }
            }
        }
    }

    private fun setSpinner(){

        val petAdapter = ArrayAdapter(requireContext(),R.layout.spinner_list_item,petList)
        binding.spinnerPetType.setAdapter(petAdapter)

        val areaAdapter = ArrayAdapter(requireContext(),R.layout.spinner_list_item,areaList)
        binding.spinnerArea.setAdapter(areaAdapter)

    }

    private fun setUpDatePickerDialog(){
        val calendar = Calendar.getInstance()
        datePicker = DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                val mFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
                calendar.set(year,month,dayOfMonth)
                binding.edAddInvitationDateTime.setText(sdf.format(calendar.time))
                val calLongType = calendar.timeInMillis
                val timeStamp = Timestamp(Date(calLongType))
                selectedDate = timeStamp


            },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))


        datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis


    }


    private fun validDataFormAndSaveImage(): Boolean{




        return when{
            selectedUriList.isNullOrEmpty() ->{
                Timber.d("test enter selectedUpload: $selectedUploadList")
                showSnackBar(resources.getString(R.string.hint_select_your_image),true)
                false
            }
            selectedPetType.isNullOrBlank() -> {
                showSnackBar(resources.getString(R.string.hint_enter_pet_type),true)
                false
            }
            TextUtils.isEmpty(binding.edAddInvitationPetTypeDescription.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_pet_type_description),true)
                false
            }
            selectedArea.isNullOrBlank() -> {
                showSnackBar(resources.getString(R.string.hint_enter_your_area),true)
                false
            }
            TextUtils.isEmpty(binding.edAddInvitationDatePlace.text.toString().trim()) -> {
                showSnackBar(resources.getString(R.string.hint_enter_date_place),true)
                false
            }


            TextUtils.isEmpty(binding.edAddInvitationNote.text.toString().trim()) ->{
                showSnackBar(resources.getString(R.string.hint_enter_date_note),true)
                false
            }
            else -> true
        }
    }

    private  fun setAdapterWitIndicator(list: List<Uri>){

        binding.bannerImage.adapter = MultiplePhotoAdapter(list)
        binding.indicatorAddInvitation.apply {
            setSliderColor(Color.GRAY,ContextCompat.getColor(requireContext(),R.color.pewter_blue))
            setSliderWidth(resources.getDimension(R.dimen.indicator_width))
            setSlideMode(IndicatorSlideMode.SCALE)
            setIndicatorStyle(IndicatorStyle.CIRCLE)
                .setupWithViewPager(binding.bannerImage)

        }

    }
    private fun getTypeFromSelectedUri(list: List<Uri>){

        var typeList: MutableList<String> = mutableListOf()
        for (i in 0 until list.size){
            val model = Constant.getFileExtension(requireActivity(),list[i])
            if (model != null) {
                typeList.add(model)
            }
        }
        selectedUriTypeList = typeList

    }


}
package com.example.petsmatchingapp.ui.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.petsmatchingapp.R
import com.example.petsmatchingapp.databinding.FragmentAddInvitationBinding
import com.example.petsmatchingapp.model.Invitation
import com.example.petsmatchingapp.ui.activity.MatchingActivity
import com.example.petsmatchingapp.utils.Constant
import com.example.petsmatchingapp.viewmodel.AccountViewModel
import com.example.petsmatchingapp.viewmodel.MatchingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.w3c.dom.Text
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AddInvitationFragment : BaseFragment(),View.OnClickListener {


    private lateinit var binding: FragmentAddInvitationBinding
    private val matchingViewModel: MatchingViewModel by sharedViewModel()
    private val accountViewModel: AccountViewModel by sharedViewModel()
    private var mUri: String? = null
    private lateinit var petList: List<String>
    private lateinit var areaList: List<String>
    private var selectedPetType: String? = null
    private var selectedArea: String? = null
    private lateinit var datePicker: DatePickerDialog
    private var selectedDate: String? = null


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ uri ->
        if (uri.resultCode == Activity.RESULT_OK){
            val selectedUri = uri.data?.data
            if (selectedUri != null){

                Constant.loadPetImage(selectedUri,binding.ivAddInvitationPetImage)
                matchingViewModel.saveImageToFireStorage(requireActivity(),this,selectedUri)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        dismissActivityActionBarAndBottomNavigationView()



        binding = FragmentAddInvitationBinding.inflate(inflater)

        binding.toolbarAddInvitationFragment.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.toolbarAddInvitationFragment.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }







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

    private fun dismissActivityActionBarAndBottomNavigationView(){
        val activityInstance = this.activity as MatchingActivity
        activityInstance.supportActionBar?.hide()
        activityInstance.findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            resultLauncher.launch(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            )
        }else{
            requestPermission()
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),Constant.REQUEST_CODE_READ)
    }


    fun saveImageSuccessful(uri: Uri){
        showSnackBar(resources.getString(R.string.update_pet_image_successful),false)
        mUri = uri.toString()
    }

    fun saveImageFail(e:String){
        showSnackBar(e,true)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.ivAddInvitationCamera -> {
                checkPermission()
                Timber.d("時間 $selectedDate")
            }
            binding.edAddInvitationDateTime -> {
                datePicker.show()

            }
            binding.btnAddInvitationFragmentSubmit ->{
                showDialog(resources.getString(R.string.please_wait))
               if (validDataForm()){
                   val invitation = Invitation(
                       user_id = accountViewModel.userDetail.value!!.id,
                       pet_image = mUri!!,
                       pet_type = selectedPetType!!,
                       pet_type_description = binding.edAddInvitationPetTypeDescription.text.toString().trim(),
                       area = selectedArea!!,
                       date_place = binding.edAddInvitationDatePlace.text.toString().trim(),
                       date_time = selectedDate!!,
                       note = binding.edAddInvitationNote.text.toString().trim()
                   )
                   matchingViewModel.addInvitationToFireStore(this,invitation)
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
                Timber.d("listener year = $year")
                Timber.d("listener month = $month")
                Timber.d("listener dayOfMonth = $dayOfMonth")
                val mFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(mFormat, Locale.getDefault())
                calendar.set(year,month,dayOfMonth)
                selectedDate = sdf.format(calendar.time)
                binding.edAddInvitationDateTime.setText(sdf.format(calendar.time))
            },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis


    }

    fun addInvitationSuccess(){
       hideDialog()
       showSnackBar(resources.getString(R.string.add_invitation_successful),false)
    }

    fun addInvitationFail(e: String){
        hideDialog()
        showSnackBar(e,true)
    }

    private fun validDataForm(): Boolean{
        return when{
            mUri.isNullOrBlank() ->{
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

            selectedDate.isNullOrBlank() ->{
                showSnackBar(resources.getString(R.string.hint_enter_date_time),true)
                false
            }

            TextUtils.isEmpty(binding.edAddInvitationNote.text.toString().trim()) ->{
                showSnackBar(resources.getString(R.string.hint_enter_date_note),true)
                false
            }
            else -> true
        }
    }





}
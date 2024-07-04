package com.example.chamasegura.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.chamasegura.MapsActivity
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.Burn
import com.example.chamasegura.data.entities.BurnState
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.vm.BurnViewModel
import com.example.chamasegura.utils.AuthManager
import com.example.chamasegura.utils.JwtUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class fragment_new_burn : Fragment() {

    private lateinit var burnViewModel: BurnViewModel
    private var latitude: Float = 0f
    private var longitude: Float = 0f
    private val MAP_REQUEST_CODE = 1001
    private var formattedDate: String? = null
    private lateinit var locationInfoText: TextView
    private var distrito: String = ""
    private var concelho: String = ""
    private var freguesia: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_burn, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        burnViewModel = ViewModelProvider(this).get(BurnViewModel::class.java)

        val buttonBack = view.findViewById<ImageButton>(R.id.buttonBack)
        buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val editTextDate = view.findViewById<EditText>(R.id.editTextDate)
        editTextDate.setOnClickListener {
            showDatePickerDialog(editTextDate)
        }

        val spinnerType = view.findViewById<Spinner>(R.id.spinnerType)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.burn_types_new_burn,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerType.adapter = adapter
        }

        val editTextReason = view.findViewById<EditText>(R.id.editTextReason)
        val editTextOtherData = view.findViewById<EditText>(R.id.editTextOtherData)
        locationInfoText = view.findViewById(R.id.locationInfoText)
        locationInfoText.setTextColor(resources.getColor(android.R.color.black))

        val buttonOpenMap = view.findViewById<Button>(R.id.buttonOpenMap)
        buttonOpenMap.setOnClickListener {
            openMap()
        }

        val buttonConfirm = view.findViewById<Button>(R.id.buttonConfirm)
        buttonConfirm.setOnClickListener {
            if (validateInputs(editTextDate, spinnerType, editTextReason)) {
                createBurn(spinnerType, editTextReason, editTextOtherData)
            }
        }

        burnViewModel.createBurnResult.observe(viewLifecycleOwner) { result ->
            val (success, message) = result
            if (success) {
                Toast.makeText(requireContext(), getString(R.string.burn_created_success), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), getString(R.string.burn_creation_error, message), Toast.LENGTH_SHORT).show()
            }
        }

        burnViewModel.locationInfo.observe(viewLifecycleOwner) { location ->
            location?.let {
                distrito = it.distrito
                concelho = it.concelho
                freguesia = it.freguesia
                val info = "${getString(R.string.distrito)}: $distrito\n${getString(R.string.concelho)}: $concelho\n${getString(R.string.freguesia)}: $freguesia\n"
                locationInfoText.text = info
                locationInfoText.visibility = View.VISIBLE
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formattedDate = dateFormat.format(selectedDate.time)
                editText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun validateInputs(
        editTextDate: EditText,
        spinnerType: Spinner,
        editTextReason: EditText,
    ): Boolean {
        if (editTextDate.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.date_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (spinnerType.selectedItem == null) {
            Toast.makeText(requireContext(), getString(R.string.type_required), Toast.LENGTH_SHORT).show()
            return false
        }
        if (editTextReason.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.reason_required), Toast.LENGTH_SHORT).show()
            return false
        }

        if (latitude == 0f || longitude == 0f) {
            Toast.makeText(requireContext(), getString(R.string.location_required), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun createBurn(
        spinnerType: Spinner,
        editTextReason: EditText,
        editTextOtherData: EditText
    ) {
        val dateString = formattedDate!!
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(dateString)

        val type = when (spinnerType.selectedItem.toString()) {
            getString(R.string.regular_clean) -> BurnType.REGCLEAN
            getString(R.string.particular) -> BurnType.PARTICULAR
            else -> BurnType.REGCLEAN
        }

        val reason = editTextReason.text.toString()
        val otherData = editTextOtherData.text.toString()

        val authManager = AuthManager(requireContext())
        val token = authManager.getToken()
        val userId = token?.let { JwtUtils.getUserIdFromToken(it) }

        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        if (userId != null && date != null) {
            val burn = Burn(
                id = 0,
                date = isoDateFormat.format(date),
                reason = reason,
                latitude = latitude,
                longitude = longitude,
                otherData = otherData,
                createdAt = "",
                updatedAt = "",
                userId = userId,
                type = type,
                distrito = distrito,
                concelho = concelho,
                freguesia = freguesia,
                state = BurnState.PENDING
            )
            burnViewModel.createBurn(burn)
        } else {
            Toast.makeText(requireContext(), getString(R.string.error_creating_burn), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMap() {
        val intent = Intent(requireContext(), MapsActivity::class.java)
        startActivityForResult(intent, MAP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            latitude = data.getFloatExtra("latitude", 0f)
            longitude = data.getFloatExtra("longitude", 0f)
            burnViewModel.fetchLocationInfo(latitude.toDouble(), longitude.toDouble())
        }
    }
}
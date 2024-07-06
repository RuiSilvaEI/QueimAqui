package com.example.chamasegura.fragments

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.User
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.vm.MunicipalityViewModel
import com.example.chamasegura.data.vm.UserViewModel
import com.example.chamasegura.utils.JwtUtils
import com.toptoche.searchablespinnerlibrary.SearchableSpinner

class fragment_register_manage_users : Fragment() {
    private val userViewModel: UserViewModel by viewModels()
    private val municipalityViewModel: MunicipalityViewModel by viewModels()
    private var passwordVisible = false
    private var confirmPasswordVisible = false

    // Lista de municípios disponíveis para seleção
    private val municipalityNames = listOf("Braga", "Viana", "Porto", "Lisboa", "Faro", "Coimbra")

    // Lista de tipos de usuário
    private val userTypes = listOf("CM", "Regular")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_manage_users, container, false)

        val fullNameEditText: EditText = view.findViewById(R.id.full_name)
        val emailEditText: EditText = view.findViewById(R.id.email)
        val passwordEditText: EditText = view.findViewById(R.id.password)
        val nifEditText: EditText = view.findViewById(R.id.nif)
        val userTypeSpinner: Spinner = view.findViewById(R.id.user_type_spinner)
        val municipalitySpinner: SearchableSpinner = view.findViewById(R.id.municipality_spinner)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)


        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Configuração do Spinner de Municípios
        val adapterMunicipality = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, municipalityNames)
        adapterMunicipality.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        municipalitySpinner.adapter = adapterMunicipality

        // Setting up the user type spinner
        val adapterUserType = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userTypes)
        adapterUserType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapterUserType

        userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUserType = userTypeSpinner.selectedItem.toString()
                if (selectedUserType == "Regular") {
                    municipalitySpinner.isEnabled = false
                    municipalitySpinner.setSelection(0)
                } else {
                    municipalitySpinner.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Limitar a entrada do NIF para apenas dígitos e no máximo 9 caracteres
        nifEditText.filters = arrayOf(InputFilter.LengthFilter(9))

        confirmButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val nifString = nifEditText.text.toString().trim()
            val userType = when (userTypeSpinner.selectedItem.toString()) {
                "Regular" -> UserType.REGULAR
                "CM" -> UserType.CM
                else -> UserType.REGULAR
            }
            val selectedMunicipality = if (municipalitySpinner.isEnabled) municipalitySpinner.selectedItem.toString() else ""

            when {
                fullName.isEmpty() -> {
                    Log.e("RegisterError", "Full name is empty")
                    Toast.makeText(requireContext(), getString(R.string.empty_full_name), Toast.LENGTH_LONG).show()
                }
                email.isEmpty() -> {
                    Log.e("RegisterError", "Email is empty")
                    Toast.makeText(requireContext(), getString(R.string.empty_email), Toast.LENGTH_LONG).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Log.e("RegisterError", "Email is invalid")
                    Toast.makeText(requireContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show()
                }
                password.isEmpty() -> {
                    Log.e("RegisterError", "Password is empty")
                    Toast.makeText(requireContext(), getString(R.string.empty_password), Toast.LENGTH_LONG).show()
                }
                password.length < 6 -> {
                    Log.e("RegisterError", "Password is too short")
                    Toast.makeText(requireContext(), getString(R.string.password_too_short), Toast.LENGTH_LONG).show()
                }
                nifString.length != 9 || nifString.any { !it.isDigit() } -> {
                    Log.e("RegisterError", "NIF is invalid")
                    Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                }
                userType == UserType.CM && selectedMunicipality.isEmpty() -> {
                    Log.e("RegisterError", "Municipality is empty for CM user type")
                    Toast.makeText(requireContext(), getString(R.string.empty_municipality), Toast.LENGTH_LONG).show()
                }
                else -> {
                    val nif = nifString.toIntOrNull()
                    if (nif != null) {
                        val newUser = User(
                            id = 0,
                            name = fullName,
                            email = email,
                            password = password,
                            photo = null,
                            nif = nif,
                            type = userType,
                            createdAt = "",
                            updatedAt = "",
                            state = StateUser.ENABLED
                        )
                        userViewModel.signUp(newUser) { response ->
                            val token = response?.token
                            if (token != null) {
                                val userId = JwtUtils.getUserIdFromToken(token)
                                if (userId != null) {
                                    // Log.i("RegisterSuccess", "User created successfully with ID: $userId")
                                    Toast.makeText(requireContext(), getString(R.string.user_created_successfully), Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.fragment_manage_users)
                                } else {
                                   // Log.e("RegisterError", "User ID extraction from token failed")
                                    Toast.makeText(requireContext(), getString(R.string.sign_up_failed), Toast.LENGTH_SHORT).show()
                                }
                            } else {
                               // Log.e("RegisterError", "Sign up failed - token is null")
                                Toast.makeText(requireContext(), getString(R.string.sign_up_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        //Log.e("RegisterError", "NIF conversion to integer failed")
                        Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        return view
    }
}

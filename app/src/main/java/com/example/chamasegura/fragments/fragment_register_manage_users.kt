package com.example.chamasegura.fragments

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
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
        val confirmPasswordEditText: EditText = view.findViewById(R.id.confirm_password)
        val nifEditText: EditText = view.findViewById(R.id.nif)
        val userTypeSpinner: Spinner = view.findViewById(R.id.user_type_spinner)
        val municipalitySpinner: SearchableSpinner = view.findViewById(R.id.municipality_spinner)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val togglePasswordVisibilityButton = view.findViewById<ImageButton>(R.id.toggle_password_visibility)
        val toggleConfirmPasswordVisibilityButton = view.findViewById<ImageButton>(R.id.toggle_confirm_password_visibility)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        togglePasswordVisibilityButton.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordVisibilityButton.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordVisibilityButton.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        toggleConfirmPasswordVisibilityButton.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            if (confirmPasswordVisible) {
                confirmPasswordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleConfirmPasswordVisibilityButton.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleConfirmPasswordVisibilityButton.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        // Configuração do Spinner de Municípios
        municipalityViewModel.getMunicipalities()
        municipalityViewModel.municipalities.observe(viewLifecycleOwner) { municipalities ->
            val municipalityNames = municipalities.map { it.name }
            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, municipalityNames).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            }
            municipalitySpinner.adapter = adapter
            municipalitySpinner.setTitle(getString(R.string.select_municipality))
            municipalitySpinner.setPositiveButton("OK")

            // Initial setup based on user type
            val selectedUserType = userTypeSpinner.selectedItem.toString()
            if (selectedUserType == "REGULAR") {
                municipalitySpinner.isEnabled = false
                municipalitySpinner.setSelection(0) // Optionally clear the selection

                // Change spinner appearance to disabled
                val selectMunicipalityString = getString(R.string.select_municipality)
                val disabledAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_disabled, arrayOf(selectMunicipalityString)).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                municipalitySpinner.adapter = disabledAdapter
            }
        }

        // Setting up the user type spinner to disable municipality spinner if type is REGULAR
        val userTypeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, userTypes).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        userTypeSpinner.adapter = userTypeAdapter

        userTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedUserType = userTypeSpinner.selectedItem.toString()
                val selectMunicipalityString = getString(R.string.select_municipality)
                if (selectedUserType == "Regular") {
                    municipalitySpinner.isEnabled = false
                    municipalitySpinner.setSelection(0) // Optionally clear the selection

                    // Change spinner appearance to disabled
                    val disabledAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_disabled, arrayOf(selectMunicipalityString)).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    }
                    municipalitySpinner.adapter = disabledAdapter
                } else {
                    municipalitySpinner.isEnabled = true

                    // Restore spinner appearance to enabled
                    municipalityViewModel.municipalities.value?.let { municipalities ->
                        val municipalityNames = municipalities.map { it.name }
                        val enabledAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, municipalityNames).also { adapter ->
                            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        }
                        municipalitySpinner.adapter = enabledAdapter
                    }
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
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val nifString = nifEditText.text.toString().trim()
            val userType = when (userTypeSpinner.selectedItem.toString()) {
                "Regular" -> UserType.REGULAR
                "CM" -> UserType.CM
                else -> UserType.REGULAR
            }
            val selectedMunicipality = if (municipalitySpinner.isEnabled) municipalitySpinner.selectedItem.toString() else ""

            when {
                fullName.isEmpty() -> {
                    Toast.makeText(requireContext(), getString(R.string.empty_full_name), Toast.LENGTH_LONG).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(requireContext(), getString(R.string.empty_email), Toast.LENGTH_LONG).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(requireContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(requireContext(), getString(R.string.empty_password), Toast.LENGTH_LONG).show()
                }
                password.length < 6 -> {
                    Toast.makeText(requireContext(), getString(R.string.password_too_short), Toast.LENGTH_LONG).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(requireContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show()
                }
                nifString.length != 9 || nifString.any { !it.isDigit() } -> {
                    Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                }
                userType == UserType.CM && selectedMunicipality.isEmpty() -> {
                    Toast.makeText(requireContext(), getString(R.string.empty_municipality), Toast.LENGTH_LONG).show()
                }
                else -> {
                    val nif = nifString.toIntOrNull()
                    if (nif != null) {
                        municipalityViewModel.municipalities.value?.let { municipalities ->
                            val municipality = municipalities.find { it.name == selectedMunicipality }
                            if (municipality != null || userType == UserType.REGULAR) {
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
                                            if (userType == UserType.CM && municipality != null) {
                                                municipalityViewModel.updateMunicipalityResponsible(municipality.id, userId)
                                            }
                                            Toast.makeText(requireContext(), getString(R.string.user_created_successfully), Toast.LENGTH_SHORT).show()
                                            findNavController().navigate(R.id.fragment_manage_users)
                                        } else {
                                            Toast.makeText(requireContext(), getString(R.string.sign_up_failed), Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.sign_up_failed), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.empty_municipality), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        return view
    }
}

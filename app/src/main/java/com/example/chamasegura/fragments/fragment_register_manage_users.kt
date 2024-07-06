package com.example.chamasegura.fragments

import android.os.Bundle
import android.text.InputFilter
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
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.vm.UserViewModel
import com.example.chamasegura.utils.JwtUtils

class fragment_register_manage_users : Fragment() {
    private val userViewModel: UserViewModel by viewModels()

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
        val municipalitySpinner: Spinner = view.findViewById(R.id.municipality_spinner)
        val userTypeSpinner: Spinner = view.findViewById(R.id.user_type_spinner)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Configuração do Spinner de Municípios
        val adapterMunicipality = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, municipalityNames)
        adapterMunicipality.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        municipalitySpinner.adapter = adapterMunicipality

        // Configuração do Spinner de Tipos de Usuário
        val adapterUserType = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userTypes)
        adapterUserType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapterUserType

        // Limitar a entrada do NIF para apenas dígitos e no máximo 9 caracteres
        nifEditText.filters = arrayOf(InputFilter.LengthFilter(9))

        confirmButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val nifString = nifEditText.text.toString().trim()
            val selectedMunicipality = municipalitySpinner.selectedItem.toString()
            val selectedUserType = userTypeSpinner.selectedItem.toString()

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
                nifString.length != 9 || nifString.any { !it.isDigit() } -> {
                    Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                }
                selectedMunicipality.isEmpty() -> {
                    Toast.makeText(requireContext(), getString(R.string.empty_municipality), Toast.LENGTH_LONG).show()
                }
                selectedUserType.isEmpty() -> {
                    Toast.makeText(requireContext(), getString(R.string.empty_user_type), Toast.LENGTH_LONG).show()
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
                            type = if (selectedUserType == "CM") UserType.CM else UserType.REGULAR,
                            createdAt = "",
                            updatedAt = "",
                            state = StateUser.ENABLED
                        )
                        userViewModel.signUp(newUser) { response ->
                            val token = response?.token
                            if (token != null) {
                                val userId = JwtUtils.getUserIdFromToken(token)
                                if (userId != null) {
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
                        Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        return view
    }
}

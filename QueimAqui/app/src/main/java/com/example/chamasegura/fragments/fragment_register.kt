package com.example.chamasegura.fragments

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.entities.User
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.vm.UserViewModel

class fragment_register : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Find views
        val fullNameEditText: EditText = view.findViewById(R.id.full_name)
        val emailEditText: EditText = view.findViewById(R.id.email)
        val passwordEditText: EditText = view.findViewById(R.id.password)
        val confirmPasswordEditText: EditText = view.findViewById(R.id.confirm_password)
        val nifEditText: EditText = view.findViewById(R.id.nif)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)



        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }


        // Limitar a entrada do NIF para apenas dígitos e no máximo 9 caracteres
        nifEditText.filters = arrayOf(InputFilter.LengthFilter(9))

        confirmButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            val nifString = nifEditText.text.toString().trim()

            when {
                fullName.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_full_name),
                        Toast.LENGTH_LONG
                    ).show()
                }

                email.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_email),
                        Toast.LENGTH_LONG
                    ).show()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.invalid_email),
                        Toast.LENGTH_LONG
                    ).show()
                }

                password.isEmpty() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.empty_password),
                        Toast.LENGTH_LONG
                    ).show()
                }

                password.length < 6 -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.password_too_short),
                        Toast.LENGTH_LONG
                    ).show()
                }

                password != confirmPassword -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.passwords_do_not_match),
                        Toast.LENGTH_LONG
                    ).show()
                }

                nifString.length != 9 || nifString.any { !it.isDigit() } -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.invalid_nif_message),
                        Toast.LENGTH_LONG
                    ).show()
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
                            type = UserType.REGULAR,
                            createdAt = "",
                            updatedAt = "",
                            state = StateUser.ENABLED
                        )
                        userViewModel.signUp(newUser) { loginResponse ->
                            val user = loginResponse?.name
                            if (user != null) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.user_created_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                                findNavController().navigate(R.id.action_fragment_register_to_fragment_first_screen2)
                            } else {
                                // Sign-up failed
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.sign_up_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.invalid_nif_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        return view
    }
}
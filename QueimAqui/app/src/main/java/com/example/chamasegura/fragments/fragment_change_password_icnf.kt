package com.example.chamasegura.fragments

import android.os.Bundle
import android.text.InputType
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
import androidx.navigation.fragment.navArgs
import com.example.chamasegura.R
import com.example.chamasegura.data.vm.UserViewModel
import com.example.chamasegura.utils.AuthManager

class fragment_change_password_icnf : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val args: fragment_change_password_icnfArgs by navArgs()
    private lateinit var authManager: AuthManager
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var confirmButton: Button
    private lateinit var showNewPasswordButton: ImageButton
    private lateinit var showConfirmPasswordButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password_icnf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newPasswordInput = view.findViewById(R.id.newPasswordInput)
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput)
        confirmButton = view.findViewById(R.id.confirmButton)
        showNewPasswordButton = view.findViewById(R.id.show_new_password_button)
        showConfirmPasswordButton = view.findViewById(R.id.show_confirm_password_button)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)

        authManager = AuthManager(requireContext())

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        showNewPasswordButton.setOnClickListener {
            togglePasswordVisibility(newPasswordInput, showNewPasswordButton)
        }

        showConfirmPasswordButton.setOnClickListener {
            togglePasswordVisibility(confirmPasswordInput, showConfirmPasswordButton)
        }

        confirmButton.setOnClickListener {
            val newPassword = newPasswordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.empty_password), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), getString(R.string.password_too_short), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val userId = args.userId

            userViewModel.changePasswordIcnf(userId, newPassword) { success, errorMessage ->
                if (success) {
                    Toast.makeText(requireContext(), getString(R.string.password_change_success), Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else {
                    val message = when {
                        errorMessage?.contains("Invalid old password") == true -> getString(R.string.invalid_old_password)
                        errorMessage?.contains("User not found") == true -> getString(R.string.user_not_found)
                        else -> getString(R.string.password_change_failure)
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, button: ImageButton) {
        if (editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.baseline_visibility_24)
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.baseline_visibility_off_24)
        }
        editText.setSelection(editText.text.length)
    }
}
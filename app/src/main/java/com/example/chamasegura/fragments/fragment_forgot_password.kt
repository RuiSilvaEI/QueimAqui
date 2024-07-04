package com.example.chamasegura.fragments

import android.os.Bundle
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
import com.example.chamasegura.data.vm.UserViewModel

class fragment_forgot_password : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val emailInput = view.findViewById<EditText>(R.id.emailInput)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        confirmButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                userViewModel.sendPasswordResetCode(email) { success, errorMessage ->
                    if (success) {
                        Toast.makeText(requireContext(), getString(R.string.verification_code_sent), Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.action_fragment_forgot_password_to_fragment_verification_code)
                    } else {
                        val message = errorMessage ?: getString(R.string.failed_to_send_verification_code)
                        Toast.makeText(requireContext(), if (message == "Email not found") getString(R.string.email_not_found) else message, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.please_enter_email), Toast.LENGTH_LONG).show()
            }
        }
    }
}
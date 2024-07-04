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

class fragment_verification_code : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verification_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val codeInput = view.findViewById<EditText>(R.id.codeInput)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        confirmButton.setOnClickListener {
            val code = codeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                userViewModel.verifyPasswordResetCode(code) { success, errorMessage ->
                    if (success) {
                        val action = fragment_verification_codeDirections.actionFragmentVerificationCodeToFragmentChangePasswordUser(code)
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), errorMessage ?: getString(R.string.invalid_verification_code), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.please_enter_verification_code), Toast.LENGTH_LONG).show()
            }
        }
    }
}
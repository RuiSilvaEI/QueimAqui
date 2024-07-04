package com.example.chamasegura.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.chamasegura.R

class fragment_first_screen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_first_screen, container, false)

        // Set up the button listeners
        val loginButton: Button = view.findViewById(R.id.login_button)
        val registerButton: Button = view.findViewById(R.id.register_button)
        val forgotPassword: TextView = view.findViewById(R.id.forgot_password)
        val exitButton: ImageButton = view.findViewById(R.id.exit_button)

        loginButton.setOnClickListener {
            Log.d("fragment_first_screen", "Login button clicked")
            findNavController().navigate(R.id.action_fragment_first_screen_to_fragment_login)
        }

        registerButton.setOnClickListener {
            Log.d("fragment_first_screen", "Register button clicked")
            findNavController().navigate(R.id.action_fragment_first_screen_to_fragment_register)
        }

        forgotPassword.setOnClickListener {
            Log.d("fragment_first_screen", "Forgot password clicked")
            findNavController().navigate(R.id.action_fragment_first_screen_to_fragment_forgot_password)
        }

        exitButton.setOnClickListener {
            Log.d("fragment_first_screen", "Exit button clicked")
            activity?.finishAffinity()
        }

        return view
    }
}

package com.example.chamasegura.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.example.chamasegura.R
import com.example.chamasegura.data.vm.UserViewModel

class fragment_contact_us : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sendButton: Button = view.findViewById(R.id.sendButton)
        val subjectEditText: EditText = view.findViewById(R.id.subjectEditText)
        val messageEditText: EditText = view.findViewById(R.id.messageEditText)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        sendButton.setOnClickListener {
            val subject = subjectEditText.text.toString().trim()
            val message = messageEditText.text.toString().trim()
            if (subject.isNotEmpty() && message.isNotEmpty()) {
                userViewModel.sendContactMessage(subject, message) { success, errorMessage ->
                    if (success) {
                        Toast.makeText(requireContext(), getString(R.string.message_sent_success), Toast.LENGTH_LONG).show()
                        subjectEditText.text.clear()
                        messageEditText.text.clear()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.message_sent_failure, errorMessage), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.enter_subject_message), Toast.LENGTH_LONG).show()
            }
        }
    }
}
package com.example.chamasegura.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chamasegura.R
import com.example.chamasegura.data.vm.UserViewModel

class fragment_manage_users : Fragment() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        recyclerView = view.findViewById(R.id.recycler_view_manage_users)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ManageUsersAdapter { userId ->
            val action = fragment_manage_usersDirections.actionFragmentManageUsersToFragmentManageUser(userId)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        userViewModel.users.observe(viewLifecycleOwner) { users ->
            if (users != null) {
                adapter.setUsers(users)
            }
        }

        val sortButton = view.findViewById<TextView>(R.id.sort_button)
        sortButton.setOnClickListener {
            userViewModel.sortUsersByName()
        }

        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                userViewModel.searchUsers(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        userViewModel.getAllUsers()

        val registerButton = view.findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_manage_users_to_fragment_register_manage_users)
        }
    }
}
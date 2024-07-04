package com.example.chamasegura.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.vm.BurnViewModel
import com.example.chamasegura.utils.AuthManager
import com.example.chamasegura.utils.JwtUtils

class fragment_my_burn_history : Fragment() {

    private lateinit var burnViewModel: BurnViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyBurnHistoryAdapter
    private var userId: Int? = null
    private var selectedBurnType: BurnType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_burn_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        recyclerView = view.findViewById(R.id.recycler_view_burn_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyBurnHistoryAdapter { burn ->
            val action = fragment_my_burn_historyDirections.actionFragmentBurnHistoryToFragmentBurnInfo(burn)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter

        burnViewModel = ViewModelProvider(this).get(BurnViewModel::class.java)

        val authManager = AuthManager(requireContext())
        val token = authManager.getToken()
        userId = token?.let { JwtUtils.getUserIdFromToken(it) }

        userId?.let {
            burnViewModel.getBurnsByUser(it)
        }

        burnViewModel.burns.observe(viewLifecycleOwner) { burns ->
            if (burns != null) {
                adapter.setBurns(burns)
            }
        }

        val sortButton = view.findViewById<TextView>(R.id.sort_button)
        sortButton.setOnClickListener {
            userId?.let {
                burnViewModel.getBurnsByUserOrderedByDate(it, selectedBurnType)
            }
        }

        val spinnerType = view.findViewById<Spinner>(R.id.spinnerType)
        val types = resources.getStringArray(R.array.burn_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedBurnType = when (position) {
                    0 -> null
                    1 -> BurnType.REGCLEAN
                    2 -> BurnType.PARTICULAR
                    else -> null
                }
                userId?.let { userId ->
                    if (selectedBurnType != null) {
                        burnViewModel.getBurnsByUserAndType(userId, selectedBurnType!!)
                    } else {
                        burnViewModel.getBurnsByUser(userId)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedBurnType = null
                userId?.let { burnViewModel.getBurnsByUser(it) }
            }
        }
    }
}
package com.example.chamasegura.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.vm.BurnViewModel
import com.example.chamasegura.data.vm.MunicipalityViewModel
import com.example.chamasegura.data.vm.UserViewModel
import com.example.chamasegura.utils.AuthManager
import com.example.chamasegura.utils.JwtUtils

class fragment_county_history : Fragment() {

    private lateinit var burnViewModel: BurnViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var municipalityViewModel: MunicipalityViewModel
    private lateinit var burnAdapter: CountyHistoryAdapter
    private var responsibleUserId: Int = 0
    private var concelho: String? = null
    private var selectedBurnType: BurnType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_county_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        burnAdapter = CountyHistoryAdapter { burn ->
            val action =
                fragment_county_historyDirections.actionFragmentCountyHistoryToFragmentBurnInfo(burn)
            findNavController().navigate(action)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_county_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = burnAdapter

        burnViewModel = ViewModelProvider(this).get(BurnViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        municipalityViewModel = ViewModelProvider(this).get(MunicipalityViewModel::class.java)

        burnViewModel.burns.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                burnAdapter.setBurns(it)
            }
        })

        // Fetch responsible user ID from AuthManager or other source
        val authManager = AuthManager(requireContext())
        val token = authManager.getToken()
        responsibleUserId = token?.let { JwtUtils.getUserIdFromToken(it) } ?: 0

        if (responsibleUserId != 0) {
            userViewModel.getUser(responsibleUserId).observe(viewLifecycleOwner, Observer { user ->
                if (user != null && (user.type == UserType.CM || user.type == UserType.ICNF)) {
                    municipalityViewModel.getMunicipalityByResponsibleUser(responsibleUserId)
                        .observe(viewLifecycleOwner, Observer { municipality ->
                            if (municipality != null) {
                                concelho = municipality.name
                                burnViewModel.getBurnsByConcelho(concelho!!)
                            }
                        })
                } else {
                    Toast.makeText(requireContext(), "Access Denied", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val sortButton = view.findViewById<TextView>(R.id.sort_button)
        sortButton.setOnClickListener {
            if (concelho != null) {
                burnViewModel.getBurnsByConcelhoOrdered(concelho!!, selectedBurnType)
            }
        }

        val spinnerType = view.findViewById<Spinner>(R.id.spinnerType)
        val types = resources.getStringArray(R.array.burn_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedBurnType = when (position) {
                    0 -> null
                    1 -> BurnType.REGCLEAN
                    2 -> BurnType.PARTICULAR
                    else -> null
                }
                if (concelho != null) {
                    burnViewModel.getBurnsByConcelhoOrdered(concelho!!, selectedBurnType)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedBurnType = null
                if (concelho != null) {
                    burnViewModel.getBurnsByConcelhoOrdered(concelho!!, selectedBurnType)
                }
            }
        }
    }
}
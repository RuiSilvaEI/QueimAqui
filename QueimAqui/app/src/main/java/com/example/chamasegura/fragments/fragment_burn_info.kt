package com.example.chamasegura.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.BurnState
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.vm.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class fragment_burn_info : Fragment() {
    private val args: fragment_burn_infoArgs by navArgs()
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_burn_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val burn = args.burn
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val openMapButton = view.findViewById<Button>(R.id.openMapButton)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        userViewModel.getUserById(burn.userId).observe(viewLifecycleOwner) { user ->
            view.findViewById<TextView>(R.id.from).text = getString(R.string.from, user?.name ?: getString(R.string.unknown))
        }

        view.findViewById<TextView>(R.id.status).text = getString(R.string.status, getBurnStateString(burn.state))
        view.findViewById<TextView>(R.id.type).text = getString(R.string.type, getBurnTypeString(burn.type))
        view.findViewById<TextView>(R.id.reason).text = getString(R.string.reason, burn.reason)
        view.findViewById<TextView>(R.id.date).text = getString(R.string.date, formatDate(burn.date))
        view.findViewById<TextView>(R.id.location).text = getString(R.string.location, burn.distrito, burn.concelho, burn.freguesia)
        view.findViewById<TextView>(R.id.otherData).text = getString(R.string.otherData, burn.otherData)

        openMapButton.setOnClickListener {
            val action = fragment_burn_infoDirections.actionFragmentBurnInfoToFragmentMapBurnInfo(burn.latitude, burn.longitude)
            findNavController().navigate(action)
        }
    }

    private fun getBurnStateString(state: BurnState): String {
        return when (state) {
            BurnState.PENDING -> getString(R.string.state_pending)
            BurnState.APPROVED -> getString(R.string.state_approved)
            BurnState.DENIED -> getString(R.string.state_denied)
        }
    }

    private fun getBurnTypeString(type: BurnType): String {
        return when (type) {
            BurnType.REGCLEAN -> getString(R.string.regular_clean)
            BurnType.PARTICULAR -> getString(R.string.particular)
        }
    }

    private fun formatDate(dateStr: String): String {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = isoDateFormat.parse(dateStr)
        return date?.let { outputDateFormat.format(it) } ?: dateStr
    }
}
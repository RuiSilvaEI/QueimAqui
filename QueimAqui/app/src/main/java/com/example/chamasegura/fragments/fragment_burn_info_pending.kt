package com.example.chamasegura.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.BurnState
import com.example.chamasegura.data.entities.BurnType
import com.example.chamasegura.data.vm.BurnViewModel
import com.example.chamasegura.data.vm.UserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class fragment_burn_info_pending : Fragment() {
    private val args: fragment_burn_infoArgs by navArgs()
    private lateinit var userViewModel: UserViewModel
    private lateinit var burnViewModel: BurnViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_burn_info_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val burn = args.burn
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val denyButton = view.findViewById<Button>(R.id.denyButton)
        val openMapButton = view.findViewById<Button>(R.id.openMapButton)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        burnViewModel = ViewModelProvider(this).get(BurnViewModel::class.java)

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
            val action = fragment_burn_info_pendingDirections.actionFragmentBurnInfoPendingToFragmentMapBurnInfo(burn.latitude, burn.longitude)
            findNavController().navigate(action)
        }

        confirmButton.setOnClickListener {
            showConfirmationDialog(burn.id, BurnState.APPROVED)
        }

        denyButton.setOnClickListener {
            showConfirmationDialog(burn.id, BurnState.DENIED)
        }
    }

    private fun showConfirmationDialog(burnId: Int, newState: BurnState) {
        val message = if (newState == BurnState.APPROVED) {
            getString(R.string.confirm_approve_burn)
        } else {
            getString(R.string.confirm_deny_burn)
        }

        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.yes) { _, _ ->
                updateBurnState(burnId, newState)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun updateBurnState(burnId: Int, newState: BurnState) {
        burnViewModel.updateBurnState(burnId, newState.name).observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), getString(R.string.burn_state_update_success), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), getString(R.string.burn_state_update_error), Toast.LENGTH_SHORT).show()
            }
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
package com.example.chamasegura.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.chamasegura.R
import com.example.chamasegura.data.entities.StateUser
import com.example.chamasegura.data.entities.User
import com.example.chamasegura.data.entities.UserType
import com.example.chamasegura.data.vm.UserViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class fragment_manage_user : Fragment() {

    private val args: fragment_manage_userArgs by navArgs()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextNif: EditText
    private lateinit var buttonConfirm: Button
    private lateinit var buttonChangePhoto: Button
    private lateinit var profileImage: ImageView
    private lateinit var textViewMemberSince: TextView
    private lateinit var textViewNumberOfBurnRequests: TextView
    private lateinit var buttonChangePassword: Button
    private lateinit var buttonChangeState: Button

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextFullName = view.findViewById(R.id.editTextFullName)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextNif = view.findViewById(R.id.editTextNif)
        buttonConfirm = view.findViewById(R.id.buttonConfirm)
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto)
        profileImage = view.findViewById(R.id.imageViewProfile)
        textViewMemberSince = view.findViewById(R.id.memberSince)
        textViewNumberOfBurnRequests = view.findViewById(R.id.numberOfBurnRequests)
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword)
        buttonChangeState = view.findViewById(R.id.buttonDeleteProfile)

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val userId = args.userId
        userViewModel.getUser(userId).observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                // Atualize a interface do utilizador com os dados do utilizador
                editTextFullName.setText(user.name)
                editTextEmail.setText(user.email)
                editTextNif.setText(user.nif?.toString())
                textViewMemberSince.text = formatDate(user.createdAt)

                // Carregar a imagem de perfil usando Glide
                user.photo?.let {
                    val imageUrl = it
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .into(profileImage)

                    profileImage.setOnClickListener {
                        val dialogFragment = fragment_expanded_image.newInstance(imageUrl)
                        dialogFragment.show(parentFragmentManager, "expandedImage")
                    }
                }

                userViewModel.getNumberOfBurnRequests(userId)
                userViewModel.numberOfBurnRequests.observe(viewLifecycleOwner, Observer { count ->
                    textViewNumberOfBurnRequests.text = count.toString()
                })

                updateButtonState(user.state)

                // Adicionar TextWatcher para validar o NIF
                editTextNif.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val text = s.toString()
                        if (text.length > 9) {
                            editTextNif.error = getString(R.string.invalid_nif_length)
                        }
                    }
                })

                // Limitar a entrada do NIF para apenas dígitos e no máximo 9 caracteres
                editTextNif.filters = arrayOf(InputFilter.LengthFilter(9))

                buttonConfirm.setOnClickListener {
                    val fullName = editTextFullName.text.toString().trim()
                    val email = editTextEmail.text.toString().trim()
                    val nif = editTextNif.text.toString().toIntOrNull()

                    if (fullName.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.empty_full_name), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    if (email.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.empty_email), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(requireContext(), getString(R.string.invalid_email_format), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    if (nif == null || editTextNif.text.toString().length != 9) {
                        Toast.makeText(requireContext(), getString(R.string.invalid_nif_message), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    val updatedUser = User(
                        id = userId,
                        name = fullName,
                        email = email,
                        password = "",
                        photo = null,
                        type = UserType.REGULAR,
                        createdAt = "",
                        updatedAt = "",
                        state = user.state,
                        nif = nif
                    )

                    userViewModel.updateUser(userId, updatedUser) { success, errorMessage ->
                        val message = when {
                            errorMessage?.contains("Email already in use by another user") == true -> getString(R.string.error_email_in_use)
                            errorMessage?.contains("NIF already in use by another user") == true -> getString(R.string.error_nif_in_use)
                            else -> getString(R.string.error_update_profile)
                        }
                        if (success) {
                            Toast.makeText(requireContext(), getString(R.string.profile_updated_success), Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_fragment_manage_user_to_fragment_manage_users)
                        } else {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                buttonChangePhoto.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                }

                buttonChangeState.setOnClickListener {
                    val newState = if (user.state == StateUser.ENABLED) StateUser.DISABLED else StateUser.ENABLED
                    userViewModel.updateUserState(userId, newState) { success, errorMessage ->
                        if (success) {
                            user.state = newState
                            updateButtonState(newState)
                            val message = if (newState == StateUser.ENABLED) getString(R.string.profile_enabled_success) else getString(R.string.profile_disabled_success)
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Error updating user state: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                buttonChangePassword.setOnClickListener {
                    val action = fragment_manage_userDirections.actionFragmentManageUserToFragmentChangePasswordIcnf(userId)
                    findNavController().navigate(action)
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_user_id_extraction), Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_fragment_manage_user_to_fragment_login)
            }
        })

        checkAndRequestPermissions()
    }

    private fun updateButtonState(state: StateUser) {
        val backgroundColor = if (state == StateUser.ENABLED) {
            ContextCompat.getColor(requireContext(), R.color.red)
        } else {
            ContextCompat.getColor(requireContext(), R.color.green)
        }

        buttonChangeState.apply {
            text = if (state == StateUser.ENABLED) getString(R.string.disable_profile) else getString(R.string.enable_profile)
            setBackgroundResource(R.drawable.rounded_button_change_state)
            backgroundTintList = ColorStateList.valueOf(backgroundColor)
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permissão concedida, continue com a ação
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_denied_message), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            profileImage.setImageURI(selectedImageUri)

            val userId = args.userId
            if (userId != null && selectedImageUri != null) {
                val fileExtension = getFileExtension(selectedImageUri!!)
                if (fileExtension != null && (fileExtension == "jpeg" || fileExtension == "jpg" || fileExtension == "png" || fileExtension == "gif")) {
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                        val file = File(requireContext().cacheDir, "temp_image.$fileExtension")
                        val outputStream = FileOutputStream(file)
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input?.copyTo(output)
                            }
                        }
                        val requestFile = file.asRequestBody("image/$fileExtension".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

                        userViewModel.updatePhoto(userId, body) { success, errorMessage ->
                            if (success) {
                                Toast.makeText(requireContext(), getString(R.string.profile_photo_update_success), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.profile_photo_update_error, errorMessage), Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.file_processing_error, e.message), Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_image_file_error), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val mimeTypeMap = android.webkit.MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun formatDate(dateString: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = sdf.parse(dateString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return outputFormat.format(date)
    }
}
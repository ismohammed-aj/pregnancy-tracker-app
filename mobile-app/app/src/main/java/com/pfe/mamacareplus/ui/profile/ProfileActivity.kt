package com.pfe.mamacareplus.ui.profile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.databinding.ActivityProfileBinding
import com.pfe.mamacareplus.model.response.profile.FemmeProfileResponse
import com.pfe.mamacareplus.model.response.profile.PregnancyProfileResponse
import com.pfe.mamacareplus.repository.risk.RiskAssessmentRepository
import com.pfe.mamacareplus.ui.DatePickerUtils
import com.pfe.mamacareplus.ui.auth.LoginActivity
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.main.MainTab
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.ui.main.MainLayoutSettings
import com.pfe.mamacareplus.ui.pregnancy.PregnancyActivity
import com.pfe.mamacareplus.utils.session.TokenManager
import com.pfe.mamacareplus.viewmodel.PregnancyUiState
import com.pfe.mamacareplus.viewmodel.PregnancyViewModel
import com.pfe.mamacareplus.viewmodel.PasswordChangeUiState
import com.pfe.mamacareplus.viewmodel.ProfileUiState
import com.pfe.mamacareplus.viewmodel.ProfileViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.time.LocalDate
import kotlin.math.roundToInt

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val pregnancyViewModel: PregnancyViewModel by viewModels()
    private val riskRepository by lazy { RiskAssessmentRepository(this) }
    private var currentProfile: FemmeProfileResponse? = null
    private var currentPregnancyProfile: PregnancyProfileResponse? = null
    private var latestRiskLevel: String? = null
    private var pendingCameraImageUri: Uri? = null
    private var pendingCameraImageFile: File? = null

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { handleSelectedAvatar(it) }
    }

    private val cameraCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val uri = pendingCameraImageUri
        val file = pendingCameraImageFile

        pendingCameraImageUri = null
        pendingCameraImageFile = null

        if (captured && uri != null) {
            handleSelectedAvatar(uri)
        } else {
            file?.delete()
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            showAvatarMessage("Permission caméra refusée. Autorisez la caméra pour prendre une nouvelle photo.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setMainContentView(binding.root, MainTab.PROFILE)

        listOf(
            binding.avatarContainer,
            binding.btnAvatarCameraOverlay,
            binding.btnPickAvatar,
            binding.btnSaveProfile,
            binding.btnEditProfileAction,
            binding.btnChangePassword,
            binding.btnUpdatePregnancy,
            binding.btnLogout,
        ).forEach { it.applyModernPressAnimation() }

        binding.tilProfileName.bindLiveValidation(binding.etProfileName) { value ->
            value.trim().isNotBlank()
        }
        binding.tilProfileBirthDate.applyPremiumInputFeedback(binding.etProfileBirthDate, invalidWhenBlank = false) { value ->
            value.isBlank() || runCatching { LocalDate.parse(value.trim()) }.getOrNull()?.isAfter(LocalDate.now()) == false
        }

        binding.switchBackgroundAnimation.isChecked = MainLayoutSettings.isBackgroundAnimationEnabled(this)
        binding.switchBackgroundAnimation.setOnCheckedChangeListener { _, enabled ->
            MainLayoutSettings.setBackgroundAnimationEnabled(this, enabled)
            recreate()
        }

        binding.avatarContainer.setOnClickListener {
            showAvatarOptions()
        }
        binding.ivProfileAvatar.setOnClickListener {
            showAvatarOptions()
        }
        binding.btnAvatarCameraOverlay.setOnClickListener {
            showAvatarOptions()
        }
        binding.btnPickAvatar.setOnClickListener {
            showAvatarOptions()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
        binding.btnEditProfileAction.setOnClickListener {
            scrollToEditProfile()
        }
        binding.cardPersonalInfo.setOnClickListener {
            scrollToEditProfile()
        }
        binding.cardPregnancyInfo.setOnClickListener {
            startActivity(Intent(this, PregnancyActivity::class.java))
        }
        binding.cardHealthInfo.setOnClickListener {
            startActivity(Intent(this, PregnancyActivity::class.java))
        }
        binding.cardSettings.setOnClickListener {
            binding.root.post {
                binding.root.smoothScrollTo(0, binding.cardProfileSettings.top)
            }
        }
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordSheet()
        }
        binding.btnUpdatePregnancy.setOnClickListener {
            startActivity(Intent(this, PregnancyActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        binding.etProfileBirthDate.showSoftInputOnFocus = false
        binding.etProfileBirthDate.setOnClickListener {
            showBirthDatePicker()
        }
        binding.tilProfileBirthDate.setEndIconOnClickListener {
            showBirthDatePicker()
        }

        observeProfile()
        observePregnancy()
        viewModel.loadProfile()
        pregnancyViewModel.loadPregnancyProfile()
        loadLatestRiskAssessment()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
        pregnancyViewModel.loadPregnancyProfile()
        loadLatestRiskAssessment()
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            viewModel.profileState.collect { state ->
                when (state) {
                    is ProfileUiState.Loading -> {
                        setLoading(true, "Chargement du profil...")
                    }

                    is ProfileUiState.Saving -> {
                        setLoading(true, "Enregistrement...")
                    }

                    is ProfileUiState.Success -> {
                        setLoading(false, "")
                        currentProfile = state.profile
                        bindProfile(state.profile)
                    }

                    is ProfileUiState.Error -> {
                        setLoading(false, state.message)
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observePregnancy() {
        lifecycleScope.launch {
            pregnancyViewModel.pregnancyState.collect { state ->
                when (state) {
                    is PregnancyUiState.Loading -> {
                        currentPregnancyProfile = null
                        binding.tvProfilePregnancyStatus.text = "Chargement du profil grossesse"
                        binding.tvPregnancyInfoValue.text = "Chargement des détails grossesse"
                        binding.tvHealthInfoValue.text = "Chargement du statut santé"
                    }

                    is PregnancyUiState.Success -> {
                        currentPregnancyProfile = state.profile
                        renderPregnancyInfo(state.profile)
                    }

                    is PregnancyUiState.Error -> {
                        currentPregnancyProfile = null
                        binding.tvProfilePregnancyStatus.text = "Complétez votre profil grossesse"
                        binding.tvPregnancyInfoValue.text = "Aucun profil grossesse pour le moment"
                        binding.tvHealthInfoValue.text = "Statut santé indisponible"
                    }
                }
            }
        }
    }

    private fun loadLatestRiskAssessment() {
        lifecycleScope.launch {
            latestRiskLevel = riskRepository
                .getLatestRiskAssessment()
                .getOrNull()
                ?.riskLevel
                ?.takeIf { it.isNotBlank() }

            currentPregnancyProfile?.let { renderPregnancyInfo(it) }
        }
    }

    private fun renderPregnancyInfo(profile: PregnancyProfileResponse) {
        val risk = latestRiskLevel ?: "Non défini"
        binding.tvProfilePregnancyStatus.text =
            "Semaine ${profile.currentWeek} | Mois ${profile.currentMonth}"
        binding.tvPregnancyInfoValue.text =
            "Semaine ${profile.currentWeek}, terme ${profile.dueDate}"
        binding.tvHealthInfoValue.text =
            "Risque : $risk | ${profile.remainingDays} jour(s) restant(s)"
    }

    private fun bindProfile(profile: FemmeProfileResponse) {
        val username = profile.username.ifBlank { "MamaCare" }
        binding.tvUsername.text = username
        binding.tvEmail.text = profile.email
        binding.etProfileName.setText(username)
        binding.etProfileBirthDate.setText(profile.birthDate.orEmpty())
        binding.tvPersonalInfoValue.text = if (profile.birthDate.isNullOrBlank()) {
            "$username | date de naissance non définie"
        } else {
            "$username | née le ${profile.birthDate}"
        }
        binding.tvBirthDate.text = if (profile.birthDate.isNullOrBlank()) {
            "Date de naissance non définie"
        } else {
            "Date de naissance : ${profile.birthDate}"
        }
        loadAvatar(profile.avatar)
    }

    private fun saveProfile() {
        val username = binding.etProfileName.text?.toString()?.trim().orEmpty()
        val birthDate = binding.etProfileBirthDate.text?.toString()?.trim().orEmpty()
        binding.tilProfileName.error = null
        binding.tilProfileBirthDate.error = null

        if (username.isBlank()) {
            binding.tilProfileName.error = "Le nom est obligatoire"
            return
        }

        val parsedBirthDate = if (birthDate.isBlank()) {
            null
        } else {
            runCatching { LocalDate.parse(birthDate) }.getOrNull()
        }

        if (birthDate.isNotBlank() && parsedBirthDate == null) {
            binding.tilProfileBirthDate.error = "Choisissez une date valide"
            return
        }

        if (parsedBirthDate != null && parsedBirthDate.isAfter(LocalDate.now())) {
            binding.tilProfileBirthDate.error = "La date ne peut pas être dans le futur"
            return
        }

        viewModel.updateProfile(
            username = username,
            birthDate = birthDate.takeIf { it.isNotBlank() }
        )
    }

    private fun setLoading(loading: Boolean, message: String) {
        binding.btnSaveProfile.isEnabled = !loading
        binding.btnPickAvatar.isEnabled = !loading
        binding.btnAvatarCameraOverlay.isEnabled = !loading
        binding.avatarContainer.isEnabled = !loading
        binding.ivProfileAvatar.isEnabled = !loading
        binding.btnLogout.isEnabled = !loading
        binding.tvProfileMessage.text = message
    }

    private fun showAvatarOptions() {
        val dialog = BottomSheetDialog(this)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_avatar_options, null)

        val takePhotoButton = sheet.findViewById<MaterialButton>(R.id.btnTakePhotoOption)
        val galleryButton = sheet.findViewById<MaterialButton>(R.id.btnChooseGalleryOption)
        takePhotoButton.applyModernPressAnimation()
        galleryButton.applyModernPressAnimation()

        takePhotoButton.setOnClickListener {
            dialog.dismiss()
            startCameraFlow()
        }

        galleryButton.setOnClickListener {
            dialog.dismiss()
            openGalleryPicker()
        }

        dialog.setContentView(sheet)
        dialog.show()
    }

    private fun showChangePasswordSheet() {
        viewModel.resetPasswordChangeState()

        val dialog = BottomSheetDialog(this)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_change_password, null)

        val tilCurrent = sheet.findViewById<TextInputLayout>(R.id.tilCurrentPassword)
        val tilNew = sheet.findViewById<TextInputLayout>(R.id.tilNewPassword)
        val tilConfirm = sheet.findViewById<TextInputLayout>(R.id.tilConfirmPassword)
        val etCurrent = sheet.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNew = sheet.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirm = sheet.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val message = sheet.findViewById<TextView>(R.id.tvChangePasswordMessage)
        val progress = sheet.findViewById<View>(R.id.progressChangePassword)
        val cancel = sheet.findViewById<MaterialButton>(R.id.btnCancelChangePassword)
        val submit = sheet.findViewById<MaterialButton>(R.id.btnSubmitChangePassword)
        cancel.applyModernPressAnimation()
        submit.applyModernPressAnimation()
        tilCurrent.bindLiveValidation(etCurrent) { value ->
            value.isNotBlank()
        }
        tilNew.bindLiveValidation(etNew) { value ->
            value.length >= 6
        }
        tilConfirm.bindLiveValidation(etConfirm) { value ->
            value.isNotBlank() && value == etNew.text?.toString().orEmpty()
        }

        fun setSaving(saving: Boolean) {
            submit.isEnabled = !saving
            cancel.isEnabled = !saving
            etCurrent.isEnabled = !saving
            etNew.isEnabled = !saving
            etConfirm.isEnabled = !saving
            submit.text = if (saving) "" else "Enregistrer"
            progress.visibility = if (saving) View.VISIBLE else View.GONE
        }

        fun clearErrors() {
            tilCurrent.error = null
            tilNew.error = null
            tilConfirm.error = null
            message.text = ""
        }

        fun validateAndSubmit() {
            clearErrors()
            val currentPassword = etCurrent.text?.toString().orEmpty()
            val newPassword = etNew.text?.toString().orEmpty()
            val confirmPassword = etConfirm.text?.toString().orEmpty()

            var valid = true
            if (currentPassword.isBlank()) {
                tilCurrent.error = "Mot de passe actuel requis"
                valid = false
            }
            if (newPassword.isBlank()) {
                tilNew.error = "Nouveau mot de passe requis"
                valid = false
            } else if (newPassword.length < 6) {
                tilNew.error = "Le mot de passe doit contenir au moins 6 caractères"
                valid = false
            }
            if (confirmPassword.isBlank()) {
                tilConfirm.error = "Confirmation requise"
                valid = false
            } else if (newPassword != confirmPassword) {
                tilConfirm.error = "Les mots de passe ne correspondent pas"
                valid = false
            }

            if (!valid) return

            viewModel.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
        }

        cancel.setOnClickListener { dialog.dismiss() }
        submit.setOnClickListener { validateAndSubmit() }

        var stateJob: Job? = lifecycleScope.launch {
            viewModel.passwordChangeState.collect { state ->
                when (state) {
                    is PasswordChangeUiState.Idle -> {
                        setSaving(false)
                    }

                    is PasswordChangeUiState.Saving -> {
                        message.text = "Enregistrement du mot de passe..."
                        setSaving(true)
                    }

                    is PasswordChangeUiState.Success -> {
                        setSaving(false)
                        Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }

                    is PasswordChangeUiState.Error -> {
                        setSaving(false)
                        val lowerMessage = state.message.lowercase()
                        when {
                            "current password" in lowerMessage || "mot de passe actuel" in lowerMessage -> tilCurrent.error = state.message
                            "confirmation" in lowerMessage || "match" in lowerMessage || "correspondent" in lowerMessage -> tilConfirm.error = state.message
                        }
                        message.text = state.message
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            stateJob?.cancel()
            stateJob = null
            viewModel.resetPasswordChangeState()
        }
        dialog.setContentView(sheet)
        dialog.show()
    }

    private fun startCameraFlow() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraCapture() {
        val file = createCameraImageFile()
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        pendingCameraImageUri = uri
        pendingCameraImageFile = file

        runCatching {
            cameraCapture.launch(uri)
        }.onFailure {
            pendingCameraImageUri = null
            pendingCameraImageFile = null
            file.delete()
            showAvatarMessage("Aucune application caméra n'est disponible sur cet appareil.")
        }
    }

    private fun createCameraImageFile(): File {
        val directory = File(cacheDir, "profile_images").apply { mkdirs() }
        return File(directory, "profile_avatar_${System.currentTimeMillis()}.jpg")
    }

    private fun openGalleryPicker() {
        avatarPicker.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    private fun handleSelectedAvatar(uri: Uri) {
        showAvatarImage(uri)
        showAvatarMessage("Envoi de la photo de profil...")
        viewModel.uploadAvatar(uri)
    }

    private fun showAvatarMessage(message: String) {
        binding.tvProfileMessage.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun scrollToEditProfile() {
        binding.root.post {
            binding.root.smoothScrollTo(0, binding.cardEditProfile.top)
            binding.etProfileName.requestFocus()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Déconnexion") { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {
        TokenManager(this).clearSession()
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun showBirthDatePicker() {
        DatePickerUtils.showIsoDatePicker(
            context = this,
            currentValue = binding.etProfileBirthDate.text?.toString(),
            maxDate = LocalDate.now()
        ) { selectedDate ->
            binding.etProfileBirthDate.setText(selectedDate)
            binding.tilProfileBirthDate.error = null
        }
    }

    private fun loadAvatar(avatar: String?) {
        val url = resolveAvatarUrl(avatar)

        if (url.isNullOrBlank()) {
            showAvatarPlaceholder()
            return
        }

        if (url.startsWith("content:")) {
            showAvatarImage(Uri.parse(url))
            return
        }

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }

            if (bitmap != null) {
                prepareAvatarForPhoto()
                binding.ivProfileAvatar.setImageBitmap(bitmap)
            } else {
                showAvatarPlaceholder()
            }
        }
    }

    private fun showAvatarImage(uri: Uri) {
        prepareAvatarForPhoto()
        binding.ivProfileAvatar.setImageURI(uri)
    }

    private fun prepareAvatarForPhoto() {
        binding.ivProfileAvatar.imageTintList = null
        binding.ivProfileAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.ivProfileAvatar.setPadding(0, 0, 0, 0)
    }

    private fun showAvatarPlaceholder() {
        val padding = 26.dp()
        binding.ivProfileAvatar.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.ivProfileAvatar.setPadding(padding, padding, padding, padding)
        binding.ivProfileAvatar.setImageResource(R.drawable.ic_profile_24)
        binding.ivProfileAvatar.imageTintList =
            ColorStateList.valueOf(android.graphics.Color.parseColor("#16B8C7"))
    }

    private fun resolveAvatarUrl(avatar: String?): String? {
        val cleaned = avatar?.trim().orEmpty()
        if (cleaned.isBlank()) return null

        return when {
            cleaned.startsWith("http://") || cleaned.startsWith("https://") || cleaned.startsWith("content:") -> cleaned
            cleaned.startsWith("/") -> ApiClient.BASE_URL.trimEnd('/') + cleaned
            else -> ApiClient.BASE_URL + cleaned.trimStart('/')
        }
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).roundToInt()
}

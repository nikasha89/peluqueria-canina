package com.peluqueriacanina.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.sync.CalendarSyncService
import com.peluqueriacanina.app.sync.DriveBackupService
import com.peluqueriacanina.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfiguracionFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnLogin: Button
    private lateinit var btnLogout: Button
    private lateinit var txtUserName: TextView
    private lateinit var txtUserEmail: TextView
    private lateinit var imgUser: ImageView
    private lateinit var btnSyncCalendar: Button
    private lateinit var btnBackupDrive: Button
    private lateinit var btnRestoreDrive: Button
    private lateinit var txtBackupInfo: TextView
    private var progressBar: ProgressBar? = null

    private var driveBackupService: DriveBackupService? = null
    private var calendarSyncService: CalendarSyncService? = null
    
    // Track pending action after consent
    private var pendingAction: PendingAction? = null
    
    private enum class PendingAction {
        BACKUP, RESTORE, CHECK_BACKUP, SYNC_CALENDAR
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Always try to get the account from the intent, regardless of resultCode
        // Google Sign-In might return RESULT_CANCELED even on success
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                authViewModel.setSignedInAccount(account)
                Toast.makeText(context, "¡Bienvenido ${account.displayName}!", Toast.LENGTH_SHORT).show()
                
                // Initialize services and check for backup
                initServices(account)
                checkAndOfferRestore()
            }
        } catch (e: ApiException) {
            android.util.Log.e("ConfiguracionFragment", "Sign-in failed: ${e.statusCode}", e)
            Toast.makeText(context, "Error de inicio de sesión: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }
    
    // Launcher for Google API consent recovery (Drive & Calendar)
    private val googleConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User granted consent, retry the pending action
            when (pendingAction) {
                PendingAction.BACKUP -> createBackup()
                PendingAction.RESTORE -> restoreBackup()
                PendingAction.CHECK_BACKUP -> checkAndOfferRestore()
                PendingAction.SYNC_CALENDAR -> syncCalendar()
                null -> {}
            }
        } else {
            setLoading(false)
            Toast.makeText(context, "Se necesitan permisos de Google para esta función", Toast.LENGTH_LONG).show()
        }
        pendingAction = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_configuracion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        googleSignInClient = authViewModel.getSignInClient(requireActivity())
        
        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogout = view.findViewById(R.id.btnLogout)
        txtUserName = view.findViewById(R.id.txtUserName)
        txtUserEmail = view.findViewById(R.id.txtUserEmail)
        imgUser = view.findViewById(R.id.imgUser)
        btnSyncCalendar = view.findViewById(R.id.btnSyncCalendar)
        btnBackupDrive = view.findViewById(R.id.btnBackupDrive)
        btnRestoreDrive = view.findViewById(R.id.btnRestoreDrive)
        txtBackupInfo = view.findViewById(R.id.txtBackupInfo)
        progressBar = view.findViewById(R.id.progressBar)

        authViewModel.currentUser.observe(viewLifecycleOwner) { account ->
            if (account != null) {
                btnLogin.visibility = View.GONE
                btnLogout.visibility = View.VISIBLE
                txtUserName.text = account.displayName ?: "Usuario"
                txtUserEmail.text = account.email ?: ""
                btnBackupDrive.isEnabled = true
                btnRestoreDrive.isEnabled = true
                btnSyncCalendar.isEnabled = true
                
                // Initialize services
                initServices(account)
                updateBackupInfo()
            } else {
                btnLogin.visibility = View.VISIBLE
                btnLogout.visibility = View.GONE
                txtUserName.text = "No conectado"
                txtUserEmail.text = "Inicia sesión para sincronizar"
                btnBackupDrive.isEnabled = false
                btnRestoreDrive.isEnabled = false
                btnSyncCalendar.isEnabled = false
                txtBackupInfo.text = ""
                driveBackupService = null
                calendarSyncService = null
            }
        }

        authViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        btnLogin.setOnClickListener {
            signIn()
        }

        btnLogout.setOnClickListener {
            signOut()
        }

        btnSyncCalendar.setOnClickListener {
            syncCalendar()
        }

        btnBackupDrive.setOnClickListener {
            createBackup()
        }

        btnRestoreDrive.setOnClickListener {
            confirmAndRestore()
        }

        // Check for existing sign-in
        authViewModel.checkExistingSignIn(requireActivity())
    }

    private fun initServices(account: GoogleSignInAccount) {
        driveBackupService = DriveBackupService(requireContext(), account)
        calendarSyncService = CalendarSyncService(requireContext(), account)
    }

    private fun updateBackupInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val backupInfo = driveBackupService?.getBackupInfo()
                if (backupInfo != null) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val date = Date(backupInfo.modifiedTime)
                    val sizeKb = backupInfo.size / 1024
                    txtBackupInfo.text = "Último backup: ${dateFormat.format(date)} (${sizeKb} KB)"
                } else {
                    txtBackupInfo.text = "No hay backup en Google Drive"
                }
            } catch (e: UserRecoverableAuthIOException) {
                txtBackupInfo.text = "Pulsa en backup para autorizar"
            } catch (e: Exception) {
                txtBackupInfo.text = "Error verificando backup"
            }
        }
    }

    private fun syncCalendar() {
        val service = calendarSyncService ?: run {
            Toast.makeText(context, "Inicia sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = service.syncAllCitas()
            setLoading(false)
            
            result.fold(
                onSuccess = { syncResult ->
                    val message = "✓ Sincronizado: ${syncResult.created} creados, ${syncResult.updated} actualizados"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    if (syncResult.hasErrors) {
                        Toast.makeText(context, "⚠️ ${syncResult.errors} errores", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { error ->
                    if (error is UserRecoverableAuthIOException) {
                        // Need additional consent for Calendar
                        pendingAction = PendingAction.SYNC_CALENDAR
                        googleConsentLauncher.launch(error.intent)
                    } else {
                        Toast.makeText(context, "Error al sincronizar: ${error.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("ConfiguracionFragment", "Calendar sync failed", error)
                    }
                }
            )
        }
    }

    private fun createBackup() {
        val service = driveBackupService ?: run {
            Toast.makeText(context, "Inicia sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = service.createBackup()
            setLoading(false)
            
            result.fold(
                onSuccess = { message ->
                    Toast.makeText(context, "✓ Backup creado exitosamente", Toast.LENGTH_SHORT).show()
                    updateBackupInfo()
                },
                onFailure = { error ->
                    if (error is UserRecoverableAuthIOException) {
                        // Need additional consent for Drive
                        pendingAction = PendingAction.BACKUP
                        googleConsentLauncher.launch(error.intent)
                    } else {
                        Toast.makeText(context, "Error al crear backup: ${error.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("ConfiguracionFragment", "Backup failed", error)
                    }
                }
            )
        }
    }

    private fun confirmAndRestore() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restaurar datos")
            .setMessage("¿Estás seguro? Esto reemplazará todos los datos actuales con los del backup.")
            .setPositiveButton("Restaurar") { _, _ ->
                restoreBackup()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restoreBackup() {
        val service = driveBackupService ?: run {
            Toast.makeText(context, "Inicia sesión primero", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = service.restoreBackup()
            setLoading(false)
            
            result.fold(
                onSuccess = { message ->
                    Toast.makeText(context, "✓ Datos restaurados exitosamente", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    if (error is UserRecoverableAuthIOException) {
                        // Need additional consent for Drive
                        pendingAction = PendingAction.RESTORE
                        googleConsentLauncher.launch(error.intent)
                    } else {
                        Toast.makeText(context, "Error al restaurar: ${error.message}", Toast.LENGTH_LONG).show()
                        android.util.Log.e("ConfiguracionFragment", "Restore failed", error)
                    }
                }
            )
        }
    }

    private fun checkAndOfferRestore() {
        val service = driveBackupService ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val hasBackup = service.hasBackup()
                if (hasBackup) {
                    val backupInfo = service.getBackupInfo()
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val dateStr = if (backupInfo != null) {
                        dateFormat.format(Date(backupInfo.modifiedTime))
                    } else {
                        "fecha desconocida"
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle("Backup encontrado")
                        .setMessage("Se encontró un backup en Google Drive del $dateStr.\n\n¿Deseas restaurar los datos?")
                        .setPositiveButton("Restaurar") { _, _ ->
                            restoreBackup()
                        }
                        .setNegativeButton("No, gracias", null)
                        .show()
                }
                updateBackupInfo()
            } catch (e: UserRecoverableAuthIOException) {
                // Need additional consent for Drive
                pendingAction = PendingAction.CHECK_BACKUP
                googleConsentLauncher.launch(e.intent)
            } catch (e: Exception) {
                android.util.Log.e("ConfiguracionFragment", "Error checking backup", e)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar?.visibility = if (loading) View.VISIBLE else View.GONE
        btnBackupDrive.isEnabled = !loading
        btnRestoreDrive.isEnabled = !loading
        btnSyncCalendar.isEnabled = !loading
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            authViewModel.signOut(requireActivity())
            driveBackupService = null
            calendarSyncService = null
            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }
    }
}

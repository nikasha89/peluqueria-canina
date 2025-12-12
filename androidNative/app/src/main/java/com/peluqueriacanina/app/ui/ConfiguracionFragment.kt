package com.peluqueriacanina.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.google.android.material.button.MaterialButton
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.peluqueriacanina.app.R
import com.peluqueriacanina.app.data.LongitudPelo
import com.peluqueriacanina.app.data.Raza
import com.peluqueriacanina.app.data.Tamano
import com.peluqueriacanina.app.sync.CalendarSyncService
import com.peluqueriacanina.app.sync.DriveBackupService
import com.peluqueriacanina.app.viewmodel.AuthViewModel
import com.peluqueriacanina.app.viewmodel.ServicioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConfiguracionFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()
    private val servicioViewModel: ServicioViewModel by activityViewModels()
    
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
    
    // Razas
    private lateinit var layoutRazas: LinearLayout
    private lateinit var txtNoRazas: TextView
    private lateinit var btnAddRaza: MaterialButton
    private lateinit var contentRazas: LinearLayout
    private lateinit var btnExpandRazas: ImageButton
    
    // Tamaños
    private lateinit var layoutTamanos: LinearLayout
    private lateinit var txtNoTamanos: TextView
    private lateinit var btnAddTamano: MaterialButton
    private lateinit var contentTamanos: LinearLayout
    private lateinit var btnExpandTamanos: ImageButton
    
    // Longitudes de pelo
    private lateinit var layoutPelos: LinearLayout
    private lateinit var txtNoPelos: TextView
    private lateinit var btnAddPelo: MaterialButton
    private lateinit var contentPelos: LinearLayout
    private lateinit var btnExpandPelos: ImageButton
    
    // Estado de expansión
    private var razasExpanded = false
    private var tamanosExpanded = false
    private var pelosExpanded = false

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
        
        // Razas
        layoutRazas = view.findViewById(R.id.layoutRazas)
        txtNoRazas = view.findViewById(R.id.txtNoRazas)
        btnAddRaza = view.findViewById(R.id.btnAddRaza)
        contentRazas = view.findViewById(R.id.contentRazas)
        btnExpandRazas = view.findViewById(R.id.btnExpandRazas)
        
        // Tamaños
        layoutTamanos = view.findViewById(R.id.layoutTamanos)
        txtNoTamanos = view.findViewById(R.id.txtNoTamanos)
        btnAddTamano = view.findViewById(R.id.btnAddTamano)
        contentTamanos = view.findViewById(R.id.contentTamanos)
        btnExpandTamanos = view.findViewById(R.id.btnExpandTamanos)
        
        // Longitudes de pelo
        layoutPelos = view.findViewById(R.id.layoutPelos)
        txtNoPelos = view.findViewById(R.id.txtNoPelos)
        btnAddPelo = view.findViewById(R.id.btnAddPelo)
        contentPelos = view.findViewById(R.id.contentPelos)
        btnExpandPelos = view.findViewById(R.id.btnExpandPelos)
        
        // Setup expand/collapse para razas
        view.findViewById<LinearLayout>(R.id.headerRazas).setOnClickListener {
            toggleRazasExpanded()
        }
        btnExpandRazas.setOnClickListener {
            toggleRazasExpanded()
        }
        
        // Setup expand/collapse para tamaños
        view.findViewById<LinearLayout>(R.id.headerTamanos).setOnClickListener {
            toggleTamanosExpanded()
        }
        btnExpandTamanos.setOnClickListener {
            toggleTamanosExpanded()
        }
        
        // Setup expand/collapse para pelos
        view.findViewById<LinearLayout>(R.id.headerPelos).setOnClickListener {
            togglePelosExpanded()
        }
        btnExpandPelos.setOnClickListener {
            togglePelosExpanded()
        }
        
        // Observar razas
        servicioViewModel.allRazas.observe(viewLifecycleOwner) { razas ->
            updateRazasUI(razas)
        }
        
        // Observar tamaños
        servicioViewModel.allTamanos.observe(viewLifecycleOwner) { tamanos ->
            updateTamanosUI(tamanos)
        }
        
        // Observar longitudes de pelo
        servicioViewModel.allLongitudesPelo.observe(viewLifecycleOwner) { pelos ->
            updatePelosUI(pelos)
        }
        
        btnAddRaza.setOnClickListener {
            showAddRazaDialog()
        }
        
        btnAddTamano.setOnClickListener {
            showAddTamanoDialog()
        }
        
        btnAddPelo.setOnClickListener {
            showAddPeloDialog()
        }

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
        
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val hasBackup = service.hasBackup()
                if (hasBackup) {
                    // Backup found - restore automatically
                    android.util.Log.d("ConfiguracionFragment", "Backup found, restoring automatically...")
                    val result = service.restoreBackup()
                    setLoading(false)
                    
                    result.fold(
                        onSuccess = {
                            val backupInfo = service.getBackupInfo()
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val dateStr = if (backupInfo != null) {
                                dateFormat.format(Date(backupInfo.modifiedTime))
                            } else {
                                ""
                            }
                            Toast.makeText(context, "✓ Datos restaurados del $dateStr", Toast.LENGTH_LONG).show()
                        },
                        onFailure = { error ->
                            if (error is UserRecoverableAuthIOException) {
                                // Need additional consent for Drive
                                pendingAction = PendingAction.CHECK_BACKUP
                                googleConsentLauncher.launch(error.intent)
                            } else {
                                Toast.makeText(context, "Error al restaurar: ${error.message}", Toast.LENGTH_LONG).show()
                                android.util.Log.e("ConfiguracionFragment", "Auto-restore failed", error)
                            }
                        }
                    )
                } else {
                    // No backup found - ask if user wants to create one
                    setLoading(false)
                    AlertDialog.Builder(requireContext())
                        .setTitle("Sin backup")
                        .setMessage("No se encontró ningún backup en Google Drive.\n\n¿Deseas crear uno ahora con los datos actuales?")
                        .setPositiveButton("Crear backup") { _, _ ->
                            createBackup()
                        }
                        .setNegativeButton("Más tarde", null)
                        .show()
                }
                updateBackupInfo()
            } catch (e: UserRecoverableAuthIOException) {
                setLoading(false)
                // Need additional consent for Drive
                pendingAction = PendingAction.CHECK_BACKUP
                googleConsentLauncher.launch(e.intent)
            } catch (e: Exception) {
                setLoading(false)
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
    
    private fun updateRazasUI(razas: List<Raza>) {
        layoutRazas.removeAllViews()
        
        if (razas.isEmpty()) {
            txtNoRazas.visibility = View.VISIBLE
        } else {
            txtNoRazas.visibility = View.GONE
            razas.forEach { raza ->
                val razaView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_raza_config, layoutRazas, false)
                
                razaView.findViewById<TextView>(R.id.txtRazaNombre).text = raza.nombre
                
                razaView.findViewById<ImageButton>(R.id.btnEditRaza).setOnClickListener {
                    showEditRazaDialog(raza)
                }
                
                razaView.findViewById<ImageButton>(R.id.btnDeleteRaza).setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar raza")
                        .setMessage("¿Estás seguro de eliminar '${raza.nombre}'?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            servicioViewModel.deleteRaza(raza)
                            Toast.makeText(context, "Raza eliminada", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                
                layoutRazas.addView(razaView)
            }
        }
    }
    
    private fun updateTamanosUI(tamanos: List<Tamano>) {
        layoutTamanos.removeAllViews()
        
        if (tamanos.isEmpty()) {
            txtNoTamanos.visibility = View.VISIBLE
        } else {
            txtNoTamanos.visibility = View.GONE
            tamanos.forEach { tamano ->
                val tamanoView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_raza_config, layoutTamanos, false)
                
                tamanoView.findViewById<TextView>(R.id.txtRazaNombre).text = tamano.nombre.replaceFirstChar { it.uppercase() }
                
                tamanoView.findViewById<ImageButton>(R.id.btnEditRaza).setOnClickListener {
                    showEditTamanoDialog(tamano)
                }
                
                tamanoView.findViewById<ImageButton>(R.id.btnDeleteRaza).setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar tamaño")
                        .setMessage("¿Estás seguro de eliminar '${tamano.nombre}'?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            servicioViewModel.deleteTamano(tamano)
                            Toast.makeText(context, "Tamaño eliminado", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                
                layoutTamanos.addView(tamanoView)
            }
        }
    }
    
    private fun updatePelosUI(pelos: List<LongitudPelo>) {
        layoutPelos.removeAllViews()
        
        if (pelos.isEmpty()) {
            txtNoPelos.visibility = View.VISIBLE
        } else {
            txtNoPelos.visibility = View.GONE
            pelos.forEach { pelo ->
                val peloView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_raza_config, layoutPelos, false)
                
                peloView.findViewById<TextView>(R.id.txtRazaNombre).text = pelo.nombre.replaceFirstChar { it.uppercase() }
                
                peloView.findViewById<ImageButton>(R.id.btnEditRaza).setOnClickListener {
                    showEditPeloDialog(pelo)
                }
                
                peloView.findViewById<ImageButton>(R.id.btnDeleteRaza).setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar longitud de pelo")
                        .setMessage("¿Estás seguro de eliminar '${pelo.nombre}'?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            servicioViewModel.deleteLongitudPelo(pelo)
                            Toast.makeText(context, "Longitud eliminada", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                
                layoutPelos.addView(peloView)
            }
        }
    }
    
    private fun toggleRazasExpanded() {
        razasExpanded = !razasExpanded
        contentRazas.visibility = if (razasExpanded) View.VISIBLE else View.GONE
        btnExpandRazas.rotation = if (razasExpanded) 180f else 0f
    }
    
    private fun toggleTamanosExpanded() {
        tamanosExpanded = !tamanosExpanded
        contentTamanos.visibility = if (tamanosExpanded) View.VISIBLE else View.GONE
        btnExpandTamanos.rotation = if (tamanosExpanded) 180f else 0f
    }
    
    private fun togglePelosExpanded() {
        pelosExpanded = !pelosExpanded
        contentPelos.visibility = if (pelosExpanded) View.VISIBLE else View.GONE
        btnExpandPelos.rotation = if (pelosExpanded) 180f else 0f
    }
    
    private fun showAddRazaDialog() {
        val input = EditText(requireContext())
        input.hint = "Nombre de la raza"
        input.setPadding(48, 32, 48, 32)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Nueva raza")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servicioViewModel.insertRaza(Raza(nombre = nombre))
                Toast.makeText(context, "Raza añadida", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditRazaDialog(raza: Raza) {
        val input = EditText(requireContext())
        input.hint = "Nombre de la raza"
        input.setText(raza.nombre)
        input.setPadding(48, 32, 48, 32)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar raza")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servicioViewModel.updateRaza(raza.copy(nombre = nombre))
                Toast.makeText(context, "Raza actualizada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showAddTamanoDialog() {
        val input = EditText(requireContext())
        input.hint = "Nombre del tamaño"
        input.setPadding(48, 32, 48, 32)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo tamaño")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val nombre = input.text.toString().trim().lowercase()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servicioViewModel.insertTamano(Tamano(nombre = nombre))
                Toast.makeText(context, "Tamaño añadido", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditTamanoDialog(tamano: Tamano) {
        val input = EditText(requireContext())
        input.hint = "Nombre del tamaño"
        input.setText(tamano.nombre)
        input.setPadding(48, 32, 48, 32)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar tamaño")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString().trim().lowercase()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servicioViewModel.updateTamano(tamano.copy(nombre = nombre))
                Toast.makeText(context, "Tamaño actualizado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showAddPeloDialog() {
        val input = EditText(requireContext())
        input.hint = "Longitud del pelo"
        input.setPadding(48, 32, 48, 32)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Nueva longitud de pelo")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val nombre = input.text.toString().trim().lowercase()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servicioViewModel.insertLongitudPelo(LongitudPelo(nombre = nombre))
                Toast.makeText(context, "Longitud añadida", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditPeloDialog(pelo: LongitudPelo) {
        val input = EditText(requireContext())
        input.hint = "Longitud del pelo"
        input.setText(pelo.nombre)
        input.setPadding(48, 32, 48, 32)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Editar longitud de pelo")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString().trim().lowercase()
                if (nombre.isEmpty()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                servicioViewModel.updateLongitudPelo(pelo.copy(nombre = nombre))
                Toast.makeText(context, "Longitud actualizada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

package com.peluqueriacanina.app.sync

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manager para auto-guardar en Google Drive cuando hay cambios en los datos.
 * Usa un debounce para evitar múltiples backups en cambios rápidos.
 */
object AutoBackupManager {
    
    private const val TAG = "AutoBackupManager"
    private const val PREFS_NAME = "auto_backup_prefs"
    private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    private const val DEBOUNCE_DELAY_MS = 5000L // 5 segundos de espera antes de hacer backup
    
    private var debounceJob: Job? = null
    private var driveBackupService: DriveBackupService? = null
    
    /**
     * Inicializa el servicio de backup con la cuenta de Google actual
     */
    fun initialize(context: Context) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            driveBackupService = DriveBackupService(context, account)
            Log.d(TAG, "AutoBackupManager initialized with account: ${account.email}")
        } else {
            driveBackupService = null
            Log.d(TAG, "No Google account found, auto-backup disabled")
        }
    }
    
    /**
     * Verifica si el auto-backup está habilitado
     */
    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true) // Habilitado por defecto
    }
    
    /**
     * Habilita o deshabilita el auto-backup
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }
    
    /**
     * Notifica que hubo un cambio en los datos y debe hacerse backup.
     * Usa debounce para evitar múltiples backups consecutivos.
     */
    fun notifyDataChanged(context: Context) {
        if (!isEnabled(context)) {
            Log.d(TAG, "Auto-backup disabled, skipping")
            return
        }
        
        // Re-initialize if needed
        if (driveBackupService == null) {
            initialize(context)
        }
        
        if (driveBackupService == null) {
            Log.d(TAG, "No Drive service available, skipping backup")
            return
        }
        
        // Cancel previous debounce job if exists
        debounceJob?.cancel()
        
        // Start new debounce job
        debounceJob = CoroutineScope(Dispatchers.IO).launch {
            delay(DEBOUNCE_DELAY_MS)
            performBackup(context)
        }
        
        Log.d(TAG, "Data change notified, backup scheduled in ${DEBOUNCE_DELAY_MS}ms")
    }
    
    /**
     * Fuerza un backup inmediato sin debounce
     */
    fun forceBackup(context: Context, onComplete: (Boolean) -> Unit = {}) {
        if (driveBackupService == null) {
            initialize(context)
        }
        
        if (driveBackupService == null) {
            Log.e(TAG, "Cannot backup: no Drive service")
            onComplete(false)
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val success = performBackup(context)
            CoroutineScope(Dispatchers.Main).launch {
                onComplete(success)
            }
        }
    }
    
    private suspend fun performBackup(context: Context): Boolean {
        return try {
            val service = driveBackupService ?: return false
            service.createBackup()
            Log.d(TAG, "Auto-backup completed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Auto-backup failed", e)
            false
        }
    }
    
    /**
     * Limpia el servicio (llamar al cerrar sesión)
     */
    fun clear() {
        debounceJob?.cancel()
        debounceJob = null
        driveBackupService = null
    }
}

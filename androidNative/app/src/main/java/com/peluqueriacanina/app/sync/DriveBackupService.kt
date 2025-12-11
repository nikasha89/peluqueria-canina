package com.peluqueriacanina.app.sync

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class DriveBackupService(
    private val context: Context,
    private val account: GoogleSignInAccount
) {
    companion object {
        private const val BACKUP_FILE_NAME = "peluqueria-canina-backup.json"  // Same as webapp
        private const val BACKUP_MIME_TYPE = "application/json"
    }

    private val driveService: Drive by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)  // Same scope as webapp
        )
        credential.selectedAccount = account.account

        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Peluquería Canina")
            .build()
    }

    private val database: AppDatabase
        get() = (context.applicationContext as PeluqueriaApp).database

    /**
     * Creates a backup of all data and uploads it to Google Drive
     */
    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Export all data to JSON
            val backupJson = exportDatabaseToJson()
            
            // Check if backup file already exists
            val existingFileId = findBackupFile()
            
            val fileContent = ByteArrayContent.fromString(BACKUP_MIME_TYPE, backupJson.toString(2))
            
            val resultFile = if (existingFileId != null) {
                // Update existing file
                driveService.files().update(existingFileId, null, fileContent).execute()
            } else {
                // Create new file in user's Drive root
                val fileMetadata = File().apply {
                    name = BACKUP_FILE_NAME
                }
                driveService.files().create(fileMetadata, fileContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
            }
            
            Result.success("Backup creado exitosamente: ${resultFile.id}")
        } catch (e: Exception) {
            android.util.Log.e("DriveBackupService", "Error creating backup", e)
            Result.failure(e)
        }
    }

    /**
     * Restores data from Google Drive backup
     */
    suspend fun restoreBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileId = findBackupFile()
                ?: return@withContext Result.failure(Exception("No se encontró backup en Google Drive"))

            // Download file content
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val jsonContent = outputStream.toString("UTF-8")
            
            // Parse and import data
            val backupJson = JSONObject(jsonContent)
            importDatabaseFromJson(backupJson)
            
            Result.success("Datos restaurados exitosamente")
        } catch (e: Exception) {
            android.util.Log.e("DriveBackupService", "Error restoring backup", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a backup exists in Google Drive
     * @throws UserRecoverableAuthIOException if additional consent is needed
     */
    suspend fun hasBackup(): Boolean = withContext(Dispatchers.IO) {
        // Let UserRecoverableAuthIOException propagate
        findBackupFile() != null
    }

    /**
     * Get backup info (last modified date)
     * @throws UserRecoverableAuthIOException if additional consent is needed
     */
    suspend fun getBackupInfo(): BackupInfo? = withContext(Dispatchers.IO) {
        val fileId = findBackupFile() ?: return@withContext null
        val file = driveService.files().get(fileId)
            .setFields("id, name, modifiedTime, size")
            .execute()
        
        BackupInfo(
            fileId = file.id,
            fileName = file.name,
            modifiedTime = file.modifiedTime?.value ?: 0L,
            size = file.getSize()?.toLong() ?: 0L
        )
    }

    private fun findBackupFile(): String? {
        // Search in user's Drive (same as webapp) - not in appDataFolder
        val result = driveService.files().list()
            .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
            .setFields("files(id, name)")
            .execute()
        
        return result.files?.firstOrNull()?.id
    }

    private suspend fun exportDatabaseToJson(): JSONObject {
        val backup = JSONObject()
        backup.put("version", 1)
        backup.put("timestamp", System.currentTimeMillis())
        
        // Export Clientes
        val clientes = database.clienteDao().getAllSync()
        val clientesArray = JSONArray()
        clientes.forEach { cliente ->
            clientesArray.put(JSONObject().apply {
                put("id", cliente.id)
                put("nombre", cliente.nombre)
                put("telefono", cliente.telefono)
                put("email", cliente.email)
                put("notas", cliente.notas)
                put("fechaCreacion", cliente.fechaCreacion)
            })
        }
        backup.put("clientes", clientesArray)

        // Export Perros
        val perros = database.perroDao().getAllSync()
        val perrosArray = JSONArray()
        perros.forEach { perro ->
            perrosArray.put(JSONObject().apply {
                put("id", perro.id)
                put("nombre", perro.nombre)
                put("clienteId", perro.clienteId)
                put("raza", perro.raza)
                put("tamano", perro.tamano)
                put("longitudPelo", perro.longitudPelo)
                put("edad", perro.edad ?: 0)
                put("notas", perro.notas)
            })
        }
        backup.put("perros", perrosArray)

        // Export Servicios
        val servicios = database.servicioDao().getAllSync()
        val serviciosArray = JSONArray()
        servicios.forEach { servicio ->
            serviciosArray.put(JSONObject().apply {
                put("id", servicio.id)
                put("nombre", servicio.nombre)
                put("descripcion", servicio.descripcion)
                put("tipoPrecio", servicio.tipoPrecio)
                put("precioBase", servicio.precioBase)
                put("activo", servicio.activo)
            })
        }
        backup.put("servicios", serviciosArray)

        // Export Precios
        val precios = database.precioServicioDao().getAllSync()
        val preciosArray = JSONArray()
        precios.forEach { precio ->
            preciosArray.put(JSONObject().apply {
                put("id", precio.id)
                put("servicioId", precio.servicioId)
                put("tamano", precio.tamano)
                put("longitudPelo", precio.longitudPelo)
                put("precio", precio.precio)
            })
        }
        backup.put("precios", preciosArray)

        // Export Razas
        val razas = database.razaDao().getAllSync()
        val razasArray = JSONArray()
        razas.forEach { raza ->
            razasArray.put(JSONObject().apply {
                put("id", raza.id)
                put("nombre", raza.nombre)
            })
        }
        backup.put("razas", razasArray)

        // Export Citas
        val citas = database.citaDao().getAllSync()
        val citasArray = JSONArray()
        citas.forEach { cita ->
            citasArray.put(JSONObject().apply {
                put("id", cita.id)
                put("clienteId", cita.clienteId)
                put("perroId", cita.perroId)
                put("fecha", cita.fecha)
                put("hora", cita.hora)
                put("serviciosIds", cita.serviciosIds)
                put("precioTotal", cita.precioTotal)
                put("estado", cita.estado)
                put("notas", cita.notas)
                put("googleEventId", cita.googleEventId ?: "")
                put("fechaCreacion", cita.fechaCreacion)
            })
        }
        backup.put("citas", citasArray)

        return backup
    }

    private suspend fun importDatabaseFromJson(backup: JSONObject) {
        // Clear existing data (in reverse order of dependencies)
        database.citaDao().deleteAll()
        database.precioServicioDao().deleteAll()
        database.perroDao().deleteAll()
        database.clienteDao().deleteAll()
        database.servicioDao().deleteAll()
        database.razaDao().deleteAll()

        // Import Clientes
        val clientesArray = backup.optJSONArray("clientes") ?: JSONArray()
        for (i in 0 until clientesArray.length()) {
            val obj = clientesArray.getJSONObject(i)
            database.clienteDao().insert(
                Cliente(
                    id = obj.getLong("id"),
                    nombre = obj.getString("nombre"),
                    telefono = obj.getString("telefono"),
                    email = obj.optString("email", ""),
                    notas = obj.optString("notas", ""),
                    fechaCreacion = obj.optLong("fechaCreacion", System.currentTimeMillis())
                )
            )
        }

        // Import Razas
        val razasArray = backup.optJSONArray("razas") ?: JSONArray()
        for (i in 0 until razasArray.length()) {
            val obj = razasArray.getJSONObject(i)
            database.razaDao().insert(
                Raza(
                    id = obj.getLong("id"),
                    nombre = obj.getString("nombre")
                )
            )
        }

        // Import Perros
        val perrosArray = backup.optJSONArray("perros") ?: JSONArray()
        for (i in 0 until perrosArray.length()) {
            val obj = perrosArray.getJSONObject(i)
            database.perroDao().insert(
                Perro(
                    id = obj.getLong("id"),
                    nombre = obj.getString("nombre"),
                    clienteId = obj.getLong("clienteId"),
                    raza = obj.optString("raza", ""),
                    tamano = obj.getString("tamano"),
                    longitudPelo = obj.optString("longitudPelo", "medio"),
                    edad = obj.optInt("edad").takeIf { it > 0 },
                    notas = obj.optString("notas", "")
                )
            )
        }

        // Import Servicios
        val serviciosArray = backup.optJSONArray("servicios") ?: JSONArray()
        for (i in 0 until serviciosArray.length()) {
            val obj = serviciosArray.getJSONObject(i)
            database.servicioDao().insert(
                Servicio(
                    id = obj.getLong("id"),
                    nombre = obj.getString("nombre"),
                    descripcion = obj.optString("descripcion", ""),
                    tipoPrecio = obj.optString("tipoPrecio", "fijo"),
                    precioBase = obj.optDouble("precioBase", 0.0),
                    activo = obj.optBoolean("activo", true)
                )
            )
        }

        // Import Precios
        val preciosArray = backup.optJSONArray("precios") ?: JSONArray()
        for (i in 0 until preciosArray.length()) {
            val obj = preciosArray.getJSONObject(i)
            database.precioServicioDao().insert(
                PrecioServicio(
                    id = obj.getLong("id"),
                    servicioId = obj.getLong("servicioId"),
                    tamano = obj.getString("tamano"),
                    longitudPelo = obj.optString("longitudPelo", "medio"),
                    precio = obj.getDouble("precio")
                )
            )
        }

        // Import Citas
        val citasArray = backup.optJSONArray("citas") ?: JSONArray()
        for (i in 0 until citasArray.length()) {
            val obj = citasArray.getJSONObject(i)
            database.citaDao().insert(
                Cita(
                    id = obj.getLong("id"),
                    clienteId = obj.getLong("clienteId"),
                    perroId = obj.getLong("perroId"),
                    fecha = obj.getLong("fecha"),
                    hora = obj.getString("hora"),
                    serviciosIds = obj.optString("serviciosIds", "[]"),
                    precioTotal = obj.getDouble("precioTotal"),
                    estado = obj.getString("estado"),
                    notas = obj.optString("notas", ""),
                    googleEventId = obj.optString("googleEventId").takeIf { it.isNotEmpty() },
                    fechaCreacion = obj.optLong("fechaCreacion", System.currentTimeMillis())
                )
            )
        }
    }

    data class BackupInfo(
        val fileId: String,
        val fileName: String,
        val modifiedTime: Long,
        val size: Long
    )
}

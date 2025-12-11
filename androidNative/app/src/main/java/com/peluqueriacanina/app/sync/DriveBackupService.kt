package com.peluqueriacanina.app.sync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
            listOf(DriveScopes.DRIVE)  // Full drive scope to access webapp backups
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
        
        // Load all data first for cross-referencing
        val clientes = database.clienteDao().getAllSync()
        val perros = database.perroDao().getAllSync()
        val servicios = database.servicioDao().getAllSync()
        val precios = database.precioServicioDao().getAllSync()
        val razas = database.razaDao().getAllSync()
        val citas = database.citaDao().getAllSync()
        
        // Create lookup maps
        val clientesMap = clientes.associateBy { it.id }
        val perrosMap = perros.associateBy { it.id }
        val serviciosMap = servicios.associateBy { it.id }
        
        // Export Clientes with embedded perros (compatible with webapp)
        val clientesArray = JSONArray()
        clientes.forEach { cliente ->
            val clientePerros = perros.filter { it.clienteId == cliente.id }
            val perrosEmbeddedArray = JSONArray()
            
            clientePerros.forEach { perro ->
                perrosEmbeddedArray.put(JSONObject().apply {
                    put("id", perro.id)  // Incluir ID para compatibilidad
                    put("nombre", perro.nombre)
                    put("raza", perro.raza)
                    put("tamano", perro.tamano)
                    put("longitudPelo", perro.longitudPelo)
                    put("edad", perro.edad ?: 0)
                    put("foto", perro.foto ?: "")
                    put("notas", perro.notas)
                })
            }
            
            clientesArray.put(JSONObject().apply {
                put("id", cliente.id)
                put("nombre", cliente.nombre)
                put("telefono", cliente.telefono)
                put("email", cliente.email)
                put("notas", cliente.notas)
                put("fechaCreacion", cliente.fechaCreacion)
                put("perros", perrosEmbeddedArray)  // Webapp format
            })
        }
        backup.put("clientes", clientesArray)

        // Export Perros separately too (for APK format compatibility)
        val perrosArray = JSONArray()
        perros.forEach { perro ->
            perrosArray.put(JSONObject().apply {
                put("id", perro.id)
                put("nombre", perro.nombre)
                put("clienteId", perro.clienteId)
                put("clienteNombre", clientesMap[perro.clienteId]?.nombre ?: "")  // Para webapp
                put("raza", perro.raza)
                put("tamano", perro.tamano)
                put("longitudPelo", perro.longitudPelo)
                put("edad", perro.edad ?: 0)
                put("foto", perro.foto ?: "")
                put("notas", perro.notas)
            })
        }
        backup.put("perros", perrosArray)

        // Export Servicios with embedded combinaciones (for webapp)
        val serviciosArray = JSONArray()
        servicios.forEach { servicio ->
            val servicioPrecios = precios.filter { it.servicioId == servicio.id }
            val combinacionesArray = JSONArray()
            
            servicioPrecios.forEach { precio ->
                combinacionesArray.put(JSONObject().apply {
                    put("id", precio.id)
                    put("tamano", precio.tamano)
                    put("longitudPelo", precio.longitudPelo)
                    put("precio", precio.precio)
                })
            }
            
            serviciosArray.put(JSONObject().apply {
                put("id", servicio.id)
                put("nombre", servicio.nombre)
                put("descripcion", servicio.descripcion)
                put("tipoPrecio", servicio.tipoPrecio)
                put("tipo", servicio.tipoPrecio)  // Alias para webapp
                put("precioBase", servicio.precioBase)
                put("precio", servicio.precioBase)  // Alias para webapp
                put("activo", servicio.activo)
                if (combinacionesArray.length() > 0) {
                    put("combinaciones", combinacionesArray)  // Webapp format
                }
            })
        }
        backup.put("servicios", serviciosArray)

        // Export Precios separately (for APK format)
        val preciosArray = JSONArray()
        precios.forEach { precio ->
            preciosArray.put(JSONObject().apply {
                put("id", precio.id)
                put("servicioId", precio.servicioId)
                put("servicioNombre", serviciosMap[precio.servicioId]?.nombre ?: "")
                put("tamano", precio.tamano)
                put("longitudPelo", precio.longitudPelo)
                put("precio", precio.precio)
            })
        }
        backup.put("precios", preciosArray)

        // Export Razas
        val razasArray = JSONArray()
        razas.forEach { raza ->
            razasArray.put(JSONObject().apply {
                put("id", raza.id)
                put("nombre", raza.nombre)
            })
        }
        backup.put("razas", razasArray)

        // Export Citas with both IDs and names (compatible with both formats)
        val citasArray = JSONArray()
        citas.forEach { cita ->
            val cliente = clientesMap[cita.clienteId]
            val perro = perrosMap[cita.perroId]
            
            // Parse serviciosIds and get names
            val serviciosNombresArray = JSONArray()
            try {
                val idsArray = JSONArray(cita.serviciosIds)
                for (i in 0 until idsArray.length()) {
                    val servicioId = idsArray.optLong(i)
                    val servicioNombre = serviciosMap[servicioId]?.nombre
                    if (servicioNombre != null) {
                        serviciosNombresArray.put(servicioNombre)
                    }
                }
            } catch (e: Exception) { /* ignore */ }
            
            // Format fecha as string for webapp
            val fechaStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(cita.fecha))
            
            citasArray.put(JSONObject().apply {
                put("id", cita.id)
                // IDs for APK
                put("clienteId", cita.clienteId)
                put("perroId", cita.perroId)
                // Names for webapp
                put("clienteNombre", cliente?.nombre ?: "")
                put("perroNombre", perro?.nombre ?: "")
                // Fecha in both formats
                put("fecha", cita.fecha)
                put("fechaStr", fechaStr)  // String format for webapp
                put("hora", cita.hora)
                // Servicios in both formats
                put("serviciosIds", cita.serviciosIds)  // JSON array string for APK
                put("servicios", serviciosNombresArray)  // Array of names for webapp
                put("precioTotal", cita.precioTotal)
                put("precio", cita.precioTotal)  // Alias for webapp
                put("estado", cita.estado)
                put("completada", cita.estado == "completada")  // Boolean for webapp
                put("notas", cita.notas)
                put("googleEventId", cita.googleEventId ?: "")
                put("fechaCreacion", cita.fechaCreacion)
            })
        }
        backup.put("citas", citasArray)

        return backup
    }

    private suspend fun importDatabaseFromJson(backup: JSONObject) {
        // Webapp stores data inside "datos" object: {version, fecha, datos: {...}}
        val datos = if (backup.has("datos")) {
            backup.getJSONObject("datos")
        } else {
            backup
        }
        
        // Clear existing data (in reverse order of dependencies)
        database.citaDao().deleteAll()
        database.precioServicioDao().deleteAll()
        database.perroDao().deleteAll()
        database.clienteDao().deleteAll()
        database.servicioDao().deleteAll()
        database.razaDao().deleteAll()

        // Mapas para buscar por nombre (formato webapp)
        val clientesPorNombre = mutableMapOf<String, Long>() // nombre -> id
        val perrosPorNombreYCliente = mutableMapOf<String, Long>() // "clienteNombre|perroNombre" -> perroId
        val serviciosPorNombre = mutableMapOf<String, Long>() // nombre -> id

        // Import Razas first
        val razasArray = datos.optJSONArray("razas") ?: JSONArray()
        for (i in 0 until razasArray.length()) {
            val obj = razasArray.getJSONObject(i)
            val razaId = obj.optLong("id", 0)
            database.razaDao().insert(
                Raza(
                    id = if (razaId > 0) razaId else (i + 1).toLong(),
                    nombre = obj.getString("nombre")
                )
            )
        }

        // Import Clientes - webapp has 'perros' embedded inside each cliente
        val clientesArray = datos.optJSONArray("clientes") ?: JSONArray()
        var perroIdCounter = 1L
        
        for (i in 0 until clientesArray.length()) {
            val obj = clientesArray.getJSONObject(i)
            val clienteId = obj.optLong("id", System.currentTimeMillis() + i)
            val clienteNombre = obj.optString("nombre", "")
            
            // Guardar referencia por nombre
            if (clienteNombre.isNotEmpty()) {
                clientesPorNombre[clienteNombre] = clienteId
            }
            
            // Insert cliente con su ID
            database.clienteDao().insert(
                Cliente(
                    id = clienteId,
                    nombre = clienteNombre,
                    telefono = obj.optString("telefono", ""),
                    email = obj.optString("email", ""),
                    notas = obj.optString("notas", ""),
                    fechaCreacion = obj.optLong("fechaCreacion", System.currentTimeMillis())
                )
            )
            
            android.util.Log.d("DriveBackupService", "Importado cliente: $clienteNombre con ID $clienteId")
            
            // Webapp embeds perros inside cliente object - SIN ID propio
            val perrosEmbedded = obj.optJSONArray("perros")
            if (perrosEmbedded != null) {
                for (j in 0 until perrosEmbedded.length()) {
                    val perroObj = perrosEmbedded.getJSONObject(j)
                    val perroNombre = perroObj.optString("nombre", "")
                    
                    // Generar ID para el perro si no tiene
                    val perroId = perroObj.optLong("id", 0).takeIf { it > 0 } ?: perroIdCounter++
                    
                    // Guardar referencia por nombre combinado
                    if (perroNombre.isNotEmpty() && clienteNombre.isNotEmpty()) {
                        perrosPorNombreYCliente["$clienteNombre|$perroNombre"] = perroId
                    }
                    
                    database.perroDao().insert(
                        Perro(
                            id = perroId,
                            nombre = perroNombre,
                            clienteId = clienteId,
                            raza = perroObj.optString("raza", ""),
                            tamano = perroObj.optString("tamano", "mediano"),
                            longitudPelo = perroObj.optString("longitudPelo", "medio"),
                            edad = perroObj.optInt("edad").takeIf { it > 0 },
                            foto = compressBase64Image(perroObj.optString("foto").takeIf { it.isNotEmpty() }),
                            notas = perroObj.optString("notas", "")
                        )
                    )
                    
                    android.util.Log.d("DriveBackupService", "Importado perro: $perroNombre con ID $perroId para cliente $clienteNombre")
                }
            }
        }
        
        // Also check for separate perros array (APK format)
        val perrosArray = datos.optJSONArray("perros") ?: JSONArray()
        if (perrosArray.length() > 0) {
            for (i in 0 until perrosArray.length()) {
                val obj = perrosArray.getJSONObject(i)
                val perroId = obj.optLong("id", 0)
                val clienteId = obj.optLong("clienteId", 0)
                if (perroId <= 0 || clienteId <= 0) continue
                
                database.perroDao().insert(
                    Perro(
                        id = perroId,
                        nombre = obj.getString("nombre"),
                        clienteId = clienteId,
                        raza = obj.optString("raza", ""),
                        tamano = obj.optString("tamano", "mediano"),
                        longitudPelo = obj.optString("longitudPelo", "medio"),
                        edad = obj.optInt("edad").takeIf { it > 0 },
                        foto = compressBase64Image(obj.optString("foto").takeIf { it.isNotEmpty() }),
                        notas = obj.optString("notas", "")
                    )
                )
            }
        }

        // Import Servicios
        val serviciosArray = datos.optJSONArray("servicios") ?: JSONArray()
        
        for (i in 0 until serviciosArray.length()) {
            val obj = serviciosArray.getJSONObject(i)
            val servicioId = obj.optLong("id", (i + 1).toLong())
            val servicioNombre = obj.optString("nombre", "")
            
            // Guardar referencia por nombre
            if (servicioNombre.isNotEmpty()) {
                serviciosPorNombre[servicioNombre] = servicioId
            }
            
            val combinaciones = obj.optJSONArray("combinaciones")
            
            if (combinaciones != null && combinaciones.length() > 0) {
                val firstCombo = combinaciones.getJSONObject(0)
                val precioBase = firstCombo.optDouble("precio", 0.0)
                
                database.servicioDao().insert(
                    Servicio(
                        id = servicioId,
                        nombre = servicioNombre,
                        descripcion = obj.optString("descripcion", ""),
                        tipoPrecio = "variable",
                        precioBase = precioBase,
                        activo = obj.optBoolean("activo", true)
                    )
                )
                
                for (j in 0 until combinaciones.length()) {
                    val combo = combinaciones.getJSONObject(j)
                    database.precioServicioDao().insert(
                        PrecioServicio(
                            id = 0,
                            servicioId = servicioId,
                            tamano = combo.optString("tamano", "mediano"),
                            longitudPelo = combo.optString("longitudPelo", "medio"),
                            precio = combo.optDouble("precio", 0.0)
                        )
                    )
                }
            } else {
                // Formato webapp simple o APK
                database.servicioDao().insert(
                    Servicio(
                        id = servicioId,
                        nombre = servicioNombre,
                        descripcion = obj.optString("descripcion", ""),
                        tipoPrecio = obj.optString("tipoPrecio", obj.optString("tipo", "fijo")),
                        precioBase = obj.optDouble("precioBase", obj.optDouble("precio", 0.0)),
                        activo = obj.optBoolean("activo", true)
                    )
                )
            }
        }

        // Import Precios from separate array (APK format only)
        val preciosArray = datos.optJSONArray("precios") ?: JSONArray()
        if (preciosArray.length() > 0) {
            for (i in 0 until preciosArray.length()) {
                val obj = preciosArray.getJSONObject(i)
                database.precioServicioDao().insert(
                    PrecioServicio(
                        id = obj.optLong("id", 0),
                        servicioId = obj.getLong("servicioId"),
                        tamano = obj.getString("tamano"),
                        longitudPelo = obj.optString("longitudPelo", "medio"),
                        precio = obj.getDouble("precio")
                    )
                )
            }
        }

        // Import Citas - webapp usa nombres en vez de IDs!
        val citasArray = datos.optJSONArray("citas") ?: JSONArray()
        
        for (i in 0 until citasArray.length()) {
            val obj = citasArray.getJSONObject(i)
            
            val citaId = obj.optLong("id", System.currentTimeMillis() + i)
            
            // Webapp usa clienteNombre y perroNombre en vez de IDs
            var clienteId = obj.optLong("clienteId", 0)
            var perroId = obj.optLong("perroId", 0)
            
            // Si no hay IDs, buscar por nombre
            if (clienteId <= 0) {
                val clienteNombre = obj.optString("clienteNombre", "")
                clienteId = clientesPorNombre[clienteNombre] ?: 0
                android.util.Log.d("DriveBackupService", "Buscando cliente '$clienteNombre' -> ID $clienteId")
            }
            
            if (perroId <= 0) {
                val clienteNombre = obj.optString("clienteNombre", "")
                val perroNombre = obj.optString("perroNombre", "")
                perroId = perrosPorNombreYCliente["$clienteNombre|$perroNombre"] ?: 0
                android.util.Log.d("DriveBackupService", "Buscando perro '$clienteNombre|$perroNombre' -> ID $perroId")
            }
            
            // Fecha
            val fechaValue = obj.opt("fecha")
            val fechaLong = when (fechaValue) {
                is Long -> fechaValue
                is Int -> fechaValue.toLong()
                is String -> {
                    try {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .parse(fechaValue)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                }
                else -> System.currentTimeMillis()
            }
            
            // Servicios - webapp usa array de nombres de servicios
            val serviciosIds = when {
                obj.has("serviciosIds") -> obj.optString("serviciosIds", "[]")
                obj.has("servicios") -> {
                    val servs = obj.optJSONArray("servicios")
                    if (servs != null) {
                        val ids = mutableListOf<Long>()
                        for (j in 0 until servs.length()) {
                            val item = servs.opt(j)
                            when (item) {
                                is Long -> ids.add(item)
                                is Int -> ids.add(item.toLong())
                                is String -> {
                                    // Buscar servicio por nombre
                                    val servId = serviciosPorNombre[item]
                                    if (servId != null) ids.add(servId)
                                }
                            }
                        }
                        JSONArray(ids).toString()
                    } else "[]"
                }
                else -> "[]"
            }
            
            // Estado
            val completada = obj.optBoolean("completada", false)
            val estado = if (completada) "completada" else obj.optString("estado", "pendiente")
            
            database.citaDao().insert(
                Cita(
                    id = citaId,
                    clienteId = clienteId,
                    perroId = perroId,
                    fecha = fechaLong,
                    hora = obj.optString("hora", "10:00"),
                    serviciosIds = serviciosIds,
                    precioTotal = obj.optDouble("precioTotal", obj.optDouble("precio", 0.0)),
                    estado = estado,
                    notas = obj.optString("notas", ""),
                    googleEventId = obj.optString("googleEventId").takeIf { it.isNotEmpty() },
                    fechaCreacion = obj.optLong("fechaCreacion", System.currentTimeMillis())
                )
            )
            
            android.util.Log.d("DriveBackupService", "Importada cita ID $citaId: clienteId=$clienteId, perroId=$perroId")
        }
        
        android.util.Log.d("DriveBackupService", "Import completado: ${clientesArray.length()} clientes, ${citasArray.length()} citas")
    }

    /**
     * Comprime una imagen Base64 para que quepa en SQLite CursorWindow (max ~1MB)
     * Redimensiona y comprime con JPEG quality
     */
    private fun compressBase64Image(base64String: String?, maxSizeKB: Int = 500): String? {
        if (base64String.isNullOrEmpty()) return null
        
        try {
            // Remove data URI prefix if present
            val pureBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            
            // If already small enough, return as-is
            if (pureBase64.length < maxSizeKB * 1024) {
                return base64String
            }
            
            // Decode Base64 to bitmap
            val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return null
            
            // Calculate scale factor to reduce size
            val maxDimension = 800 // Max width/height in pixels
            val scale = minOf(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height,
                1f // Don't upscale
            )
            
            if (scale < 1f) {
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            }
            
            // Compress to JPEG with decreasing quality until small enough
            var quality = 80
            var outputStream: ByteArrayOutputStream
            var resultBytes: ByteArray
            
            do {
                outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                resultBytes = outputStream.toByteArray()
                quality -= 10
            } while (resultBytes.size > maxSizeKB * 1024 && quality > 10)
            
            val resultBase64 = Base64.encodeToString(resultBytes, Base64.NO_WRAP)
            android.util.Log.d("DriveBackupService", "Compressed image from ${pureBase64.length} to ${resultBase64.length} chars")
            
            return "data:image/jpeg;base64,$resultBase64"
        } catch (e: Exception) {
            android.util.Log.e("DriveBackupService", "Error compressing image", e)
            return null // Return null instead of huge broken image
        }
    }

    data class BackupInfo(
        val fileId: String,
        val fileName: String,
        val modifiedTime: Long,
        val size: Long
    )
}

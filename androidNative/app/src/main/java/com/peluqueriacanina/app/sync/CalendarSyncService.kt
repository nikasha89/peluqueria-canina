package com.peluqueriacanina.app.sync

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.AppDatabase
import com.peluqueriacanina.app.data.Cita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CalendarSyncService(
    private val context: Context,
    private val account: GoogleSignInAccount
) {
    companion object {
        private const val CALENDAR_ID = "primary"
    }

    private val calendarService: Calendar by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_EVENTS)
        )
        credential.selectedAccount = account.account

        Calendar.Builder(
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
     * Sync all appointments to Google Calendar
     * @throws UserRecoverableAuthIOException if additional consent is needed
     */
    suspend fun syncAllCitas(): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val citas = database.citaDao().getAllSync()
            var created = 0
            var updated = 0
            var errors = 0

            for (cita in citas) {
                try {
                    // Get client and dog info for the event title
                    val perro = database.perroDao().getPerroById(cita.perroId)
                    val cliente = perro?.let { database.clienteDao().getClienteById(it.clienteId) }
                    
                    val eventTitle = buildEventTitle(perro?.nombre, cliente?.nombre)
                    val eventDescription = buildEventDescription(cita, perro?.nombre, cliente?.telefono)

                    if (cita.googleEventId != null) {
                        // Update existing event
                        val success = updateCalendarEvent(cita, eventTitle, eventDescription)
                        if (success) updated++ else errors++
                    } else {
                        // Create new event
                        val eventId = createCalendarEvent(cita, eventTitle, eventDescription)
                        if (eventId != null) {
                            // Save the Google Event ID back to the database
                            database.citaDao().update(cita.copy(googleEventId = eventId))
                            created++
                        } else {
                            errors++
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CalendarSyncService", "Error syncing cita ${cita.id}", e)
                    errors++
                }
            }

            Result.success(SyncResult(created, updated, errors))
        } catch (e: Exception) {
            android.util.Log.e("CalendarSyncService", "Error syncing calendar", e)
            Result.failure(e)
        }
    }

    /**
     * Create a single calendar event for an appointment
     */
    suspend fun createEventForCita(cita: Cita): Result<String> = withContext(Dispatchers.IO) {
        try {
            val perro = database.perroDao().getPerroById(cita.perroId)
            val cliente = perro?.let { database.clienteDao().getClienteById(it.clienteId) }
            
            val eventTitle = buildEventTitle(perro?.nombre, cliente?.nombre)
            val eventDescription = buildEventDescription(cita, perro?.nombre, cliente?.telefono)

            val eventId = createCalendarEvent(cita, eventTitle, eventDescription)
            if (eventId != null) {
                // Update cita with the event ID
                database.citaDao().update(cita.copy(googleEventId = eventId))
                Result.success(eventId)
            } else {
                Result.failure(Exception("No se pudo crear el evento"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarSyncService", "Error creating event", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a calendar event
     */
    suspend fun deleteEventForCita(cita: Cita): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (cita.googleEventId != null) {
                calendarService.events().delete(CALENDAR_ID, cita.googleEventId).execute()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CalendarSyncService", "Error deleting event", e)
            Result.failure(e)
        }
    }

    private fun createCalendarEvent(cita: Cita, title: String, description: String): String? {
        val event = Event().apply {
            summary = title
            this.description = description
            
            // Set start time
            val startDateTime = createEventDateTime(cita.fecha, cita.hora)
            start = EventDateTime().apply {
                dateTime = startDateTime
                timeZone = TimeZone.getDefault().id
            }
            
            // Set end time (assume 1 hour duration)
            val endDateTime = DateTime(startDateTime.value + 3600000) // +1 hour
            end = EventDateTime().apply {
                dateTime = endDateTime
                timeZone = TimeZone.getDefault().id
            }
        }

        val createdEvent = calendarService.events().insert(CALENDAR_ID, event).execute()
        return createdEvent.id
    }

    private fun updateCalendarEvent(cita: Cita, title: String, description: String): Boolean {
        return try {
            val existingEvent = calendarService.events().get(CALENDAR_ID, cita.googleEventId).execute()
            
            existingEvent.summary = title
            existingEvent.description = description
            
            // Update times
            val startDateTime = createEventDateTime(cita.fecha, cita.hora)
            existingEvent.start = EventDateTime().apply {
                dateTime = startDateTime
                timeZone = TimeZone.getDefault().id
            }
            
            val endDateTime = DateTime(startDateTime.value + 3600000)
            existingEvent.end = EventDateTime().apply {
                dateTime = endDateTime
                timeZone = TimeZone.getDefault().id
            }

            calendarService.events().update(CALENDAR_ID, cita.googleEventId, existingEvent).execute()
            true
        } catch (e: Exception) {
            android.util.Log.e("CalendarSyncService", "Error updating event", e)
            false
        }
    }

    private fun createEventDateTime(fecha: Long, hora: String): DateTime {
        // Parse hour from "HH:mm" format
        val parts = hora.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
        
        // Create calendar with the date and time
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = fecha
            set(java.util.Calendar.HOUR_OF_DAY, hours)
            set(java.util.Calendar.MINUTE, minutes)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        return DateTime(calendar.timeInMillis)
    }

    private fun buildEventTitle(perroNombre: String?, clienteNombre: String?): String {
        return when {
            perroNombre != null && clienteNombre != null -> "🐕 $perroNombre ($clienteNombre)"
            perroNombre != null -> "🐕 $perroNombre"
            else -> "🐕 Cita Peluquería"
        }
    }

    private fun buildEventDescription(cita: Cita, perroNombre: String?, clienteTelefono: String?): String {
        val sb = StringBuilder()
        sb.appendLine("📍 Peluquería Canina")
        
        if (perroNombre != null) {
            sb.appendLine("🐶 Mascota: $perroNombre")
        }
        
        if (clienteTelefono != null) {
            sb.appendLine("📞 Teléfono: $clienteTelefono")
        }
        
        sb.appendLine("💰 Precio: ${String.format("%.2f", cita.precioTotal)}€")
        
        if (cita.notas.isNotEmpty()) {
            sb.appendLine("📝 Notas: ${cita.notas}")
        }
        
        return sb.toString()
    }

    data class SyncResult(
        val created: Int,
        val updated: Int,
        val errors: Int
    ) {
        val total: Int get() = created + updated
        val hasErrors: Boolean get() = errors > 0
    }
}

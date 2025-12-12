package com.peluqueriacanina.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import com.peluqueriacanina.app.sync.AutoBackupManager
import com.peluqueriacanina.app.sync.CalendarSyncService
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Calendar

class CitaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as PeluqueriaApp
    private val citaDao = app.database.citaDao()
    private val clienteDao = app.database.clienteDao()
    private val perroDao = app.database.perroDao()
    private val servicioDao = app.database.servicioDao()
    
    val allCitas: LiveData<List<Cita>> = citaDao.getAllCitas()
    val allServicios: LiveData<List<Servicio>> = servicioDao.getAllServicios()
    
    private val _citasHoy = MutableLiveData<List<Cita>>()
    val citasHoy: LiveData<List<Cita>> = _citasHoy
    
    private val _citasSemana = MutableLiveData<List<Cita>>()
    val citasSemana: LiveData<List<Cita>> = _citasSemana
    
    // Citas con detalles cargados
    private val _citasConDetalles = MutableLiveData<List<CitaConDetalles>>()
    val citasConDetalles: LiveData<List<CitaConDetalles>> = _citasConDetalles
    
    companion object {
        private const val TAG = "CitaViewModel"
    }
    
    init {
        loadCitasHoy()
        loadCitasSemana()
    }
    
    private fun notifyDataChanged() {
        AutoBackupManager.notifyDataChanged(app)
    }
    
    /**
     * Obtiene el servicio de Calendar si el usuario está logueado
     */
    private fun getCalendarService(): CalendarSyncService? {
        val account = GoogleSignIn.getLastSignedInAccount(app)
        return if (account != null) {
            CalendarSyncService(app, account)
        } else {
            null
        }
    }
    
    fun loadCitasConDetalles(citas: List<Cita>) {
        viewModelScope.launch {
            val citasEnriquecidas = citas.map { cita ->
                val cliente = clienteDao.getClienteById(cita.clienteId)
                val perro = perroDao.getPerroById(cita.perroId)
                
                val serviciosNombres = mutableListOf<String>()
                try {
                    val serviciosIds = JSONArray(cita.serviciosIds)
                    for (i in 0 until serviciosIds.length()) {
                        val servicioId = serviciosIds.getLong(i)
                        val servicio = servicioDao.getServicioById(servicioId)
                        servicio?.let { serviciosNombres.add(it.nombre) }
                    }
                } catch (e: Exception) { }
                
                CitaConDetalles(
                    cita = cita,
                    clienteNombre = cliente?.nombre ?: "Desconocido",
                    clienteTelefono = cliente?.telefono ?: "",
                    perroNombre = perro?.nombre ?: "Desconocido",
                    perroRaza = perro?.raza ?: "",
                    serviciosNombres = serviciosNombres
                )
            }
            _citasConDetalles.postValue(citasEnriquecidas)
        }
    }
    
    fun loadCitasHoy() {
        val today = getStartOfDay(System.currentTimeMillis())
        val tomorrow = today + 24 * 60 * 60 * 1000
        citaDao.getCitasByDateRange(today, tomorrow - 1).observeForever { citas ->
            _citasHoy.value = citas
        }
    }
    
    fun loadCitasSemana() {
        val today = getStartOfDay(System.currentTimeMillis())
        val weekEnd = today + 7 * 24 * 60 * 60 * 1000
        citaDao.getCitasByDateRange(today, weekEnd).observeForever { citas ->
            _citasSemana.value = citas
        }
    }
    
    fun getCitasByDate(fecha: Long): LiveData<List<Cita>> {
        return citaDao.getCitasByDate(getStartOfDay(fecha))
    }
    
    fun insertCita(cita: Cita, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = citaDao.insert(cita)
            loadCitasHoy()
            loadCitasSemana()
            notifyDataChanged()
            
            // Sincronizar con Google Calendar
            val calendarService = getCalendarService()
            if (calendarService != null) {
                val insertedCita = cita.copy(id = id)
                calendarService.createEventForCita(insertedCita)
                    .onSuccess { eventId ->
                        Log.d(TAG, "Evento de calendario creado: $eventId")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Error al crear evento de calendario", e)
                    }
            }
            
            onResult(id)
        }
    }
    
    fun updateCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.update(cita)
            loadCitasHoy()
            loadCitasSemana()
            notifyDataChanged()
            
            // Sincronizar con Google Calendar
            val calendarService = getCalendarService()
            if (calendarService != null) {
                if (cita.googleEventId != null) {
                    // Eliminar evento anterior y crear uno nuevo
                    calendarService.deleteEventForCita(cita)
                        .onFailure { e ->
                            Log.e(TAG, "Error al eliminar evento anterior", e)
                        }
                }
                calendarService.createEventForCita(cita)
                    .onSuccess { eventId ->
                        Log.d(TAG, "Evento de calendario actualizado: $eventId")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Error al actualizar evento de calendario", e)
                    }
            }
        }
    }
    
    fun deleteCita(cita: Cita) {
        viewModelScope.launch {
            // Eliminar de Google Calendar primero
            val calendarService = getCalendarService()
            if (calendarService != null && cita.googleEventId != null) {
                calendarService.deleteEventForCita(cita)
                    .onSuccess {
                        Log.d(TAG, "Evento de calendario eliminado")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Error al eliminar evento de calendario", e)
                    }
            }
            
            citaDao.delete(cita)
            loadCitasHoy()
            loadCitasSemana()
            notifyDataChanged()
        }
    }
    
    fun completarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.update(cita.copy(estado = "completada"))
            loadCitasHoy()
            loadCitasSemana()
            notifyDataChanged()
        }
    }
    
    fun cancelarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.update(cita.copy(estado = "cancelada"))
            loadCitasHoy()
            loadCitasSemana()
            notifyDataChanged()
        }
    }
    
    suspend fun getClienteById(id: Long): Cliente? = clienteDao.getClienteById(id)
    suspend fun getPerroById(id: Long): Perro? = perroDao.getPerroById(id)
    suspend fun getServicioById(id: Long): Servicio? = servicioDao.getServicioById(id)
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

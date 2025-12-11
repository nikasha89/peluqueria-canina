package com.peluqueriacanina.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import kotlinx.coroutines.launch
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
    
    init {
        loadCitasHoy()
        loadCitasSemana()
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
            onResult(id)
        }
    }
    
    fun updateCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.update(cita)
            loadCitasHoy()
            loadCitasSemana()
        }
    }
    
    fun deleteCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.delete(cita)
            loadCitasHoy()
            loadCitasSemana()
        }
    }
    
    fun completarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.update(cita.copy(estado = "completada"))
            loadCitasHoy()
            loadCitasSemana()
        }
    }
    
    fun cancelarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.update(cita.copy(estado = "cancelada"))
            loadCitasHoy()
            loadCitasSemana()
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

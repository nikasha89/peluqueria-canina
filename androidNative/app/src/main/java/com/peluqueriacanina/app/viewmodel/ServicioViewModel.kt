package com.peluqueriacanina.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import kotlinx.coroutines.launch

class ServicioViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as PeluqueriaApp
    private val servicioDao = app.database.servicioDao()
    private val precioDao = app.database.precioServicioDao()
    private val razaDao = app.database.razaDao()
    
    val allServicios: LiveData<List<Servicio>> = servicioDao.getAllServicios()
    val allRazas: LiveData<List<Raza>> = razaDao.getAllRazas()
    
    fun getPreciosByServicio(servicioId: Long): LiveData<List<PrecioServicio>> {
        return precioDao.getPreciosByServicio(servicioId)
    }
    
    fun insertServicio(servicio: Servicio, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = servicioDao.insert(servicio)
            onResult(id)
        }
    }
    
    fun updateServicio(servicio: Servicio) {
        viewModelScope.launch {
            servicioDao.update(servicio)
        }
    }
    
    fun deleteServicio(servicio: Servicio) {
        viewModelScope.launch {
            precioDao.deleteByServicio(servicio.id)
            servicioDao.delete(servicio)
        }
    }
    
    fun insertPrecio(precio: PrecioServicio, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = precioDao.insert(precio)
            onResult(id)
        }
    }
    
    fun updatePrecio(precio: PrecioServicio) {
        viewModelScope.launch {
            precioDao.update(precio)
        }
    }
    
    fun deletePrecio(precio: PrecioServicio) {
        viewModelScope.launch {
            precioDao.delete(precio)
        }
    }
    
    suspend fun calcularPrecio(servicioId: Long, tamano: String, longitudPelo: String): Double {
        val servicio = servicioDao.getServicioById(servicioId) ?: return 0.0
        
        return if (servicio.tipoPrecio == "fijo") {
            servicio.precioBase
        } else {
            val precio = precioDao.getPrecio(servicioId, tamano, longitudPelo)
            precio?.precio ?: servicio.precioBase
        }
    }
    
    // Razas
    fun insertRaza(raza: Raza, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = razaDao.insert(raza)
            onResult(id)
        }
    }
    
    fun updateRaza(raza: Raza) {
        viewModelScope.launch {
            razaDao.update(raza)
        }
    }
    
    fun deleteRaza(raza: Raza) {
        viewModelScope.launch {
            razaDao.delete(raza)
        }
    }
}

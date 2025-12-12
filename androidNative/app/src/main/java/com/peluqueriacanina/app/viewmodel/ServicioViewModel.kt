package com.peluqueriacanina.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import com.peluqueriacanina.app.sync.AutoBackupManager
import kotlinx.coroutines.launch

class ServicioViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as PeluqueriaApp
    private val servicioDao = app.database.servicioDao()
    private val precioDao = app.database.precioServicioDao()
    private val razaDao = app.database.razaDao()
    private val tamanoDao = app.database.tamanoDao()
    private val longitudPeloDao = app.database.longitudPeloDao()
    
    val allServicios: LiveData<List<Servicio>> = servicioDao.getAllServicios()
    val allRazas: LiveData<List<Raza>> = razaDao.getAllRazas()
    val allTamanos: LiveData<List<Tamano>> = tamanoDao.getAllTamanos()
    val allLongitudesPelo: LiveData<List<LongitudPelo>> = longitudPeloDao.getAllLongitudesPelo()
    val allPrecios: LiveData<List<PrecioServicio>> = precioDao.getAll()
    
    private fun notifyDataChanged() {
        AutoBackupManager.notifyDataChanged(app)
    }
    
    fun getPreciosByServicio(servicioId: Long): LiveData<List<PrecioServicio>> {
        return precioDao.getPreciosByServicio(servicioId)
    }
    
    fun insertServicio(servicio: Servicio, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = servicioDao.insert(servicio)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updateServicio(servicio: Servicio) {
        viewModelScope.launch {
            servicioDao.update(servicio)
            notifyDataChanged()
        }
    }
    
    fun deleteServicio(servicio: Servicio) {
        viewModelScope.launch {
            precioDao.deleteByServicio(servicio.id)
            servicioDao.delete(servicio)
            notifyDataChanged()
        }
    }
    
    fun insertPrecio(precio: PrecioServicio, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = precioDao.insert(precio)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updatePrecio(precio: PrecioServicio) {
        viewModelScope.launch {
            precioDao.update(precio)
            notifyDataChanged()
        }
    }
    
    fun deletePrecio(precio: PrecioServicio) {
        viewModelScope.launch {
            precioDao.delete(precio)
            notifyDataChanged()
        }
    }
    
    suspend fun getPreciosForServicio(servicioId: Long): List<PrecioServicio> {
        return precioDao.getPreciosByServicioSync(servicioId)
    }
    
    fun getPreciosForServicioLive(servicioId: Long): LiveData<List<PrecioServicio>> {
        return precioDao.getPreciosByServicio(servicioId)
    }
    
    suspend fun insertPrecioServicio(precio: PrecioServicio): Long {
        val id = precioDao.insert(precio)
        notifyDataChanged()
        return id
    }
    
    suspend fun updatePrecioServicio(precio: PrecioServicio) {
        precioDao.update(precio)
        notifyDataChanged()
    }
    
    suspend fun deletePrecioServicio(precio: PrecioServicio) {
        precioDao.delete(precio)
        notifyDataChanged()
    }
    
    fun insertServicioConPrecios(servicio: Servicio, precios: List<PrecioServicio>) {
        viewModelScope.launch {
            val servicioId = servicioDao.insert(servicio)
            precios.forEach { precio ->
                precioDao.insert(precio.copy(servicioId = servicioId))
            }
            notifyDataChanged()
        }
    }
    
    fun updateServicioConPrecios(servicio: Servicio, precios: List<PrecioServicio>) {
        viewModelScope.launch {
            servicioDao.update(servicio)
            // Delete old precios and insert new ones
            precioDao.deleteByServicio(servicio.id)
            precios.forEach { precio ->
                precioDao.insert(precio.copy(servicioId = servicio.id))
            }
            notifyDataChanged()
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
    
    /**
     * Calcula el precio de un servicio basándose en la raza, tamaño y longitud del pelo del perro.
     * Busca coincidencia exacta con raza + tamaño + pelo.
     * Si no encuentra la combinación exacta, devuelve 0.0
     */
    suspend fun calcularPrecioParaPerro(servicioId: Long, raza: String?, tamano: String, longitudPelo: String): Double {
        val servicio = servicioDao.getServicioById(servicioId) ?: return 0.0
        
        return if (servicio.tipoPrecio == "fijo") {
            servicio.precioBase
        } else {
            // Buscar coincidencia exacta con raza
            if (!raza.isNullOrBlank()) {
                val precioConRaza = precioDao.getPrecioConRaza(servicioId, raza, tamano, longitudPelo)
                if (precioConRaza != null) {
                    return precioConRaza.precio
                }
            }
            
            // No hay combinación exacta, devolver 0.0
            0.0
        }
    }
    
    // Razas
    fun insertRaza(raza: Raza, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = razaDao.insert(raza)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updateRaza(raza: Raza) {
        viewModelScope.launch {
            razaDao.update(raza)
            notifyDataChanged()
        }
    }
    
    fun deleteRaza(raza: Raza) {
        viewModelScope.launch {
            razaDao.delete(raza)
            notifyDataChanged()
        }
    }
    
    // Tamaños
    fun insertTamano(tamano: Tamano, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = tamanoDao.insert(tamano)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updateTamano(tamano: Tamano) {
        viewModelScope.launch {
            tamanoDao.update(tamano)
            notifyDataChanged()
        }
    }
    
    fun deleteTamano(tamano: Tamano) {
        viewModelScope.launch {
            tamanoDao.delete(tamano)
            notifyDataChanged()
        }
    }
    
    // Longitudes de pelo
    fun insertLongitudPelo(longitudPelo: LongitudPelo, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = longitudPeloDao.insert(longitudPelo)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updateLongitudPelo(longitudPelo: LongitudPelo) {
        viewModelScope.launch {
            longitudPeloDao.update(longitudPelo)
            notifyDataChanged()
        }
    }
    
    fun deleteLongitudPelo(longitudPelo: LongitudPelo) {
        viewModelScope.launch {
            longitudPeloDao.delete(longitudPelo)
            notifyDataChanged()
        }
    }
}

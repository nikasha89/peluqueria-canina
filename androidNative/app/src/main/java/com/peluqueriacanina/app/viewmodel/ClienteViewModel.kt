package com.peluqueriacanina.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import com.peluqueriacanina.app.sync.AutoBackupManager
import kotlinx.coroutines.launch

class ClienteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as PeluqueriaApp
    private val clienteDao = app.database.clienteDao()
    private val perroDao = app.database.perroDao()
    private val razaDao = app.database.razaDao()
    
    val allClientes: LiveData<List<Cliente>> = clienteDao.getAllClientes()
    val allRazas: LiveData<List<Raza>> = razaDao.getAllRazas()
    
    private val _selectedCliente = MutableLiveData<Cliente?>()
    val selectedCliente: LiveData<Cliente?> = _selectedCliente
    
    // Use switchMap to automatically observe perros when cliente changes
    val perrosDelCliente: LiveData<List<Perro>> = _selectedCliente.switchMap { cliente ->
        if (cliente != null) {
            perroDao.getPerrosByCliente(cliente.id)
        } else {
            MutableLiveData(emptyList())
        }
    }
    
    private fun notifyDataChanged() {
        AutoBackupManager.notifyDataChanged(app)
    }
    
    fun selectCliente(cliente: Cliente?) {
        _selectedCliente.value = cliente
    }
    
    fun getPerrosForCliente(clienteId: Long): LiveData<List<Perro>> {
        return perroDao.getPerrosByCliente(clienteId)
    }
    
    fun searchClientes(query: String): LiveData<List<Cliente>> {
        return clienteDao.searchClientes(query)
    }
    
    fun insertCliente(cliente: Cliente, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = clienteDao.insert(cliente)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updateCliente(cliente: Cliente) {
        viewModelScope.launch {
            clienteDao.update(cliente)
            notifyDataChanged()
        }
    }
    
    fun deleteCliente(cliente: Cliente) {
        viewModelScope.launch {
            perroDao.deleteByCliente(cliente.id)
            clienteDao.delete(cliente)
            notifyDataChanged()
        }
    }
    
    fun insertPerro(perro: Perro, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = perroDao.insert(perro)
            notifyDataChanged()
            onResult(id)
        }
    }
    
    fun updatePerro(perro: Perro) {
        viewModelScope.launch {
            perroDao.update(perro)
            notifyDataChanged()
        }
    }
    
    fun deletePerro(perro: Perro) {
        viewModelScope.launch {
            perroDao.delete(perro)
            notifyDataChanged()
        }
    }
}

package com.peluqueriacanina.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.peluqueriacanina.app.PeluqueriaApp
import com.peluqueriacanina.app.data.*
import kotlinx.coroutines.launch

class ClienteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val app = application as PeluqueriaApp
    private val clienteDao = app.database.clienteDao()
    private val perroDao = app.database.perroDao()
    
    val allClientes: LiveData<List<Cliente>> = clienteDao.getAllClientes()
    
    private val _selectedCliente = MutableLiveData<Cliente?>()
    val selectedCliente: LiveData<Cliente?> = _selectedCliente
    
    private val _perrosDelCliente = MutableLiveData<List<Perro>>()
    val perrosDelCliente: LiveData<List<Perro>> = _perrosDelCliente
    
    fun selectCliente(cliente: Cliente?) {
        _selectedCliente.value = cliente
        cliente?.let { loadPerrosDelCliente(it.id) }
    }
    
    private fun loadPerrosDelCliente(clienteId: Long) {
        perroDao.getPerrosByCliente(clienteId).observeForever { perros ->
            _perrosDelCliente.value = perros
        }
    }
    
    fun searchClientes(query: String): LiveData<List<Cliente>> {
        return clienteDao.searchClientes(query)
    }
    
    fun insertCliente(cliente: Cliente, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = clienteDao.insert(cliente)
            onResult(id)
        }
    }
    
    fun updateCliente(cliente: Cliente) {
        viewModelScope.launch {
            clienteDao.update(cliente)
        }
    }
    
    fun deleteCliente(cliente: Cliente) {
        viewModelScope.launch {
            perroDao.deleteByCliente(cliente.id)
            clienteDao.delete(cliente)
        }
    }
    
    fun insertPerro(perro: Perro, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = perroDao.insert(perro)
            loadPerrosDelCliente(perro.clienteId)
            onResult(id)
        }
    }
    
    fun updatePerro(perro: Perro) {
        viewModelScope.launch {
            perroDao.update(perro)
            loadPerrosDelCliente(perro.clienteId)
        }
    }
    
    fun deletePerro(perro: Perro) {
        viewModelScope.launch {
            perroDao.delete(perro)
            loadPerrosDelCliente(perro.clienteId)
        }
    }
}

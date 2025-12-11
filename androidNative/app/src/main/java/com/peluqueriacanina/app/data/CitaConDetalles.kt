package com.peluqueriacanina.app.data

/**
 * Clase que contiene una cita con los datos de cliente, perro y servicios cargados
 */
data class CitaConDetalles(
    val cita: Cita,
    val clienteNombre: String,
    val clienteTelefono: String,
    val perroNombre: String,
    val perroRaza: String,
    val serviciosNombres: List<String>
)

package com.peluqueriacanina.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val email: String = "",
    val notas: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "perros")
data class Perro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: Long,
    val nombre: String,
    val raza: String,
    val tamano: String, // mini, pequeno, mediano, grande, gigante
    val longitudPelo: String, // corto, medio, largo
    val edad: Int? = null,
    val notas: String = ""
)

@Entity(tableName = "servicios")
data class Servicio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    val tipoPrecio: String = "fijo", // fijo, porTamano
    val precioBase: Double = 0.0,
    val activo: Boolean = true
)

@Entity(tableName = "precios_servicio")
data class PrecioServicio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val servicioId: Long,
    val tamano: String,
    val longitudPelo: String,
    val precio: Double
)

@Entity(tableName = "citas")
data class Cita(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: Long,
    val perroId: Long,
    val fecha: Long, // timestamp
    val hora: String, // HH:mm
    val serviciosIds: String, // JSON array of service IDs
    val precioTotal: Double,
    val estado: String = "pendiente", // pendiente, completada, cancelada
    val notas: String = "",
    val googleEventId: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "razas")
data class Raza(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String
)

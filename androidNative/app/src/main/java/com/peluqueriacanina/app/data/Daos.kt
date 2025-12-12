package com.peluqueriacanina.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    fun getAllClientes(): LiveData<List<Cliente>>
    
    @Query("SELECT * FROM clientes ORDER BY nombre ASC")
    suspend fun getAllSync(): List<Cliente>
    
    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getClienteById(id: Long): Cliente?
    
    @Query("SELECT * FROM clientes WHERE nombre LIKE '%' || :query || '%' OR telefono LIKE '%' || :query || '%'")
    fun searchClientes(query: String): LiveData<List<Cliente>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cliente: Cliente): Long
    
    @Update
    suspend fun update(cliente: Cliente)
    
    @Delete
    suspend fun delete(cliente: Cliente)
    
    @Query("DELETE FROM clientes")
    suspend fun deleteAll()
}

@Dao
interface PerroDao {
    @Query("SELECT * FROM perros WHERE clienteId = :clienteId ORDER BY nombre ASC")
    fun getPerrosByCliente(clienteId: Long): LiveData<List<Perro>>
    
    @Query("SELECT * FROM perros WHERE id = :id")
    suspend fun getPerroById(id: Long): Perro?
    
    @Query("SELECT * FROM perros ORDER BY nombre ASC")
    fun getAllPerros(): LiveData<List<Perro>>
    
    @Query("SELECT * FROM perros ORDER BY nombre ASC")
    suspend fun getAllSync(): List<Perro>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(perro: Perro): Long
    
    @Update
    suspend fun update(perro: Perro)
    
    @Delete
    suspend fun delete(perro: Perro)
    
    @Query("DELETE FROM perros WHERE clienteId = :clienteId")
    suspend fun deleteByCliente(clienteId: Long)
    
    @Query("DELETE FROM perros")
    suspend fun deleteAll()
}

@Dao
interface ServicioDao {
    @Query("SELECT * FROM servicios WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllServicios(): LiveData<List<Servicio>>
    
    @Query("SELECT * FROM servicios ORDER BY nombre ASC")
    suspend fun getAllSync(): List<Servicio>
    
    @Query("SELECT * FROM servicios WHERE id = :id")
    suspend fun getServicioById(id: Long): Servicio?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(servicio: Servicio): Long
    
    @Update
    suspend fun update(servicio: Servicio)
    
    @Delete
    suspend fun delete(servicio: Servicio)
    
    @Query("DELETE FROM servicios")
    suspend fun deleteAll()
}

@Dao
interface PrecioServicioDao {
    @Query("SELECT * FROM precios_servicio WHERE servicioId = :servicioId")
    fun getPreciosByServicio(servicioId: Long): LiveData<List<PrecioServicio>>
    
    @Query("SELECT * FROM precios_servicio WHERE servicioId = :servicioId")
    suspend fun getPreciosByServicioSync(servicioId: Long): List<PrecioServicio>
    
    @Query("SELECT * FROM precios_servicio")
    fun getAll(): LiveData<List<PrecioServicio>>
    
    @Query("SELECT * FROM precios_servicio")
    suspend fun getAllSync(): List<PrecioServicio>
    
    @Query("SELECT * FROM precios_servicio WHERE servicioId = :servicioId AND tamano = :tamano AND longitudPelo = :longitudPelo")
    suspend fun getPrecio(servicioId: Long, tamano: String, longitudPelo: String): PrecioServicio?
    
    @Query("SELECT * FROM precios_servicio WHERE servicioId = :servicioId AND raza = :raza AND tamano = :tamano AND longitudPelo = :longitudPelo")
    suspend fun getPrecioConRaza(servicioId: Long, raza: String, tamano: String, longitudPelo: String): PrecioServicio?
    
    @Query("SELECT * FROM precios_servicio WHERE servicioId = :servicioId AND raza IS NULL AND tamano = :tamano AND longitudPelo = :longitudPelo")
    suspend fun getPrecioSinRaza(servicioId: Long, tamano: String, longitudPelo: String): PrecioServicio?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(precio: PrecioServicio): Long
    
    @Update
    suspend fun update(precio: PrecioServicio)
    
    @Delete
    suspend fun delete(precio: PrecioServicio)
    
    @Query("DELETE FROM precios_servicio WHERE servicioId = :servicioId")
    suspend fun deleteByServicio(servicioId: Long)
    
    @Query("DELETE FROM precios_servicio")
    suspend fun deleteAll()
}

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas ORDER BY fecha DESC, hora DESC")
    fun getAllCitas(): LiveData<List<Cita>>
    
    @Query("SELECT * FROM citas ORDER BY fecha DESC")
    suspend fun getAllSync(): List<Cita>
    
    @Query("SELECT * FROM citas WHERE fecha >= :startDate AND fecha <= :endDate ORDER BY fecha ASC, hora ASC")
    fun getCitasByDateRange(startDate: Long, endDate: Long): LiveData<List<Cita>>
    
    @Query("SELECT * FROM citas WHERE fecha = :fecha ORDER BY hora ASC")
    fun getCitasByDate(fecha: Long): LiveData<List<Cita>>
    
    @Query("SELECT * FROM citas WHERE id = :id")
    suspend fun getCitaById(id: Long): Cita?
    
    @Query("SELECT * FROM citas WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun getCitasByCliente(clienteId: Long): LiveData<List<Cita>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cita: Cita): Long
    
    @Update
    suspend fun update(cita: Cita)
    
    @Delete
    suspend fun delete(cita: Cita)
    
    @Query("DELETE FROM citas")
    suspend fun deleteAll()
}

@Dao
interface RazaDao {
    @Query("SELECT * FROM razas ORDER BY nombre ASC")
    fun getAllRazas(): LiveData<List<Raza>>
    
    @Query("SELECT * FROM razas ORDER BY nombre ASC")
    suspend fun getAllSync(): List<Raza>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(raza: Raza): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(razas: List<Raza>)
    
    @Update
    suspend fun update(raza: Raza)
    
    @Delete
    suspend fun delete(raza: Raza)
    
    @Query("SELECT COUNT(*) FROM razas")
    suspend fun getCount(): Int
    
    @Query("DELETE FROM razas")
    suspend fun deleteAll()
}

@Dao
interface TamanoDao {
    @Query("SELECT * FROM tamanos ORDER BY id ASC")
    fun getAllTamanos(): LiveData<List<Tamano>>
    
    @Query("SELECT * FROM tamanos ORDER BY id ASC")
    suspend fun getAllSync(): List<Tamano>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tamano: Tamano): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tamanos: List<Tamano>)
    
    @Update
    suspend fun update(tamano: Tamano)
    
    @Delete
    suspend fun delete(tamano: Tamano)
    
    @Query("DELETE FROM tamanos")
    suspend fun deleteAll()
}

@Dao
interface LongitudPeloDao {
    @Query("SELECT * FROM longitudes_pelo ORDER BY id ASC")
    fun getAllLongitudesPelo(): LiveData<List<LongitudPelo>>
    
    @Query("SELECT * FROM longitudes_pelo ORDER BY id ASC")
    suspend fun getAllSync(): List<LongitudPelo>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(longitudPelo: LongitudPelo): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(longitudesPelo: List<LongitudPelo>)
    
    @Update
    suspend fun update(longitudPelo: LongitudPelo)
    
    @Delete
    suspend fun delete(longitudPelo: LongitudPelo)
    
    @Query("DELETE FROM longitudes_pelo")
    suspend fun deleteAll()
}

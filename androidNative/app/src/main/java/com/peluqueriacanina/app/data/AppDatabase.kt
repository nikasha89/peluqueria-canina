package com.peluqueriacanina.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Cliente::class,
        Perro::class,
        Servicio::class,
        PrecioServicio::class,
        Cita::class,
        Raza::class,
        Tamano::class,
        LongitudPelo::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun clienteDao(): ClienteDao
    abstract fun perroDao(): PerroDao
    abstract fun servicioDao(): ServicioDao
    abstract fun precioServicioDao(): PrecioServicioDao
    abstract fun citaDao(): CitaDao
    abstract fun razaDao(): RazaDao
    abstract fun tamanoDao(): TamanoDao
    abstract fun longitudPeloDao(): LongitudPeloDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2: add foto column to perros
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE perros ADD COLUMN foto TEXT")
            }
        }
        
        // Migration from version 2 to 3: add raza column to precios_servicio
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE precios_servicio ADD COLUMN raza TEXT")
            }
        }
        
        // Migration from version 3 to 4: add tamanos and longitudes_pelo tables
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS tamanos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS longitudes_pelo (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL
                    )
                """)
                // Insert default values
                database.execSQL("INSERT INTO tamanos (nombre) VALUES ('mini'), ('pequeno'), ('mediano'), ('grande'), ('gigante')")
                database.execSQL("INSERT INTO longitudes_pelo (nombre) VALUES ('corto'), ('medio'), ('largo')")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "peluqueria_canina_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        private suspend fun populateDatabase(database: AppDatabase) {
            // Insert default breeds
            val razas = listOf(
                Raza(nombre = "Mestizo"),
                Raza(nombre = "Labrador Retriever"),
                Raza(nombre = "Golden Retriever"),
                Raza(nombre = "Pastor Alemán"),
                Raza(nombre = "Bulldog Francés"),
                Raza(nombre = "Bulldog Inglés"),
                Raza(nombre = "Poodle/Caniche"),
                Raza(nombre = "Chihuahua"),
                Raza(nombre = "Yorkshire Terrier"),
                Raza(nombre = "Bichón Maltés"),
                Raza(nombre = "Bichón Frisé"),
                Raza(nombre = "Shih Tzu"),
                Raza(nombre = "Pomerania"),
                Raza(nombre = "Schnauzer"),
                Raza(nombre = "Cocker Spaniel"),
                Raza(nombre = "Beagle"),
                Raza(nombre = "Boxer"),
                Raza(nombre = "Rottweiler"),
                Raza(nombre = "Husky Siberiano"),
                Raza(nombre = "Border Collie"),
                Raza(nombre = "Jack Russell Terrier"),
                Raza(nombre = "West Highland White Terrier"),
                Raza(nombre = "Cavalier King Charles"),
                Raza(nombre = "Teckel/Dachshund"),
                Raza(nombre = "Pug/Carlino"),
                Raza(nombre = "Doberman"),
                Raza(nombre = "Gran Danés"),
                Raza(nombre = "San Bernardo"),
                Raza(nombre = "Samoyedo"),
                Raza(nombre = "Akita Inu"),
                Raza(nombre = "Shiba Inu"),
                Raza(nombre = "Setter Irlandés"),
                Raza(nombre = "Dálmata"),
                Raza(nombre = "Weimaraner"),
                Raza(nombre = "Vizsla"),
                Raza(nombre = "Pointer"),
                Raza(nombre = "Galgo"),
                Raza(nombre = "Whippet"),
                Raza(nombre = "Basenji"),
                Raza(nombre = "Chow Chow"),
                Raza(nombre = "Shar Pei"),
                Raza(nombre = "Mastín"),
                Raza(nombre = "Terranova"),
                Raza(nombre = "Boyero de Berna"),
                Raza(nombre = "Australian Shepherd"),
                Raza(nombre = "Corgi"),
                Raza(nombre = "Papillon"),
                Raza(nombre = "Lhasa Apso"),
                Raza(nombre = "Boston Terrier"),
                Raza(nombre = "Otro")
            )
            database.razaDao().insertAll(razas)
            
            // Insert default sizes
            val tamanos = listOf(
                Tamano(nombre = "mini"),
                Tamano(nombre = "pequeno"),
                Tamano(nombre = "mediano"),
                Tamano(nombre = "grande"),
                Tamano(nombre = "gigante")
            )
            database.tamanoDao().insertAll(tamanos)
            
            // Insert default fur lengths
            val pelos = listOf(
                LongitudPelo(nombre = "corto"),
                LongitudPelo(nombre = "medio"),
                LongitudPelo(nombre = "largo")
            )
            database.longitudPeloDao().insertAll(pelos)
            
            // Insert default services
            val servicios = listOf(
                Servicio(nombre = "Baño", descripcion = "Baño completo con champú", tipoPrecio = "fijo", precioBase = 15.0),
                Servicio(nombre = "Corte", descripcion = "Corte de pelo a máquina o tijera", tipoPrecio = "porTamano", precioBase = 20.0),
                Servicio(nombre = "Baño + Corte", descripcion = "Servicio completo", tipoPrecio = "porTamano", precioBase = 30.0),
                Servicio(nombre = "Corte de uñas", descripcion = "Corte y limado de uñas", tipoPrecio = "fijo", precioBase = 8.0),
                Servicio(nombre = "Limpieza de oídos", descripcion = "Limpieza de oídos", tipoPrecio = "fijo", precioBase = 5.0),
                Servicio(nombre = "Deslanado", descripcion = "Eliminación de pelo muerto", tipoPrecio = "porTamano", precioBase = 25.0)
            )
            servicios.forEach { database.servicioDao().insert(it) }
        }
    }
}

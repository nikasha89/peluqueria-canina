package com.peluqueriacanina.app.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ClienteDao _clienteDao;

  private volatile PerroDao _perroDao;

  private volatile ServicioDao _servicioDao;

  private volatile PrecioServicioDao _precioServicioDao;

  private volatile CitaDao _citaDao;

  private volatile RazaDao _razaDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `clientes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `telefono` TEXT NOT NULL, `email` TEXT NOT NULL, `notas` TEXT NOT NULL, `fechaCreacion` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `perros` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clienteId` INTEGER NOT NULL, `nombre` TEXT NOT NULL, `raza` TEXT NOT NULL, `tamano` TEXT NOT NULL, `longitudPelo` TEXT NOT NULL, `edad` INTEGER, `notas` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `servicios` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `descripcion` TEXT NOT NULL, `tipoPrecio` TEXT NOT NULL, `precioBase` REAL NOT NULL, `activo` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `precios_servicio` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `servicioId` INTEGER NOT NULL, `tamano` TEXT NOT NULL, `longitudPelo` TEXT NOT NULL, `precio` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `citas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `clienteId` INTEGER NOT NULL, `perroId` INTEGER NOT NULL, `fecha` INTEGER NOT NULL, `hora` TEXT NOT NULL, `serviciosIds` TEXT NOT NULL, `precioTotal` REAL NOT NULL, `estado` TEXT NOT NULL, `notas` TEXT NOT NULL, `googleEventId` TEXT, `fechaCreacion` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `razas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e8a47b3ecb573bb9b2b7c25c40cc2717')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `clientes`");
        db.execSQL("DROP TABLE IF EXISTS `perros`");
        db.execSQL("DROP TABLE IF EXISTS `servicios`");
        db.execSQL("DROP TABLE IF EXISTS `precios_servicio`");
        db.execSQL("DROP TABLE IF EXISTS `citas`");
        db.execSQL("DROP TABLE IF EXISTS `razas`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsClientes = new HashMap<String, TableInfo.Column>(6);
        _columnsClientes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("telefono", new TableInfo.Column("telefono", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("notas", new TableInfo.Column("notas", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClientes.put("fechaCreacion", new TableInfo.Column("fechaCreacion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysClientes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesClientes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoClientes = new TableInfo("clientes", _columnsClientes, _foreignKeysClientes, _indicesClientes);
        final TableInfo _existingClientes = TableInfo.read(db, "clientes");
        if (!_infoClientes.equals(_existingClientes)) {
          return new RoomOpenHelper.ValidationResult(false, "clientes(com.peluqueriacanina.app.data.Cliente).\n"
                  + " Expected:\n" + _infoClientes + "\n"
                  + " Found:\n" + _existingClientes);
        }
        final HashMap<String, TableInfo.Column> _columnsPerros = new HashMap<String, TableInfo.Column>(8);
        _columnsPerros.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("clienteId", new TableInfo.Column("clienteId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("raza", new TableInfo.Column("raza", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("tamano", new TableInfo.Column("tamano", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("longitudPelo", new TableInfo.Column("longitudPelo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("edad", new TableInfo.Column("edad", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerros.put("notas", new TableInfo.Column("notas", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPerros = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPerros = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPerros = new TableInfo("perros", _columnsPerros, _foreignKeysPerros, _indicesPerros);
        final TableInfo _existingPerros = TableInfo.read(db, "perros");
        if (!_infoPerros.equals(_existingPerros)) {
          return new RoomOpenHelper.ValidationResult(false, "perros(com.peluqueriacanina.app.data.Perro).\n"
                  + " Expected:\n" + _infoPerros + "\n"
                  + " Found:\n" + _existingPerros);
        }
        final HashMap<String, TableInfo.Column> _columnsServicios = new HashMap<String, TableInfo.Column>(6);
        _columnsServicios.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServicios.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServicios.put("descripcion", new TableInfo.Column("descripcion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServicios.put("tipoPrecio", new TableInfo.Column("tipoPrecio", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServicios.put("precioBase", new TableInfo.Column("precioBase", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsServicios.put("activo", new TableInfo.Column("activo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysServicios = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesServicios = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoServicios = new TableInfo("servicios", _columnsServicios, _foreignKeysServicios, _indicesServicios);
        final TableInfo _existingServicios = TableInfo.read(db, "servicios");
        if (!_infoServicios.equals(_existingServicios)) {
          return new RoomOpenHelper.ValidationResult(false, "servicios(com.peluqueriacanina.app.data.Servicio).\n"
                  + " Expected:\n" + _infoServicios + "\n"
                  + " Found:\n" + _existingServicios);
        }
        final HashMap<String, TableInfo.Column> _columnsPreciosServicio = new HashMap<String, TableInfo.Column>(5);
        _columnsPreciosServicio.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreciosServicio.put("servicioId", new TableInfo.Column("servicioId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreciosServicio.put("tamano", new TableInfo.Column("tamano", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreciosServicio.put("longitudPelo", new TableInfo.Column("longitudPelo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPreciosServicio.put("precio", new TableInfo.Column("precio", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPreciosServicio = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPreciosServicio = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPreciosServicio = new TableInfo("precios_servicio", _columnsPreciosServicio, _foreignKeysPreciosServicio, _indicesPreciosServicio);
        final TableInfo _existingPreciosServicio = TableInfo.read(db, "precios_servicio");
        if (!_infoPreciosServicio.equals(_existingPreciosServicio)) {
          return new RoomOpenHelper.ValidationResult(false, "precios_servicio(com.peluqueriacanina.app.data.PrecioServicio).\n"
                  + " Expected:\n" + _infoPreciosServicio + "\n"
                  + " Found:\n" + _existingPreciosServicio);
        }
        final HashMap<String, TableInfo.Column> _columnsCitas = new HashMap<String, TableInfo.Column>(11);
        _columnsCitas.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("clienteId", new TableInfo.Column("clienteId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("perroId", new TableInfo.Column("perroId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("fecha", new TableInfo.Column("fecha", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("hora", new TableInfo.Column("hora", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("serviciosIds", new TableInfo.Column("serviciosIds", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("precioTotal", new TableInfo.Column("precioTotal", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("estado", new TableInfo.Column("estado", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("notas", new TableInfo.Column("notas", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("googleEventId", new TableInfo.Column("googleEventId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCitas.put("fechaCreacion", new TableInfo.Column("fechaCreacion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCitas = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCitas = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCitas = new TableInfo("citas", _columnsCitas, _foreignKeysCitas, _indicesCitas);
        final TableInfo _existingCitas = TableInfo.read(db, "citas");
        if (!_infoCitas.equals(_existingCitas)) {
          return new RoomOpenHelper.ValidationResult(false, "citas(com.peluqueriacanina.app.data.Cita).\n"
                  + " Expected:\n" + _infoCitas + "\n"
                  + " Found:\n" + _existingCitas);
        }
        final HashMap<String, TableInfo.Column> _columnsRazas = new HashMap<String, TableInfo.Column>(2);
        _columnsRazas.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRazas.put("nombre", new TableInfo.Column("nombre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRazas = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRazas = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRazas = new TableInfo("razas", _columnsRazas, _foreignKeysRazas, _indicesRazas);
        final TableInfo _existingRazas = TableInfo.read(db, "razas");
        if (!_infoRazas.equals(_existingRazas)) {
          return new RoomOpenHelper.ValidationResult(false, "razas(com.peluqueriacanina.app.data.Raza).\n"
                  + " Expected:\n" + _infoRazas + "\n"
                  + " Found:\n" + _existingRazas);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e8a47b3ecb573bb9b2b7c25c40cc2717", "68fd8b14576489d7cb1e5c78cb16ea65");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "clientes","perros","servicios","precios_servicio","citas","razas");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `clientes`");
      _db.execSQL("DELETE FROM `perros`");
      _db.execSQL("DELETE FROM `servicios`");
      _db.execSQL("DELETE FROM `precios_servicio`");
      _db.execSQL("DELETE FROM `citas`");
      _db.execSQL("DELETE FROM `razas`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ClienteDao.class, ClienteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PerroDao.class, PerroDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ServicioDao.class, ServicioDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PrecioServicioDao.class, PrecioServicioDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CitaDao.class, CitaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RazaDao.class, RazaDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ClienteDao clienteDao() {
    if (_clienteDao != null) {
      return _clienteDao;
    } else {
      synchronized(this) {
        if(_clienteDao == null) {
          _clienteDao = new ClienteDao_Impl(this);
        }
        return _clienteDao;
      }
    }
  }

  @Override
  public PerroDao perroDao() {
    if (_perroDao != null) {
      return _perroDao;
    } else {
      synchronized(this) {
        if(_perroDao == null) {
          _perroDao = new PerroDao_Impl(this);
        }
        return _perroDao;
      }
    }
  }

  @Override
  public ServicioDao servicioDao() {
    if (_servicioDao != null) {
      return _servicioDao;
    } else {
      synchronized(this) {
        if(_servicioDao == null) {
          _servicioDao = new ServicioDao_Impl(this);
        }
        return _servicioDao;
      }
    }
  }

  @Override
  public PrecioServicioDao precioServicioDao() {
    if (_precioServicioDao != null) {
      return _precioServicioDao;
    } else {
      synchronized(this) {
        if(_precioServicioDao == null) {
          _precioServicioDao = new PrecioServicioDao_Impl(this);
        }
        return _precioServicioDao;
      }
    }
  }

  @Override
  public CitaDao citaDao() {
    if (_citaDao != null) {
      return _citaDao;
    } else {
      synchronized(this) {
        if(_citaDao == null) {
          _citaDao = new CitaDao_Impl(this);
        }
        return _citaDao;
      }
    }
  }

  @Override
  public RazaDao razaDao() {
    if (_razaDao != null) {
      return _razaDao;
    } else {
      synchronized(this) {
        if(_razaDao == null) {
          _razaDao = new RazaDao_Impl(this);
        }
        return _razaDao;
      }
    }
  }
}

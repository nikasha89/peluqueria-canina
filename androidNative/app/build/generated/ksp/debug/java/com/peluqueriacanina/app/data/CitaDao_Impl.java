package com.peluqueriacanina.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CitaDao_Impl implements CitaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Cita> __insertionAdapterOfCita;

  private final EntityDeletionOrUpdateAdapter<Cita> __deletionAdapterOfCita;

  private final EntityDeletionOrUpdateAdapter<Cita> __updateAdapterOfCita;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public CitaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCita = new EntityInsertionAdapter<Cita>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `citas` (`id`,`clienteId`,`perroId`,`fecha`,`hora`,`serviciosIds`,`precioTotal`,`estado`,`notas`,`googleEventId`,`fechaCreacion`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cita entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClienteId());
        statement.bindLong(3, entity.getPerroId());
        statement.bindLong(4, entity.getFecha());
        statement.bindString(5, entity.getHora());
        statement.bindString(6, entity.getServiciosIds());
        statement.bindDouble(7, entity.getPrecioTotal());
        statement.bindString(8, entity.getEstado());
        statement.bindString(9, entity.getNotas());
        if (entity.getGoogleEventId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getGoogleEventId());
        }
        statement.bindLong(11, entity.getFechaCreacion());
      }
    };
    this.__deletionAdapterOfCita = new EntityDeletionOrUpdateAdapter<Cita>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `citas` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cita entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCita = new EntityDeletionOrUpdateAdapter<Cita>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `citas` SET `id` = ?,`clienteId` = ?,`perroId` = ?,`fecha` = ?,`hora` = ?,`serviciosIds` = ?,`precioTotal` = ?,`estado` = ?,`notas` = ?,`googleEventId` = ?,`fechaCreacion` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Cita entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClienteId());
        statement.bindLong(3, entity.getPerroId());
        statement.bindLong(4, entity.getFecha());
        statement.bindString(5, entity.getHora());
        statement.bindString(6, entity.getServiciosIds());
        statement.bindDouble(7, entity.getPrecioTotal());
        statement.bindString(8, entity.getEstado());
        statement.bindString(9, entity.getNotas());
        if (entity.getGoogleEventId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getGoogleEventId());
        }
        statement.bindLong(11, entity.getFechaCreacion());
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM citas";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Cita cita, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCita.insertAndReturnId(cita);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Cita cita, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCita.handle(cita);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Cita cita, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCita.handle(cita);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Cita>> getAllCitas() {
    final String _sql = "SELECT * FROM citas ORDER BY fecha DESC, hora DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"citas"}, false, new Callable<List<Cita>>() {
      @Override
      @Nullable
      public List<Cita> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfPerroId = CursorUtil.getColumnIndexOrThrow(_cursor, "perroId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfServiciosIds = CursorUtil.getColumnIndexOrThrow(_cursor, "serviciosIds");
          final int _cursorIndexOfPrecioTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "precioTotal");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final int _cursorIndexOfGoogleEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "googleEventId");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<Cita> _result = new ArrayList<Cita>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cita _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final long _tmpPerroId;
            _tmpPerroId = _cursor.getLong(_cursorIndexOfPerroId);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpServiciosIds;
            _tmpServiciosIds = _cursor.getString(_cursorIndexOfServiciosIds);
            final double _tmpPrecioTotal;
            _tmpPrecioTotal = _cursor.getDouble(_cursorIndexOfPrecioTotal);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            final String _tmpGoogleEventId;
            if (_cursor.isNull(_cursorIndexOfGoogleEventId)) {
              _tmpGoogleEventId = null;
            } else {
              _tmpGoogleEventId = _cursor.getString(_cursorIndexOfGoogleEventId);
            }
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _item = new Cita(_tmpId,_tmpClienteId,_tmpPerroId,_tmpFecha,_tmpHora,_tmpServiciosIds,_tmpPrecioTotal,_tmpEstado,_tmpNotas,_tmpGoogleEventId,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllSync(final Continuation<? super List<Cita>> $completion) {
    final String _sql = "SELECT * FROM citas ORDER BY fecha DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Cita>>() {
      @Override
      @NonNull
      public List<Cita> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfPerroId = CursorUtil.getColumnIndexOrThrow(_cursor, "perroId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfServiciosIds = CursorUtil.getColumnIndexOrThrow(_cursor, "serviciosIds");
          final int _cursorIndexOfPrecioTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "precioTotal");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final int _cursorIndexOfGoogleEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "googleEventId");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<Cita> _result = new ArrayList<Cita>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cita _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final long _tmpPerroId;
            _tmpPerroId = _cursor.getLong(_cursorIndexOfPerroId);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpServiciosIds;
            _tmpServiciosIds = _cursor.getString(_cursorIndexOfServiciosIds);
            final double _tmpPrecioTotal;
            _tmpPrecioTotal = _cursor.getDouble(_cursorIndexOfPrecioTotal);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            final String _tmpGoogleEventId;
            if (_cursor.isNull(_cursorIndexOfGoogleEventId)) {
              _tmpGoogleEventId = null;
            } else {
              _tmpGoogleEventId = _cursor.getString(_cursorIndexOfGoogleEventId);
            }
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _item = new Cita(_tmpId,_tmpClienteId,_tmpPerroId,_tmpFecha,_tmpHora,_tmpServiciosIds,_tmpPrecioTotal,_tmpEstado,_tmpNotas,_tmpGoogleEventId,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Cita>> getCitasByDateRange(final long startDate, final long endDate) {
    final String _sql = "SELECT * FROM citas WHERE fecha >= ? AND fecha <= ? ORDER BY fecha ASC, hora ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return __db.getInvalidationTracker().createLiveData(new String[] {"citas"}, false, new Callable<List<Cita>>() {
      @Override
      @Nullable
      public List<Cita> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfPerroId = CursorUtil.getColumnIndexOrThrow(_cursor, "perroId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfServiciosIds = CursorUtil.getColumnIndexOrThrow(_cursor, "serviciosIds");
          final int _cursorIndexOfPrecioTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "precioTotal");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final int _cursorIndexOfGoogleEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "googleEventId");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<Cita> _result = new ArrayList<Cita>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cita _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final long _tmpPerroId;
            _tmpPerroId = _cursor.getLong(_cursorIndexOfPerroId);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpServiciosIds;
            _tmpServiciosIds = _cursor.getString(_cursorIndexOfServiciosIds);
            final double _tmpPrecioTotal;
            _tmpPrecioTotal = _cursor.getDouble(_cursorIndexOfPrecioTotal);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            final String _tmpGoogleEventId;
            if (_cursor.isNull(_cursorIndexOfGoogleEventId)) {
              _tmpGoogleEventId = null;
            } else {
              _tmpGoogleEventId = _cursor.getString(_cursorIndexOfGoogleEventId);
            }
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _item = new Cita(_tmpId,_tmpClienteId,_tmpPerroId,_tmpFecha,_tmpHora,_tmpServiciosIds,_tmpPrecioTotal,_tmpEstado,_tmpNotas,_tmpGoogleEventId,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<Cita>> getCitasByDate(final long fecha) {
    final String _sql = "SELECT * FROM citas WHERE fecha = ? ORDER BY hora ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fecha);
    return __db.getInvalidationTracker().createLiveData(new String[] {"citas"}, false, new Callable<List<Cita>>() {
      @Override
      @Nullable
      public List<Cita> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfPerroId = CursorUtil.getColumnIndexOrThrow(_cursor, "perroId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfServiciosIds = CursorUtil.getColumnIndexOrThrow(_cursor, "serviciosIds");
          final int _cursorIndexOfPrecioTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "precioTotal");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final int _cursorIndexOfGoogleEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "googleEventId");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<Cita> _result = new ArrayList<Cita>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cita _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final long _tmpPerroId;
            _tmpPerroId = _cursor.getLong(_cursorIndexOfPerroId);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpServiciosIds;
            _tmpServiciosIds = _cursor.getString(_cursorIndexOfServiciosIds);
            final double _tmpPrecioTotal;
            _tmpPrecioTotal = _cursor.getDouble(_cursorIndexOfPrecioTotal);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            final String _tmpGoogleEventId;
            if (_cursor.isNull(_cursorIndexOfGoogleEventId)) {
              _tmpGoogleEventId = null;
            } else {
              _tmpGoogleEventId = _cursor.getString(_cursorIndexOfGoogleEventId);
            }
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _item = new Cita(_tmpId,_tmpClienteId,_tmpPerroId,_tmpFecha,_tmpHora,_tmpServiciosIds,_tmpPrecioTotal,_tmpEstado,_tmpNotas,_tmpGoogleEventId,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getCitaById(final long id, final Continuation<? super Cita> $completion) {
    final String _sql = "SELECT * FROM citas WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Cita>() {
      @Override
      @Nullable
      public Cita call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfPerroId = CursorUtil.getColumnIndexOrThrow(_cursor, "perroId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfServiciosIds = CursorUtil.getColumnIndexOrThrow(_cursor, "serviciosIds");
          final int _cursorIndexOfPrecioTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "precioTotal");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final int _cursorIndexOfGoogleEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "googleEventId");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final Cita _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final long _tmpPerroId;
            _tmpPerroId = _cursor.getLong(_cursorIndexOfPerroId);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpServiciosIds;
            _tmpServiciosIds = _cursor.getString(_cursorIndexOfServiciosIds);
            final double _tmpPrecioTotal;
            _tmpPrecioTotal = _cursor.getDouble(_cursorIndexOfPrecioTotal);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            final String _tmpGoogleEventId;
            if (_cursor.isNull(_cursorIndexOfGoogleEventId)) {
              _tmpGoogleEventId = null;
            } else {
              _tmpGoogleEventId = _cursor.getString(_cursorIndexOfGoogleEventId);
            }
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _result = new Cita(_tmpId,_tmpClienteId,_tmpPerroId,_tmpFecha,_tmpHora,_tmpServiciosIds,_tmpPrecioTotal,_tmpEstado,_tmpNotas,_tmpGoogleEventId,_tmpFechaCreacion);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<Cita>> getCitasByCliente(final long clienteId) {
    final String _sql = "SELECT * FROM citas WHERE clienteId = ? ORDER BY fecha DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clienteId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"citas"}, false, new Callable<List<Cita>>() {
      @Override
      @Nullable
      public List<Cita> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfPerroId = CursorUtil.getColumnIndexOrThrow(_cursor, "perroId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfHora = CursorUtil.getColumnIndexOrThrow(_cursor, "hora");
          final int _cursorIndexOfServiciosIds = CursorUtil.getColumnIndexOrThrow(_cursor, "serviciosIds");
          final int _cursorIndexOfPrecioTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "precioTotal");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final int _cursorIndexOfGoogleEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "googleEventId");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<Cita> _result = new ArrayList<Cita>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Cita _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final long _tmpPerroId;
            _tmpPerroId = _cursor.getLong(_cursorIndexOfPerroId);
            final long _tmpFecha;
            _tmpFecha = _cursor.getLong(_cursorIndexOfFecha);
            final String _tmpHora;
            _tmpHora = _cursor.getString(_cursorIndexOfHora);
            final String _tmpServiciosIds;
            _tmpServiciosIds = _cursor.getString(_cursorIndexOfServiciosIds);
            final double _tmpPrecioTotal;
            _tmpPrecioTotal = _cursor.getDouble(_cursorIndexOfPrecioTotal);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            final String _tmpGoogleEventId;
            if (_cursor.isNull(_cursorIndexOfGoogleEventId)) {
              _tmpGoogleEventId = null;
            } else {
              _tmpGoogleEventId = _cursor.getString(_cursorIndexOfGoogleEventId);
            }
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _item = new Cita(_tmpId,_tmpClienteId,_tmpPerroId,_tmpFecha,_tmpHora,_tmpServiciosIds,_tmpPrecioTotal,_tmpEstado,_tmpNotas,_tmpGoogleEventId,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

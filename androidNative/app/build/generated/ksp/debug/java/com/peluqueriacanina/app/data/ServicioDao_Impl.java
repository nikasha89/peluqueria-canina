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
public final class ServicioDao_Impl implements ServicioDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Servicio> __insertionAdapterOfServicio;

  private final EntityDeletionOrUpdateAdapter<Servicio> __deletionAdapterOfServicio;

  private final EntityDeletionOrUpdateAdapter<Servicio> __updateAdapterOfServicio;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public ServicioDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfServicio = new EntityInsertionAdapter<Servicio>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `servicios` (`id`,`nombre`,`descripcion`,`tipoPrecio`,`precioBase`,`activo`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Servicio entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        statement.bindString(3, entity.getDescripcion());
        statement.bindString(4, entity.getTipoPrecio());
        statement.bindDouble(5, entity.getPrecioBase());
        final int _tmp = entity.getActivo() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfServicio = new EntityDeletionOrUpdateAdapter<Servicio>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `servicios` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Servicio entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfServicio = new EntityDeletionOrUpdateAdapter<Servicio>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `servicios` SET `id` = ?,`nombre` = ?,`descripcion` = ?,`tipoPrecio` = ?,`precioBase` = ?,`activo` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Servicio entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getNombre());
        statement.bindString(3, entity.getDescripcion());
        statement.bindString(4, entity.getTipoPrecio());
        statement.bindDouble(5, entity.getPrecioBase());
        final int _tmp = entity.getActivo() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM servicios";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Servicio servicio, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfServicio.insertAndReturnId(servicio);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Servicio servicio, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfServicio.handle(servicio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Servicio servicio, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfServicio.handle(servicio);
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
  public LiveData<List<Servicio>> getAllServicios() {
    final String _sql = "SELECT * FROM servicios WHERE activo = 1 ORDER BY nombre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"servicios"}, false, new Callable<List<Servicio>>() {
      @Override
      @Nullable
      public List<Servicio> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfTipoPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "tipoPrecio");
          final int _cursorIndexOfPrecioBase = CursorUtil.getColumnIndexOrThrow(_cursor, "precioBase");
          final int _cursorIndexOfActivo = CursorUtil.getColumnIndexOrThrow(_cursor, "activo");
          final List<Servicio> _result = new ArrayList<Servicio>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Servicio _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpDescripcion;
            _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            final String _tmpTipoPrecio;
            _tmpTipoPrecio = _cursor.getString(_cursorIndexOfTipoPrecio);
            final double _tmpPrecioBase;
            _tmpPrecioBase = _cursor.getDouble(_cursorIndexOfPrecioBase);
            final boolean _tmpActivo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActivo);
            _tmpActivo = _tmp != 0;
            _item = new Servicio(_tmpId,_tmpNombre,_tmpDescripcion,_tmpTipoPrecio,_tmpPrecioBase,_tmpActivo);
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
  public Object getAllSync(final Continuation<? super List<Servicio>> $completion) {
    final String _sql = "SELECT * FROM servicios ORDER BY nombre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Servicio>>() {
      @Override
      @NonNull
      public List<Servicio> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfTipoPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "tipoPrecio");
          final int _cursorIndexOfPrecioBase = CursorUtil.getColumnIndexOrThrow(_cursor, "precioBase");
          final int _cursorIndexOfActivo = CursorUtil.getColumnIndexOrThrow(_cursor, "activo");
          final List<Servicio> _result = new ArrayList<Servicio>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Servicio _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpDescripcion;
            _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            final String _tmpTipoPrecio;
            _tmpTipoPrecio = _cursor.getString(_cursorIndexOfTipoPrecio);
            final double _tmpPrecioBase;
            _tmpPrecioBase = _cursor.getDouble(_cursorIndexOfPrecioBase);
            final boolean _tmpActivo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActivo);
            _tmpActivo = _tmp != 0;
            _item = new Servicio(_tmpId,_tmpNombre,_tmpDescripcion,_tmpTipoPrecio,_tmpPrecioBase,_tmpActivo);
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
  public Object getServicioById(final long id, final Continuation<? super Servicio> $completion) {
    final String _sql = "SELECT * FROM servicios WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Servicio>() {
      @Override
      @Nullable
      public Servicio call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfTipoPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "tipoPrecio");
          final int _cursorIndexOfPrecioBase = CursorUtil.getColumnIndexOrThrow(_cursor, "precioBase");
          final int _cursorIndexOfActivo = CursorUtil.getColumnIndexOrThrow(_cursor, "activo");
          final Servicio _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpDescripcion;
            _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            final String _tmpTipoPrecio;
            _tmpTipoPrecio = _cursor.getString(_cursorIndexOfTipoPrecio);
            final double _tmpPrecioBase;
            _tmpPrecioBase = _cursor.getDouble(_cursorIndexOfPrecioBase);
            final boolean _tmpActivo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfActivo);
            _tmpActivo = _tmp != 0;
            _result = new Servicio(_tmpId,_tmpNombre,_tmpDescripcion,_tmpTipoPrecio,_tmpPrecioBase,_tmpActivo);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

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
public final class PrecioServicioDao_Impl implements PrecioServicioDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PrecioServicio> __insertionAdapterOfPrecioServicio;

  private final EntityDeletionOrUpdateAdapter<PrecioServicio> __deletionAdapterOfPrecioServicio;

  private final EntityDeletionOrUpdateAdapter<PrecioServicio> __updateAdapterOfPrecioServicio;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByServicio;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public PrecioServicioDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPrecioServicio = new EntityInsertionAdapter<PrecioServicio>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `precios_servicio` (`id`,`servicioId`,`tamano`,`longitudPelo`,`precio`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PrecioServicio entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getServicioId());
        statement.bindString(3, entity.getTamano());
        statement.bindString(4, entity.getLongitudPelo());
        statement.bindDouble(5, entity.getPrecio());
      }
    };
    this.__deletionAdapterOfPrecioServicio = new EntityDeletionOrUpdateAdapter<PrecioServicio>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `precios_servicio` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PrecioServicio entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPrecioServicio = new EntityDeletionOrUpdateAdapter<PrecioServicio>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `precios_servicio` SET `id` = ?,`servicioId` = ?,`tamano` = ?,`longitudPelo` = ?,`precio` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PrecioServicio entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getServicioId());
        statement.bindString(3, entity.getTamano());
        statement.bindString(4, entity.getLongitudPelo());
        statement.bindDouble(5, entity.getPrecio());
        statement.bindLong(6, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteByServicio = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM precios_servicio WHERE servicioId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM precios_servicio";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PrecioServicio precio, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPrecioServicio.insertAndReturnId(precio);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final PrecioServicio precio, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPrecioServicio.handle(precio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final PrecioServicio precio, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPrecioServicio.handle(precio);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByServicio(final long servicioId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByServicio.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, servicioId);
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
          __preparedStmtOfDeleteByServicio.release(_stmt);
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
  public LiveData<List<PrecioServicio>> getPreciosByServicio(final long servicioId) {
    final String _sql = "SELECT * FROM precios_servicio WHERE servicioId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, servicioId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"precios_servicio"}, false, new Callable<List<PrecioServicio>>() {
      @Override
      @Nullable
      public List<PrecioServicio> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServicioId = CursorUtil.getColumnIndexOrThrow(_cursor, "servicioId");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "precio");
          final List<PrecioServicio> _result = new ArrayList<PrecioServicio>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PrecioServicio _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServicioId;
            _tmpServicioId = _cursor.getLong(_cursorIndexOfServicioId);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final double _tmpPrecio;
            _tmpPrecio = _cursor.getDouble(_cursorIndexOfPrecio);
            _item = new PrecioServicio(_tmpId,_tmpServicioId,_tmpTamano,_tmpLongitudPelo,_tmpPrecio);
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
  public Object getAllSync(final Continuation<? super List<PrecioServicio>> $completion) {
    final String _sql = "SELECT * FROM precios_servicio";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PrecioServicio>>() {
      @Override
      @NonNull
      public List<PrecioServicio> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServicioId = CursorUtil.getColumnIndexOrThrow(_cursor, "servicioId");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "precio");
          final List<PrecioServicio> _result = new ArrayList<PrecioServicio>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PrecioServicio _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServicioId;
            _tmpServicioId = _cursor.getLong(_cursorIndexOfServicioId);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final double _tmpPrecio;
            _tmpPrecio = _cursor.getDouble(_cursorIndexOfPrecio);
            _item = new PrecioServicio(_tmpId,_tmpServicioId,_tmpTamano,_tmpLongitudPelo,_tmpPrecio);
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
  public Object getPrecio(final long servicioId, final String tamano, final String longitudPelo,
      final Continuation<? super PrecioServicio> $completion) {
    final String _sql = "SELECT * FROM precios_servicio WHERE servicioId = ? AND tamano = ? AND longitudPelo = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, servicioId);
    _argIndex = 2;
    _statement.bindString(_argIndex, tamano);
    _argIndex = 3;
    _statement.bindString(_argIndex, longitudPelo);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PrecioServicio>() {
      @Override
      @Nullable
      public PrecioServicio call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfServicioId = CursorUtil.getColumnIndexOrThrow(_cursor, "servicioId");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfPrecio = CursorUtil.getColumnIndexOrThrow(_cursor, "precio");
          final PrecioServicio _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpServicioId;
            _tmpServicioId = _cursor.getLong(_cursorIndexOfServicioId);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final double _tmpPrecio;
            _tmpPrecio = _cursor.getDouble(_cursorIndexOfPrecio);
            _result = new PrecioServicio(_tmpId,_tmpServicioId,_tmpTamano,_tmpLongitudPelo,_tmpPrecio);
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

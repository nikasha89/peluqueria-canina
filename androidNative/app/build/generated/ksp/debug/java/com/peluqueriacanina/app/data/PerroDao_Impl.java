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
import java.lang.Integer;
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
public final class PerroDao_Impl implements PerroDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Perro> __insertionAdapterOfPerro;

  private final EntityDeletionOrUpdateAdapter<Perro> __deletionAdapterOfPerro;

  private final EntityDeletionOrUpdateAdapter<Perro> __updateAdapterOfPerro;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByCliente;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public PerroDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerro = new EntityInsertionAdapter<Perro>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `perros` (`id`,`clienteId`,`nombre`,`raza`,`tamano`,`longitudPelo`,`edad`,`notas`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Perro entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClienteId());
        statement.bindString(3, entity.getNombre());
        statement.bindString(4, entity.getRaza());
        statement.bindString(5, entity.getTamano());
        statement.bindString(6, entity.getLongitudPelo());
        if (entity.getEdad() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getEdad());
        }
        statement.bindString(8, entity.getNotas());
      }
    };
    this.__deletionAdapterOfPerro = new EntityDeletionOrUpdateAdapter<Perro>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `perros` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Perro entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPerro = new EntityDeletionOrUpdateAdapter<Perro>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `perros` SET `id` = ?,`clienteId` = ?,`nombre` = ?,`raza` = ?,`tamano` = ?,`longitudPelo` = ?,`edad` = ?,`notas` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Perro entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getClienteId());
        statement.bindString(3, entity.getNombre());
        statement.bindString(4, entity.getRaza());
        statement.bindString(5, entity.getTamano());
        statement.bindString(6, entity.getLongitudPelo());
        if (entity.getEdad() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getEdad());
        }
        statement.bindString(8, entity.getNotas());
        statement.bindLong(9, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteByCliente = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM perros WHERE clienteId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM perros";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Perro perro, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPerro.insertAndReturnId(perro);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Perro perro, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPerro.handle(perro);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Perro perro, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPerro.handle(perro);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByCliente(final long clienteId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByCliente.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, clienteId);
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
          __preparedStmtOfDeleteByCliente.release(_stmt);
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
  public LiveData<List<Perro>> getPerrosByCliente(final long clienteId) {
    final String _sql = "SELECT * FROM perros WHERE clienteId = ? ORDER BY nombre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clienteId);
    return __db.getInvalidationTracker().createLiveData(new String[] {"perros"}, false, new Callable<List<Perro>>() {
      @Override
      @Nullable
      public List<Perro> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfRaza = CursorUtil.getColumnIndexOrThrow(_cursor, "raza");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfEdad = CursorUtil.getColumnIndexOrThrow(_cursor, "edad");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final List<Perro> _result = new ArrayList<Perro>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Perro _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpRaza;
            _tmpRaza = _cursor.getString(_cursorIndexOfRaza);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final Integer _tmpEdad;
            if (_cursor.isNull(_cursorIndexOfEdad)) {
              _tmpEdad = null;
            } else {
              _tmpEdad = _cursor.getInt(_cursorIndexOfEdad);
            }
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            _item = new Perro(_tmpId,_tmpClienteId,_tmpNombre,_tmpRaza,_tmpTamano,_tmpLongitudPelo,_tmpEdad,_tmpNotas);
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
  public Object getPerroById(final long id, final Continuation<? super Perro> $completion) {
    final String _sql = "SELECT * FROM perros WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Perro>() {
      @Override
      @Nullable
      public Perro call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfRaza = CursorUtil.getColumnIndexOrThrow(_cursor, "raza");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfEdad = CursorUtil.getColumnIndexOrThrow(_cursor, "edad");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final Perro _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpRaza;
            _tmpRaza = _cursor.getString(_cursorIndexOfRaza);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final Integer _tmpEdad;
            if (_cursor.isNull(_cursorIndexOfEdad)) {
              _tmpEdad = null;
            } else {
              _tmpEdad = _cursor.getInt(_cursorIndexOfEdad);
            }
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            _result = new Perro(_tmpId,_tmpClienteId,_tmpNombre,_tmpRaza,_tmpTamano,_tmpLongitudPelo,_tmpEdad,_tmpNotas);
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
  public LiveData<List<Perro>> getAllPerros() {
    final String _sql = "SELECT * FROM perros ORDER BY nombre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"perros"}, false, new Callable<List<Perro>>() {
      @Override
      @Nullable
      public List<Perro> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfRaza = CursorUtil.getColumnIndexOrThrow(_cursor, "raza");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfEdad = CursorUtil.getColumnIndexOrThrow(_cursor, "edad");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final List<Perro> _result = new ArrayList<Perro>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Perro _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpRaza;
            _tmpRaza = _cursor.getString(_cursorIndexOfRaza);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final Integer _tmpEdad;
            if (_cursor.isNull(_cursorIndexOfEdad)) {
              _tmpEdad = null;
            } else {
              _tmpEdad = _cursor.getInt(_cursorIndexOfEdad);
            }
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            _item = new Perro(_tmpId,_tmpClienteId,_tmpNombre,_tmpRaza,_tmpTamano,_tmpLongitudPelo,_tmpEdad,_tmpNotas);
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
  public Object getAllSync(final Continuation<? super List<Perro>> $completion) {
    final String _sql = "SELECT * FROM perros ORDER BY nombre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Perro>>() {
      @Override
      @NonNull
      public List<Perro> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClienteId = CursorUtil.getColumnIndexOrThrow(_cursor, "clienteId");
          final int _cursorIndexOfNombre = CursorUtil.getColumnIndexOrThrow(_cursor, "nombre");
          final int _cursorIndexOfRaza = CursorUtil.getColumnIndexOrThrow(_cursor, "raza");
          final int _cursorIndexOfTamano = CursorUtil.getColumnIndexOrThrow(_cursor, "tamano");
          final int _cursorIndexOfLongitudPelo = CursorUtil.getColumnIndexOrThrow(_cursor, "longitudPelo");
          final int _cursorIndexOfEdad = CursorUtil.getColumnIndexOrThrow(_cursor, "edad");
          final int _cursorIndexOfNotas = CursorUtil.getColumnIndexOrThrow(_cursor, "notas");
          final List<Perro> _result = new ArrayList<Perro>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Perro _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpClienteId;
            _tmpClienteId = _cursor.getLong(_cursorIndexOfClienteId);
            final String _tmpNombre;
            _tmpNombre = _cursor.getString(_cursorIndexOfNombre);
            final String _tmpRaza;
            _tmpRaza = _cursor.getString(_cursorIndexOfRaza);
            final String _tmpTamano;
            _tmpTamano = _cursor.getString(_cursorIndexOfTamano);
            final String _tmpLongitudPelo;
            _tmpLongitudPelo = _cursor.getString(_cursorIndexOfLongitudPelo);
            final Integer _tmpEdad;
            if (_cursor.isNull(_cursorIndexOfEdad)) {
              _tmpEdad = null;
            } else {
              _tmpEdad = _cursor.getInt(_cursorIndexOfEdad);
            }
            final String _tmpNotas;
            _tmpNotas = _cursor.getString(_cursorIndexOfNotas);
            _item = new Perro(_tmpId,_tmpClienteId,_tmpNombre,_tmpRaza,_tmpTamano,_tmpLongitudPelo,_tmpEdad,_tmpNotas);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

package com.mybbm.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mybbm.app.data.local.entity.KendaraanEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class KendaraanDao_Impl implements KendaraanDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<KendaraanEntity> __insertionAdapterOfKendaraanEntity;

  private final EntityDeletionOrUpdateAdapter<KendaraanEntity> __updateAdapterOfKendaraanEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearDefault;

  private final SharedSQLiteStatement __preparedStmtOfMarkDefault;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  public KendaraanDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfKendaraanEntity = new EntityInsertionAdapter<KendaraanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `kendaraan` (`id_kendaraan`,`nama_kendaraan`,`jenis_kendaraan`,`konsumsi_km_per_liter`,`is_default`,`created_at`,`updated_at`,`deleted_at`,`sync_state`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KendaraanEntity entity) {
        statement.bindString(1, entity.getIdKendaraan());
        statement.bindString(2, entity.getNamaKendaraan());
        statement.bindString(3, entity.getJenisKendaraan());
        statement.bindString(4, entity.getKonsumsiKmPerLiter());
        final int _tmp = entity.isDefault() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeletedAt());
        }
        statement.bindLong(9, entity.getSyncState());
      }
    };
    this.__updateAdapterOfKendaraanEntity = new EntityDeletionOrUpdateAdapter<KendaraanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `kendaraan` SET `id_kendaraan` = ?,`nama_kendaraan` = ?,`jenis_kendaraan` = ?,`konsumsi_km_per_liter` = ?,`is_default` = ?,`created_at` = ?,`updated_at` = ?,`deleted_at` = ?,`sync_state` = ? WHERE `id_kendaraan` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final KendaraanEntity entity) {
        statement.bindString(1, entity.getIdKendaraan());
        statement.bindString(2, entity.getNamaKendaraan());
        statement.bindString(3, entity.getJenisKendaraan());
        statement.bindString(4, entity.getKonsumsiKmPerLiter());
        final int _tmp = entity.isDefault() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getDeletedAt());
        }
        statement.bindLong(9, entity.getSyncState());
        statement.bindString(10, entity.getIdKendaraan());
      }
    };
    this.__preparedStmtOfClearDefault = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE kendaraan\n"
                + "        SET    is_default = 0,\n"
                + "               sync_state = 1,\n"
                + "               updated_at = ?\n"
                + "        WHERE  is_default = 1\n"
                + "          AND  deleted_at IS NULL\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfMarkDefault = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE kendaraan\n"
                + "        SET    is_default = 1,\n"
                + "               sync_state = 1,\n"
                + "               updated_at = ?\n"
                + "        WHERE  id_kendaraan = ?\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE kendaraan\n"
                + "        SET    deleted_at = ?,\n"
                + "               updated_at = ?,\n"
                + "               sync_state = 1\n"
                + "        WHERE  id_kendaraan = ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final KendaraanEntity kendaraan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfKendaraanEntity.insert(kendaraan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<KendaraanEntity> kendaraan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfKendaraanEntity.insert(kendaraan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final KendaraanEntity kendaraan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfKendaraanEntity.handle(kendaraan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setDefault(final String id, final long now,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> KendaraanDao.DefaultImpls.setDefault(KendaraanDao_Impl.this, id, now, __cont), $completion);
  }

  @Override
  public Object clearDefault(final long now, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearDefault.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
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
          __preparedStmtOfClearDefault.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markDefault(final String id, final long now,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkDefault.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfMarkDefault.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object softDelete(final String id, final long now,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, now);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, now);
        _argIndex = 3;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<KendaraanEntity> observeDefault() {
    final String _sql = "\n"
            + "        SELECT * FROM kendaraan\n"
            + "        WHERE  is_default = 1\n"
            + "          AND  deleted_at IS NULL\n"
            + "        LIMIT  1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"kendaraan"}, new Callable<KendaraanEntity>() {
      @Override
      @Nullable
      public KendaraanEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfNamaKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_kendaraan");
          final int _cursorIndexOfJenisKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_kendaraan");
          final int _cursorIndexOfKonsumsiKmPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "konsumsi_km_per_liter");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final KendaraanEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpNamaKendaraan;
            _tmpNamaKendaraan = _cursor.getString(_cursorIndexOfNamaKendaraan);
            final String _tmpJenisKendaraan;
            _tmpJenisKendaraan = _cursor.getString(_cursorIndexOfJenisKendaraan);
            final String _tmpKonsumsiKmPerLiter;
            _tmpKonsumsiKmPerLiter = _cursor.getString(_cursorIndexOfKonsumsiKmPerLiter);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new KendaraanEntity(_tmpIdKendaraan,_tmpNamaKendaraan,_tmpJenisKendaraan,_tmpKonsumsiKmPerLiter,_tmpIsDefault,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
          } else {
            _result = null;
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
  public Flow<List<KendaraanEntity>> observeAll() {
    final String _sql = "\n"
            + "        SELECT * FROM kendaraan\n"
            + "        WHERE  deleted_at IS NULL\n"
            + "        ORDER  BY created_at DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"kendaraan"}, new Callable<List<KendaraanEntity>>() {
      @Override
      @NonNull
      public List<KendaraanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfNamaKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_kendaraan");
          final int _cursorIndexOfJenisKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_kendaraan");
          final int _cursorIndexOfKonsumsiKmPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "konsumsi_km_per_liter");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<KendaraanEntity> _result = new ArrayList<KendaraanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KendaraanEntity _item;
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpNamaKendaraan;
            _tmpNamaKendaraan = _cursor.getString(_cursorIndexOfNamaKendaraan);
            final String _tmpJenisKendaraan;
            _tmpJenisKendaraan = _cursor.getString(_cursorIndexOfJenisKendaraan);
            final String _tmpKonsumsiKmPerLiter;
            _tmpKonsumsiKmPerLiter = _cursor.getString(_cursorIndexOfKonsumsiKmPerLiter);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new KendaraanEntity(_tmpIdKendaraan,_tmpNamaKendaraan,_tmpJenisKendaraan,_tmpKonsumsiKmPerLiter,_tmpIsDefault,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getById(final String id, final Continuation<? super KendaraanEntity> $completion) {
    final String _sql = "SELECT * FROM kendaraan WHERE id_kendaraan = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<KendaraanEntity>() {
      @Override
      @Nullable
      public KendaraanEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfNamaKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_kendaraan");
          final int _cursorIndexOfJenisKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_kendaraan");
          final int _cursorIndexOfKonsumsiKmPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "konsumsi_km_per_liter");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final KendaraanEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpNamaKendaraan;
            _tmpNamaKendaraan = _cursor.getString(_cursorIndexOfNamaKendaraan);
            final String _tmpJenisKendaraan;
            _tmpJenisKendaraan = _cursor.getString(_cursorIndexOfJenisKendaraan);
            final String _tmpKonsumsiKmPerLiter;
            _tmpKonsumsiKmPerLiter = _cursor.getString(_cursorIndexOfKonsumsiKmPerLiter);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new KendaraanEntity(_tmpIdKendaraan,_tmpNamaKendaraan,_tmpJenisKendaraan,_tmpKonsumsiKmPerLiter,_tmpIsDefault,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getPending(final Continuation<? super List<KendaraanEntity>> $completion) {
    final String _sql = "SELECT * FROM kendaraan WHERE sync_state = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<KendaraanEntity>>() {
      @Override
      @NonNull
      public List<KendaraanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfNamaKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_kendaraan");
          final int _cursorIndexOfJenisKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_kendaraan");
          final int _cursorIndexOfKonsumsiKmPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "konsumsi_km_per_liter");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<KendaraanEntity> _result = new ArrayList<KendaraanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final KendaraanEntity _item;
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpNamaKendaraan;
            _tmpNamaKendaraan = _cursor.getString(_cursorIndexOfNamaKendaraan);
            final String _tmpJenisKendaraan;
            _tmpJenisKendaraan = _cursor.getString(_cursorIndexOfJenisKendaraan);
            final String _tmpKonsumsiKmPerLiter;
            _tmpKonsumsiKmPerLiter = _cursor.getString(_cursorIndexOfKonsumsiKmPerLiter);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _item = new KendaraanEntity(_tmpIdKendaraan,_tmpNamaKendaraan,_tmpJenisKendaraan,_tmpKonsumsiKmPerLiter,_tmpIsDefault,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getLatestActive(final Continuation<? super KendaraanEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM kendaraan\n"
            + "        WHERE  deleted_at IS NULL\n"
            + "        ORDER  BY created_at DESC\n"
            + "        LIMIT  1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<KendaraanEntity>() {
      @Override
      @Nullable
      public KendaraanEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfNamaKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_kendaraan");
          final int _cursorIndexOfJenisKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_kendaraan");
          final int _cursorIndexOfKonsumsiKmPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "konsumsi_km_per_liter");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final KendaraanEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpNamaKendaraan;
            _tmpNamaKendaraan = _cursor.getString(_cursorIndexOfNamaKendaraan);
            final String _tmpJenisKendaraan;
            _tmpJenisKendaraan = _cursor.getString(_cursorIndexOfJenisKendaraan);
            final String _tmpKonsumsiKmPerLiter;
            _tmpKonsumsiKmPerLiter = _cursor.getString(_cursorIndexOfKonsumsiKmPerLiter);
            final boolean _tmpIsDefault;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpDeletedAt;
            if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
              _tmpDeletedAt = null;
            } else {
              _tmpDeletedAt = _cursor.getLong(_cursorIndexOfDeletedAt);
            }
            final int _tmpSyncState;
            _tmpSyncState = _cursor.getInt(_cursorIndexOfSyncState);
            _result = new KendaraanEntity(_tmpIdKendaraan,_tmpNamaKendaraan,_tmpJenisKendaraan,_tmpKonsumsiKmPerLiter,_tmpIsDefault,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object markSynced(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("\n");
        _stringBuilder.append("        UPDATE kendaraan");
        _stringBuilder.append("\n");
        _stringBuilder.append("        SET    sync_state = 0");
        _stringBuilder.append("\n");
        _stringBuilder.append("        WHERE  id_kendaraan IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        _stringBuilder.append("\n");
        _stringBuilder.append("        ");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

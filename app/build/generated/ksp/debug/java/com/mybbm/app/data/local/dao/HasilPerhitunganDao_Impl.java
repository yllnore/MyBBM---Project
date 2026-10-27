package com.mybbm.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mybbm.app.data.local.entity.HasilPerhitunganEntity;
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
public final class HasilPerhitunganDao_Impl implements HasilPerhitunganDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HasilPerhitunganEntity> __insertionAdapterOfHasilPerhitunganEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  public HasilPerhitunganDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHasilPerhitunganEntity = new EntityInsertionAdapter<HasilPerhitunganEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `hasil_perhitungan` (`id_hasil`,`id_perjalanan`,`id_kendaraan`,`id_bbm`,`id_harga`,`liter_dibutuhkan`,`total_biaya`,`created_at`,`updated_at`,`deleted_at`,`sync_state`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HasilPerhitunganEntity entity) {
        statement.bindString(1, entity.getIdHasil());
        statement.bindString(2, entity.getIdPerjalanan());
        statement.bindString(3, entity.getIdKendaraan());
        statement.bindString(4, entity.getIdBbm());
        statement.bindString(5, entity.getIdHarga());
        statement.bindString(6, entity.getLiterDibutuhkan());
        statement.bindString(7, entity.getTotalBiaya());
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getDeletedAt());
        }
        statement.bindLong(11, entity.getSyncState());
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE hasil_perhitungan\n"
                + "        SET    deleted_at = ?,\n"
                + "               updated_at = ?,\n"
                + "               sync_state = 1\n"
                + "        WHERE  id_hasil = ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final HasilPerhitunganEntity hasilPerhitungan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHasilPerhitunganEntity.insert(hasilPerhitungan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Flow<List<HasilPerhitunganEntity>> observeAll() {
    final String _sql = "\n"
            + "        SELECT * FROM hasil_perhitungan\n"
            + "        WHERE  deleted_at IS NULL\n"
            + "        ORDER  BY created_at DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hasil_perhitungan"}, new Callable<List<HasilPerhitunganEntity>>() {
      @Override
      @NonNull
      public List<HasilPerhitunganEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHasil = CursorUtil.getColumnIndexOrThrow(_cursor, "id_hasil");
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfLiterDibutuhkan = CursorUtil.getColumnIndexOrThrow(_cursor, "liter_dibutuhkan");
          final int _cursorIndexOfTotalBiaya = CursorUtil.getColumnIndexOrThrow(_cursor, "total_biaya");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<HasilPerhitunganEntity> _result = new ArrayList<HasilPerhitunganEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HasilPerhitunganEntity _item;
            final String _tmpIdHasil;
            _tmpIdHasil = _cursor.getString(_cursorIndexOfIdHasil);
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpLiterDibutuhkan;
            _tmpLiterDibutuhkan = _cursor.getString(_cursorIndexOfLiterDibutuhkan);
            final String _tmpTotalBiaya;
            _tmpTotalBiaya = _cursor.getString(_cursorIndexOfTotalBiaya);
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
            _item = new HasilPerhitunganEntity(_tmpIdHasil,_tmpIdPerjalanan,_tmpIdKendaraan,_tmpIdBbm,_tmpIdHarga,_tmpLiterDibutuhkan,_tmpTotalBiaya,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Flow<List<HasilPerhitunganEntity>> observeByPerjalanan(final String idPerjalanan) {
    final String _sql = "\n"
            + "        SELECT * FROM hasil_perhitungan\n"
            + "        WHERE  id_perjalanan = ?\n"
            + "          AND  deleted_at    IS NULL\n"
            + "        ORDER  BY created_at DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, idPerjalanan);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"hasil_perhitungan"}, new Callable<List<HasilPerhitunganEntity>>() {
      @Override
      @NonNull
      public List<HasilPerhitunganEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHasil = CursorUtil.getColumnIndexOrThrow(_cursor, "id_hasil");
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfLiterDibutuhkan = CursorUtil.getColumnIndexOrThrow(_cursor, "liter_dibutuhkan");
          final int _cursorIndexOfTotalBiaya = CursorUtil.getColumnIndexOrThrow(_cursor, "total_biaya");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<HasilPerhitunganEntity> _result = new ArrayList<HasilPerhitunganEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HasilPerhitunganEntity _item;
            final String _tmpIdHasil;
            _tmpIdHasil = _cursor.getString(_cursorIndexOfIdHasil);
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpLiterDibutuhkan;
            _tmpLiterDibutuhkan = _cursor.getString(_cursorIndexOfLiterDibutuhkan);
            final String _tmpTotalBiaya;
            _tmpTotalBiaya = _cursor.getString(_cursorIndexOfTotalBiaya);
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
            _item = new HasilPerhitunganEntity(_tmpIdHasil,_tmpIdPerjalanan,_tmpIdKendaraan,_tmpIdBbm,_tmpIdHarga,_tmpLiterDibutuhkan,_tmpTotalBiaya,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getById(final String id,
      final Continuation<? super HasilPerhitunganEntity> $completion) {
    final String _sql = "SELECT * FROM hasil_perhitungan WHERE id_hasil = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HasilPerhitunganEntity>() {
      @Override
      @Nullable
      public HasilPerhitunganEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHasil = CursorUtil.getColumnIndexOrThrow(_cursor, "id_hasil");
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfLiterDibutuhkan = CursorUtil.getColumnIndexOrThrow(_cursor, "liter_dibutuhkan");
          final int _cursorIndexOfTotalBiaya = CursorUtil.getColumnIndexOrThrow(_cursor, "total_biaya");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final HasilPerhitunganEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdHasil;
            _tmpIdHasil = _cursor.getString(_cursorIndexOfIdHasil);
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpLiterDibutuhkan;
            _tmpLiterDibutuhkan = _cursor.getString(_cursorIndexOfLiterDibutuhkan);
            final String _tmpTotalBiaya;
            _tmpTotalBiaya = _cursor.getString(_cursorIndexOfTotalBiaya);
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
            _result = new HasilPerhitunganEntity(_tmpIdHasil,_tmpIdPerjalanan,_tmpIdKendaraan,_tmpIdBbm,_tmpIdHarga,_tmpLiterDibutuhkan,_tmpTotalBiaya,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getPending(final Continuation<? super List<HasilPerhitunganEntity>> $completion) {
    final String _sql = "SELECT * FROM hasil_perhitungan WHERE sync_state = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<HasilPerhitunganEntity>>() {
      @Override
      @NonNull
      public List<HasilPerhitunganEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHasil = CursorUtil.getColumnIndexOrThrow(_cursor, "id_hasil");
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfIdKendaraan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_kendaraan");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfLiterDibutuhkan = CursorUtil.getColumnIndexOrThrow(_cursor, "liter_dibutuhkan");
          final int _cursorIndexOfTotalBiaya = CursorUtil.getColumnIndexOrThrow(_cursor, "total_biaya");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<HasilPerhitunganEntity> _result = new ArrayList<HasilPerhitunganEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HasilPerhitunganEntity _item;
            final String _tmpIdHasil;
            _tmpIdHasil = _cursor.getString(_cursorIndexOfIdHasil);
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpIdKendaraan;
            _tmpIdKendaraan = _cursor.getString(_cursorIndexOfIdKendaraan);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpLiterDibutuhkan;
            _tmpLiterDibutuhkan = _cursor.getString(_cursorIndexOfLiterDibutuhkan);
            final String _tmpTotalBiaya;
            _tmpTotalBiaya = _cursor.getString(_cursorIndexOfTotalBiaya);
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
            _item = new HasilPerhitunganEntity(_tmpIdHasil,_tmpIdPerjalanan,_tmpIdKendaraan,_tmpIdBbm,_tmpIdHarga,_tmpLiterDibutuhkan,_tmpTotalBiaya,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object markSynced(final List<String> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("\n");
        _stringBuilder.append("        UPDATE hasil_perhitungan");
        _stringBuilder.append("\n");
        _stringBuilder.append("        SET    sync_state = 0");
        _stringBuilder.append("\n");
        _stringBuilder.append("        WHERE  id_hasil IN (");
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

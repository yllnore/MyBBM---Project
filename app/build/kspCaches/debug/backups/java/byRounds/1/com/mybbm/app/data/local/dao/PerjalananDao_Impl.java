package com.mybbm.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mybbm.app.data.local.entity.PerjalananEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class PerjalananDao_Impl implements PerjalananDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PerjalananEntity> __insertionAdapterOfPerjalananEntity;

  private final EntityDeletionOrUpdateAdapter<PerjalananEntity> __updateAdapterOfPerjalananEntity;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  public PerjalananDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerjalananEntity = new EntityInsertionAdapter<PerjalananEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `perjalanan` (`id_perjalanan`,`label`,`asal_nama`,`tujuan_nama`,`asal_place_id`,`tujuan_place_id`,`asal_lat`,`asal_lng`,`tujuan_lat`,`tujuan_lng`,`jarak_km`,`durasi_menit`,`encoded_polyline`,`sumber_jarak`,`created_at`,`updated_at`,`deleted_at`,`sync_state`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PerjalananEntity entity) {
        statement.bindString(1, entity.getIdPerjalanan());
        if (entity.getLabel() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getLabel());
        }
        if (entity.getAsalNama() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAsalNama());
        }
        if (entity.getTujuanNama() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTujuanNama());
        }
        if (entity.getAsalPlaceId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAsalPlaceId());
        }
        if (entity.getTujuanPlaceId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getTujuanPlaceId());
        }
        statement.bindDouble(7, entity.getAsalLat());
        statement.bindDouble(8, entity.getAsalLng());
        statement.bindDouble(9, entity.getTujuanLat());
        statement.bindDouble(10, entity.getTujuanLng());
        statement.bindString(11, entity.getJarakKm());
        if (entity.getDurasiMenit() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDurasiMenit());
        }
        if (entity.getEncodedPolyline() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getEncodedPolyline());
        }
        statement.bindString(14, entity.getSumberJarak());
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getDeletedAt());
        }
        statement.bindLong(18, entity.getSyncState());
      }
    };
    this.__updateAdapterOfPerjalananEntity = new EntityDeletionOrUpdateAdapter<PerjalananEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `perjalanan` SET `id_perjalanan` = ?,`label` = ?,`asal_nama` = ?,`tujuan_nama` = ?,`asal_place_id` = ?,`tujuan_place_id` = ?,`asal_lat` = ?,`asal_lng` = ?,`tujuan_lat` = ?,`tujuan_lng` = ?,`jarak_km` = ?,`durasi_menit` = ?,`encoded_polyline` = ?,`sumber_jarak` = ?,`created_at` = ?,`updated_at` = ?,`deleted_at` = ?,`sync_state` = ? WHERE `id_perjalanan` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PerjalananEntity entity) {
        statement.bindString(1, entity.getIdPerjalanan());
        if (entity.getLabel() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getLabel());
        }
        if (entity.getAsalNama() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAsalNama());
        }
        if (entity.getTujuanNama() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTujuanNama());
        }
        if (entity.getAsalPlaceId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAsalPlaceId());
        }
        if (entity.getTujuanPlaceId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getTujuanPlaceId());
        }
        statement.bindDouble(7, entity.getAsalLat());
        statement.bindDouble(8, entity.getAsalLng());
        statement.bindDouble(9, entity.getTujuanLat());
        statement.bindDouble(10, entity.getTujuanLng());
        statement.bindString(11, entity.getJarakKm());
        if (entity.getDurasiMenit() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getDurasiMenit());
        }
        if (entity.getEncodedPolyline() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getEncodedPolyline());
        }
        statement.bindString(14, entity.getSumberJarak());
        statement.bindLong(15, entity.getCreatedAt());
        statement.bindLong(16, entity.getUpdatedAt());
        if (entity.getDeletedAt() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getDeletedAt());
        }
        statement.bindLong(18, entity.getSyncState());
        statement.bindString(19, entity.getIdPerjalanan());
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE perjalanan\n"
                + "        SET    deleted_at = ?,\n"
                + "               updated_at = ?,\n"
                + "               sync_state = 1\n"
                + "        WHERE  id_perjalanan = ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PerjalananEntity perjalanan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPerjalananEntity.insert(perjalanan);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final PerjalananEntity perjalanan,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPerjalananEntity.handle(perjalanan);
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
  public Object findCached(final double asalLat, final double asalLng, final double tujuanLat,
      final double tujuanLng, final Continuation<? super PerjalananEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM perjalanan\n"
            + "        WHERE  ROUND(asal_lat,   4) = ROUND(?,   4)\n"
            + "          AND  ROUND(asal_lng,   4) = ROUND(?,   4)\n"
            + "          AND  ROUND(tujuan_lat, 4) = ROUND(?, 4)\n"
            + "          AND  ROUND(tujuan_lng, 4) = ROUND(?, 4)\n"
            + "          AND  sumber_jarak        = 'DIRECTIONS_API'\n"
            + "          AND  deleted_at          IS NULL\n"
            + "        ORDER  BY created_at DESC\n"
            + "        LIMIT  1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindDouble(_argIndex, asalLat);
    _argIndex = 2;
    _statement.bindDouble(_argIndex, asalLng);
    _argIndex = 3;
    _statement.bindDouble(_argIndex, tujuanLat);
    _argIndex = 4;
    _statement.bindDouble(_argIndex, tujuanLng);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PerjalananEntity>() {
      @Override
      @Nullable
      public PerjalananEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfAsalNama = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_nama");
          final int _cursorIndexOfTujuanNama = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_nama");
          final int _cursorIndexOfAsalPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_place_id");
          final int _cursorIndexOfTujuanPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_place_id");
          final int _cursorIndexOfAsalLat = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lat");
          final int _cursorIndexOfAsalLng = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lng");
          final int _cursorIndexOfTujuanLat = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lat");
          final int _cursorIndexOfTujuanLng = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lng");
          final int _cursorIndexOfJarakKm = CursorUtil.getColumnIndexOrThrow(_cursor, "jarak_km");
          final int _cursorIndexOfDurasiMenit = CursorUtil.getColumnIndexOrThrow(_cursor, "durasi_menit");
          final int _cursorIndexOfEncodedPolyline = CursorUtil.getColumnIndexOrThrow(_cursor, "encoded_polyline");
          final int _cursorIndexOfSumberJarak = CursorUtil.getColumnIndexOrThrow(_cursor, "sumber_jarak");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final PerjalananEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final String _tmpAsalNama;
            if (_cursor.isNull(_cursorIndexOfAsalNama)) {
              _tmpAsalNama = null;
            } else {
              _tmpAsalNama = _cursor.getString(_cursorIndexOfAsalNama);
            }
            final String _tmpTujuanNama;
            if (_cursor.isNull(_cursorIndexOfTujuanNama)) {
              _tmpTujuanNama = null;
            } else {
              _tmpTujuanNama = _cursor.getString(_cursorIndexOfTujuanNama);
            }
            final String _tmpAsalPlaceId;
            if (_cursor.isNull(_cursorIndexOfAsalPlaceId)) {
              _tmpAsalPlaceId = null;
            } else {
              _tmpAsalPlaceId = _cursor.getString(_cursorIndexOfAsalPlaceId);
            }
            final String _tmpTujuanPlaceId;
            if (_cursor.isNull(_cursorIndexOfTujuanPlaceId)) {
              _tmpTujuanPlaceId = null;
            } else {
              _tmpTujuanPlaceId = _cursor.getString(_cursorIndexOfTujuanPlaceId);
            }
            final double _tmpAsalLat;
            _tmpAsalLat = _cursor.getDouble(_cursorIndexOfAsalLat);
            final double _tmpAsalLng;
            _tmpAsalLng = _cursor.getDouble(_cursorIndexOfAsalLng);
            final double _tmpTujuanLat;
            _tmpTujuanLat = _cursor.getDouble(_cursorIndexOfTujuanLat);
            final double _tmpTujuanLng;
            _tmpTujuanLng = _cursor.getDouble(_cursorIndexOfTujuanLng);
            final String _tmpJarakKm;
            _tmpJarakKm = _cursor.getString(_cursorIndexOfJarakKm);
            final Integer _tmpDurasiMenit;
            if (_cursor.isNull(_cursorIndexOfDurasiMenit)) {
              _tmpDurasiMenit = null;
            } else {
              _tmpDurasiMenit = _cursor.getInt(_cursorIndexOfDurasiMenit);
            }
            final String _tmpEncodedPolyline;
            if (_cursor.isNull(_cursorIndexOfEncodedPolyline)) {
              _tmpEncodedPolyline = null;
            } else {
              _tmpEncodedPolyline = _cursor.getString(_cursorIndexOfEncodedPolyline);
            }
            final String _tmpSumberJarak;
            _tmpSumberJarak = _cursor.getString(_cursorIndexOfSumberJarak);
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
            _result = new PerjalananEntity(_tmpIdPerjalanan,_tmpLabel,_tmpAsalNama,_tmpTujuanNama,_tmpAsalPlaceId,_tmpTujuanPlaceId,_tmpAsalLat,_tmpAsalLng,_tmpTujuanLat,_tmpTujuanLng,_tmpJarakKm,_tmpDurasiMenit,_tmpEncodedPolyline,_tmpSumberJarak,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getById(final String id, final Continuation<? super PerjalananEntity> $completion) {
    final String _sql = "SELECT * FROM perjalanan WHERE id_perjalanan = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PerjalananEntity>() {
      @Override
      @Nullable
      public PerjalananEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfAsalNama = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_nama");
          final int _cursorIndexOfTujuanNama = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_nama");
          final int _cursorIndexOfAsalPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_place_id");
          final int _cursorIndexOfTujuanPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_place_id");
          final int _cursorIndexOfAsalLat = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lat");
          final int _cursorIndexOfAsalLng = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lng");
          final int _cursorIndexOfTujuanLat = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lat");
          final int _cursorIndexOfTujuanLng = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lng");
          final int _cursorIndexOfJarakKm = CursorUtil.getColumnIndexOrThrow(_cursor, "jarak_km");
          final int _cursorIndexOfDurasiMenit = CursorUtil.getColumnIndexOrThrow(_cursor, "durasi_menit");
          final int _cursorIndexOfEncodedPolyline = CursorUtil.getColumnIndexOrThrow(_cursor, "encoded_polyline");
          final int _cursorIndexOfSumberJarak = CursorUtil.getColumnIndexOrThrow(_cursor, "sumber_jarak");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final PerjalananEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final String _tmpAsalNama;
            if (_cursor.isNull(_cursorIndexOfAsalNama)) {
              _tmpAsalNama = null;
            } else {
              _tmpAsalNama = _cursor.getString(_cursorIndexOfAsalNama);
            }
            final String _tmpTujuanNama;
            if (_cursor.isNull(_cursorIndexOfTujuanNama)) {
              _tmpTujuanNama = null;
            } else {
              _tmpTujuanNama = _cursor.getString(_cursorIndexOfTujuanNama);
            }
            final String _tmpAsalPlaceId;
            if (_cursor.isNull(_cursorIndexOfAsalPlaceId)) {
              _tmpAsalPlaceId = null;
            } else {
              _tmpAsalPlaceId = _cursor.getString(_cursorIndexOfAsalPlaceId);
            }
            final String _tmpTujuanPlaceId;
            if (_cursor.isNull(_cursorIndexOfTujuanPlaceId)) {
              _tmpTujuanPlaceId = null;
            } else {
              _tmpTujuanPlaceId = _cursor.getString(_cursorIndexOfTujuanPlaceId);
            }
            final double _tmpAsalLat;
            _tmpAsalLat = _cursor.getDouble(_cursorIndexOfAsalLat);
            final double _tmpAsalLng;
            _tmpAsalLng = _cursor.getDouble(_cursorIndexOfAsalLng);
            final double _tmpTujuanLat;
            _tmpTujuanLat = _cursor.getDouble(_cursorIndexOfTujuanLat);
            final double _tmpTujuanLng;
            _tmpTujuanLng = _cursor.getDouble(_cursorIndexOfTujuanLng);
            final String _tmpJarakKm;
            _tmpJarakKm = _cursor.getString(_cursorIndexOfJarakKm);
            final Integer _tmpDurasiMenit;
            if (_cursor.isNull(_cursorIndexOfDurasiMenit)) {
              _tmpDurasiMenit = null;
            } else {
              _tmpDurasiMenit = _cursor.getInt(_cursorIndexOfDurasiMenit);
            }
            final String _tmpEncodedPolyline;
            if (_cursor.isNull(_cursorIndexOfEncodedPolyline)) {
              _tmpEncodedPolyline = null;
            } else {
              _tmpEncodedPolyline = _cursor.getString(_cursorIndexOfEncodedPolyline);
            }
            final String _tmpSumberJarak;
            _tmpSumberJarak = _cursor.getString(_cursorIndexOfSumberJarak);
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
            _result = new PerjalananEntity(_tmpIdPerjalanan,_tmpLabel,_tmpAsalNama,_tmpTujuanNama,_tmpAsalPlaceId,_tmpTujuanPlaceId,_tmpAsalLat,_tmpAsalLng,_tmpTujuanLat,_tmpTujuanLng,_tmpJarakKm,_tmpDurasiMenit,_tmpEncodedPolyline,_tmpSumberJarak,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Flow<List<PerjalananEntity>> observeAll() {
    final String _sql = "\n"
            + "        SELECT * FROM perjalanan\n"
            + "        WHERE  deleted_at IS NULL\n"
            + "        ORDER  BY created_at DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"perjalanan"}, new Callable<List<PerjalananEntity>>() {
      @Override
      @NonNull
      public List<PerjalananEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfAsalNama = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_nama");
          final int _cursorIndexOfTujuanNama = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_nama");
          final int _cursorIndexOfAsalPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_place_id");
          final int _cursorIndexOfTujuanPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_place_id");
          final int _cursorIndexOfAsalLat = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lat");
          final int _cursorIndexOfAsalLng = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lng");
          final int _cursorIndexOfTujuanLat = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lat");
          final int _cursorIndexOfTujuanLng = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lng");
          final int _cursorIndexOfJarakKm = CursorUtil.getColumnIndexOrThrow(_cursor, "jarak_km");
          final int _cursorIndexOfDurasiMenit = CursorUtil.getColumnIndexOrThrow(_cursor, "durasi_menit");
          final int _cursorIndexOfEncodedPolyline = CursorUtil.getColumnIndexOrThrow(_cursor, "encoded_polyline");
          final int _cursorIndexOfSumberJarak = CursorUtil.getColumnIndexOrThrow(_cursor, "sumber_jarak");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<PerjalananEntity> _result = new ArrayList<PerjalananEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PerjalananEntity _item;
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final String _tmpAsalNama;
            if (_cursor.isNull(_cursorIndexOfAsalNama)) {
              _tmpAsalNama = null;
            } else {
              _tmpAsalNama = _cursor.getString(_cursorIndexOfAsalNama);
            }
            final String _tmpTujuanNama;
            if (_cursor.isNull(_cursorIndexOfTujuanNama)) {
              _tmpTujuanNama = null;
            } else {
              _tmpTujuanNama = _cursor.getString(_cursorIndexOfTujuanNama);
            }
            final String _tmpAsalPlaceId;
            if (_cursor.isNull(_cursorIndexOfAsalPlaceId)) {
              _tmpAsalPlaceId = null;
            } else {
              _tmpAsalPlaceId = _cursor.getString(_cursorIndexOfAsalPlaceId);
            }
            final String _tmpTujuanPlaceId;
            if (_cursor.isNull(_cursorIndexOfTujuanPlaceId)) {
              _tmpTujuanPlaceId = null;
            } else {
              _tmpTujuanPlaceId = _cursor.getString(_cursorIndexOfTujuanPlaceId);
            }
            final double _tmpAsalLat;
            _tmpAsalLat = _cursor.getDouble(_cursorIndexOfAsalLat);
            final double _tmpAsalLng;
            _tmpAsalLng = _cursor.getDouble(_cursorIndexOfAsalLng);
            final double _tmpTujuanLat;
            _tmpTujuanLat = _cursor.getDouble(_cursorIndexOfTujuanLat);
            final double _tmpTujuanLng;
            _tmpTujuanLng = _cursor.getDouble(_cursorIndexOfTujuanLng);
            final String _tmpJarakKm;
            _tmpJarakKm = _cursor.getString(_cursorIndexOfJarakKm);
            final Integer _tmpDurasiMenit;
            if (_cursor.isNull(_cursorIndexOfDurasiMenit)) {
              _tmpDurasiMenit = null;
            } else {
              _tmpDurasiMenit = _cursor.getInt(_cursorIndexOfDurasiMenit);
            }
            final String _tmpEncodedPolyline;
            if (_cursor.isNull(_cursorIndexOfEncodedPolyline)) {
              _tmpEncodedPolyline = null;
            } else {
              _tmpEncodedPolyline = _cursor.getString(_cursorIndexOfEncodedPolyline);
            }
            final String _tmpSumberJarak;
            _tmpSumberJarak = _cursor.getString(_cursorIndexOfSumberJarak);
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
            _item = new PerjalananEntity(_tmpIdPerjalanan,_tmpLabel,_tmpAsalNama,_tmpTujuanNama,_tmpAsalPlaceId,_tmpTujuanPlaceId,_tmpAsalLat,_tmpAsalLng,_tmpTujuanLat,_tmpTujuanLng,_tmpJarakKm,_tmpDurasiMenit,_tmpEncodedPolyline,_tmpSumberJarak,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
  public Object getPending(final Continuation<? super List<PerjalananEntity>> $completion) {
    final String _sql = "SELECT * FROM perjalanan WHERE sync_state = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PerjalananEntity>>() {
      @Override
      @NonNull
      public List<PerjalananEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdPerjalanan = CursorUtil.getColumnIndexOrThrow(_cursor, "id_perjalanan");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfAsalNama = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_nama");
          final int _cursorIndexOfTujuanNama = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_nama");
          final int _cursorIndexOfAsalPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_place_id");
          final int _cursorIndexOfTujuanPlaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_place_id");
          final int _cursorIndexOfAsalLat = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lat");
          final int _cursorIndexOfAsalLng = CursorUtil.getColumnIndexOrThrow(_cursor, "asal_lng");
          final int _cursorIndexOfTujuanLat = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lat");
          final int _cursorIndexOfTujuanLng = CursorUtil.getColumnIndexOrThrow(_cursor, "tujuan_lng");
          final int _cursorIndexOfJarakKm = CursorUtil.getColumnIndexOrThrow(_cursor, "jarak_km");
          final int _cursorIndexOfDurasiMenit = CursorUtil.getColumnIndexOrThrow(_cursor, "durasi_menit");
          final int _cursorIndexOfEncodedPolyline = CursorUtil.getColumnIndexOrThrow(_cursor, "encoded_polyline");
          final int _cursorIndexOfSumberJarak = CursorUtil.getColumnIndexOrThrow(_cursor, "sumber_jarak");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final int _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_at");
          final int _cursorIndexOfSyncState = CursorUtil.getColumnIndexOrThrow(_cursor, "sync_state");
          final List<PerjalananEntity> _result = new ArrayList<PerjalananEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PerjalananEntity _item;
            final String _tmpIdPerjalanan;
            _tmpIdPerjalanan = _cursor.getString(_cursorIndexOfIdPerjalanan);
            final String _tmpLabel;
            if (_cursor.isNull(_cursorIndexOfLabel)) {
              _tmpLabel = null;
            } else {
              _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            }
            final String _tmpAsalNama;
            if (_cursor.isNull(_cursorIndexOfAsalNama)) {
              _tmpAsalNama = null;
            } else {
              _tmpAsalNama = _cursor.getString(_cursorIndexOfAsalNama);
            }
            final String _tmpTujuanNama;
            if (_cursor.isNull(_cursorIndexOfTujuanNama)) {
              _tmpTujuanNama = null;
            } else {
              _tmpTujuanNama = _cursor.getString(_cursorIndexOfTujuanNama);
            }
            final String _tmpAsalPlaceId;
            if (_cursor.isNull(_cursorIndexOfAsalPlaceId)) {
              _tmpAsalPlaceId = null;
            } else {
              _tmpAsalPlaceId = _cursor.getString(_cursorIndexOfAsalPlaceId);
            }
            final String _tmpTujuanPlaceId;
            if (_cursor.isNull(_cursorIndexOfTujuanPlaceId)) {
              _tmpTujuanPlaceId = null;
            } else {
              _tmpTujuanPlaceId = _cursor.getString(_cursorIndexOfTujuanPlaceId);
            }
            final double _tmpAsalLat;
            _tmpAsalLat = _cursor.getDouble(_cursorIndexOfAsalLat);
            final double _tmpAsalLng;
            _tmpAsalLng = _cursor.getDouble(_cursorIndexOfAsalLng);
            final double _tmpTujuanLat;
            _tmpTujuanLat = _cursor.getDouble(_cursorIndexOfTujuanLat);
            final double _tmpTujuanLng;
            _tmpTujuanLng = _cursor.getDouble(_cursorIndexOfTujuanLng);
            final String _tmpJarakKm;
            _tmpJarakKm = _cursor.getString(_cursorIndexOfJarakKm);
            final Integer _tmpDurasiMenit;
            if (_cursor.isNull(_cursorIndexOfDurasiMenit)) {
              _tmpDurasiMenit = null;
            } else {
              _tmpDurasiMenit = _cursor.getInt(_cursorIndexOfDurasiMenit);
            }
            final String _tmpEncodedPolyline;
            if (_cursor.isNull(_cursorIndexOfEncodedPolyline)) {
              _tmpEncodedPolyline = null;
            } else {
              _tmpEncodedPolyline = _cursor.getString(_cursorIndexOfEncodedPolyline);
            }
            final String _tmpSumberJarak;
            _tmpSumberJarak = _cursor.getString(_cursorIndexOfSumberJarak);
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
            _item = new PerjalananEntity(_tmpIdPerjalanan,_tmpLabel,_tmpAsalNama,_tmpTujuanNama,_tmpAsalPlaceId,_tmpTujuanPlaceId,_tmpAsalLat,_tmpAsalLng,_tmpTujuanLat,_tmpTujuanLng,_tmpJarakKm,_tmpDurasiMenit,_tmpEncodedPolyline,_tmpSumberJarak,_tmpCreatedAt,_tmpUpdatedAt,_tmpDeletedAt,_tmpSyncState);
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
        _stringBuilder.append("        UPDATE perjalanan");
        _stringBuilder.append("\n");
        _stringBuilder.append("        SET    sync_state = 0");
        _stringBuilder.append("\n");
        _stringBuilder.append("        WHERE  id_perjalanan IN (");
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

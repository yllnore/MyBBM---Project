package com.mybbm.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mybbm.app.data.local.entity.HargaBbmEntity;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HargaBbmDao_Impl implements HargaBbmDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HargaBbmEntity> __insertionAdapterOfHargaBbmEntity;

  public HargaBbmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHargaBbmEntity = new EntityInsertionAdapter<HargaBbmEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `harga_bbm` (`id_harga`,`id_bbm`,`wilayah`,`harga_per_liter`,`berlaku_mulai`,`berlaku_hingga`,`updated_at`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HargaBbmEntity entity) {
        statement.bindString(1, entity.getIdHarga());
        statement.bindString(2, entity.getIdBbm());
        statement.bindString(3, entity.getWilayah());
        statement.bindString(4, entity.getHargaPerLiter());
        statement.bindLong(5, entity.getBerlakuMulai());
        if (entity.getBerlakuHingga() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getBerlakuHingga());
        }
        statement.bindLong(7, entity.getUpdatedAt());
      }
    };
  }

  @Override
  public Object insertAll(final List<HargaBbmEntity> hargaBbm,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHargaBbmEntity.insert(hargaBbm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object hargaTerkini(final String idBbm, final String wilayah, final long tanggal,
      final Continuation<? super HargaBbmEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM harga_bbm\n"
            + "        WHERE  id_bbm       = ?\n"
            + "          AND  wilayah      = ?\n"
            + "          AND  berlaku_mulai <= ?\n"
            + "        ORDER  BY berlaku_mulai DESC\n"
            + "        LIMIT  1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, idBbm);
    _argIndex = 2;
    _statement.bindString(_argIndex, wilayah);
    _argIndex = 3;
    _statement.bindLong(_argIndex, tanggal);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<HargaBbmEntity>() {
      @Override
      @Nullable
      public HargaBbmEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfWilayah = CursorUtil.getColumnIndexOrThrow(_cursor, "wilayah");
          final int _cursorIndexOfHargaPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "harga_per_liter");
          final int _cursorIndexOfBerlakuMulai = CursorUtil.getColumnIndexOrThrow(_cursor, "berlaku_mulai");
          final int _cursorIndexOfBerlakuHingga = CursorUtil.getColumnIndexOrThrow(_cursor, "berlaku_hingga");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final HargaBbmEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpWilayah;
            _tmpWilayah = _cursor.getString(_cursorIndexOfWilayah);
            final String _tmpHargaPerLiter;
            _tmpHargaPerLiter = _cursor.getString(_cursorIndexOfHargaPerLiter);
            final long _tmpBerlakuMulai;
            _tmpBerlakuMulai = _cursor.getLong(_cursorIndexOfBerlakuMulai);
            final Long _tmpBerlakuHingga;
            if (_cursor.isNull(_cursorIndexOfBerlakuHingga)) {
              _tmpBerlakuHingga = null;
            } else {
              _tmpBerlakuHingga = _cursor.getLong(_cursorIndexOfBerlakuHingga);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new HargaBbmEntity(_tmpIdHarga,_tmpIdBbm,_tmpWilayah,_tmpHargaPerLiter,_tmpBerlakuMulai,_tmpBerlakuHingga,_tmpUpdatedAt);
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
  public Object getMaxUpdatedAt(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(updated_at) FROM harga_bbm";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
  public Flow<List<HargaBbmEntity>> observeByBbm(final String idBbm) {
    final String _sql = "\n"
            + "        SELECT * FROM harga_bbm\n"
            + "        WHERE  id_bbm = ?\n"
            + "        ORDER  BY berlaku_mulai DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, idBbm);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"harga_bbm"}, new Callable<List<HargaBbmEntity>>() {
      @Override
      @NonNull
      public List<HargaBbmEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfWilayah = CursorUtil.getColumnIndexOrThrow(_cursor, "wilayah");
          final int _cursorIndexOfHargaPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "harga_per_liter");
          final int _cursorIndexOfBerlakuMulai = CursorUtil.getColumnIndexOrThrow(_cursor, "berlaku_mulai");
          final int _cursorIndexOfBerlakuHingga = CursorUtil.getColumnIndexOrThrow(_cursor, "berlaku_hingga");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<HargaBbmEntity> _result = new ArrayList<HargaBbmEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HargaBbmEntity _item;
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpWilayah;
            _tmpWilayah = _cursor.getString(_cursorIndexOfWilayah);
            final String _tmpHargaPerLiter;
            _tmpHargaPerLiter = _cursor.getString(_cursorIndexOfHargaPerLiter);
            final long _tmpBerlakuMulai;
            _tmpBerlakuMulai = _cursor.getLong(_cursorIndexOfBerlakuMulai);
            final Long _tmpBerlakuHingga;
            if (_cursor.isNull(_cursorIndexOfBerlakuHingga)) {
              _tmpBerlakuHingga = null;
            } else {
              _tmpBerlakuHingga = _cursor.getLong(_cursorIndexOfBerlakuHingga);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new HargaBbmEntity(_tmpIdHarga,_tmpIdBbm,_tmpWilayah,_tmpHargaPerLiter,_tmpBerlakuMulai,_tmpBerlakuHingga,_tmpUpdatedAt);
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
  public Flow<List<HargaBbmEntity>> observeHargaAktif(final String wilayah) {
    final String _sql = "\n"
            + "        SELECT * FROM harga_bbm\n"
            + "        WHERE  wilayah        = ?\n"
            + "          AND  berlaku_hingga IS NULL\n"
            + "        ORDER  BY berlaku_mulai DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, wilayah);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"harga_bbm"}, new Callable<List<HargaBbmEntity>>() {
      @Override
      @NonNull
      public List<HargaBbmEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdHarga = CursorUtil.getColumnIndexOrThrow(_cursor, "id_harga");
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfWilayah = CursorUtil.getColumnIndexOrThrow(_cursor, "wilayah");
          final int _cursorIndexOfHargaPerLiter = CursorUtil.getColumnIndexOrThrow(_cursor, "harga_per_liter");
          final int _cursorIndexOfBerlakuMulai = CursorUtil.getColumnIndexOrThrow(_cursor, "berlaku_mulai");
          final int _cursorIndexOfBerlakuHingga = CursorUtil.getColumnIndexOrThrow(_cursor, "berlaku_hingga");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<HargaBbmEntity> _result = new ArrayList<HargaBbmEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final HargaBbmEntity _item;
            final String _tmpIdHarga;
            _tmpIdHarga = _cursor.getString(_cursorIndexOfIdHarga);
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpWilayah;
            _tmpWilayah = _cursor.getString(_cursorIndexOfWilayah);
            final String _tmpHargaPerLiter;
            _tmpHargaPerLiter = _cursor.getString(_cursorIndexOfHargaPerLiter);
            final long _tmpBerlakuMulai;
            _tmpBerlakuMulai = _cursor.getLong(_cursorIndexOfBerlakuMulai);
            final Long _tmpBerlakuHingga;
            if (_cursor.isNull(_cursorIndexOfBerlakuHingga)) {
              _tmpBerlakuHingga = null;
            } else {
              _tmpBerlakuHingga = _cursor.getLong(_cursorIndexOfBerlakuHingga);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new HargaBbmEntity(_tmpIdHarga,_tmpIdBbm,_tmpWilayah,_tmpHargaPerLiter,_tmpBerlakuMulai,_tmpBerlakuHingga,_tmpUpdatedAt);
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

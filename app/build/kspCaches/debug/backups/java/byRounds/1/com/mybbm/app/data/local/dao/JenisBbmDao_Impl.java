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
import com.mybbm.app.data.local.entity.JenisBbmEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class JenisBbmDao_Impl implements JenisBbmDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<JenisBbmEntity> __insertionAdapterOfJenisBbmEntity;

  public JenisBbmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfJenisBbmEntity = new EntityInsertionAdapter<JenisBbmEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `jenis_bbm` (`id_bbm`,`nama_bbm`,`jenis_bahan_bakar`,`is_active`,`updated_at`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final JenisBbmEntity entity) {
        statement.bindString(1, entity.getIdBbm());
        statement.bindString(2, entity.getNamaBbm());
        statement.bindString(3, entity.getJenisBahanBakar());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getUpdatedAt());
      }
    };
  }

  @Override
  public Object insertAll(final List<JenisBbmEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfJenisBbmEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<JenisBbmEntity>> observeActive() {
    final String _sql = "SELECT * FROM jenis_bbm WHERE is_active = 1 ORDER BY nama_bbm ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"jenis_bbm"}, new Callable<List<JenisBbmEntity>>() {
      @Override
      @NonNull
      public List<JenisBbmEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfNamaBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_bbm");
          final int _cursorIndexOfJenisBahanBakar = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_bahan_bakar");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<JenisBbmEntity> _result = new ArrayList<JenisBbmEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final JenisBbmEntity _item;
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpNamaBbm;
            _tmpNamaBbm = _cursor.getString(_cursorIndexOfNamaBbm);
            final String _tmpJenisBahanBakar;
            _tmpJenisBahanBakar = _cursor.getString(_cursorIndexOfJenisBahanBakar);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new JenisBbmEntity(_tmpIdBbm,_tmpNamaBbm,_tmpJenisBahanBakar,_tmpIsActive,_tmpUpdatedAt);
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
  public Object getActive(final Continuation<? super List<JenisBbmEntity>> $completion) {
    final String _sql = "SELECT * FROM jenis_bbm WHERE is_active = 1 ORDER BY nama_bbm ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<JenisBbmEntity>>() {
      @Override
      @NonNull
      public List<JenisBbmEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfNamaBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_bbm");
          final int _cursorIndexOfJenisBahanBakar = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_bahan_bakar");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final List<JenisBbmEntity> _result = new ArrayList<JenisBbmEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final JenisBbmEntity _item;
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpNamaBbm;
            _tmpNamaBbm = _cursor.getString(_cursorIndexOfNamaBbm);
            final String _tmpJenisBahanBakar;
            _tmpJenisBahanBakar = _cursor.getString(_cursorIndexOfJenisBahanBakar);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new JenisBbmEntity(_tmpIdBbm,_tmpNamaBbm,_tmpJenisBahanBakar,_tmpIsActive,_tmpUpdatedAt);
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
  public Object getById(final String id, final Continuation<? super JenisBbmEntity> $completion) {
    final String _sql = "SELECT * FROM jenis_bbm WHERE id_bbm = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<JenisBbmEntity>() {
      @Override
      @Nullable
      public JenisBbmEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIdBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "id_bbm");
          final int _cursorIndexOfNamaBbm = CursorUtil.getColumnIndexOrThrow(_cursor, "nama_bbm");
          final int _cursorIndexOfJenisBahanBakar = CursorUtil.getColumnIndexOrThrow(_cursor, "jenis_bahan_bakar");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "is_active");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updated_at");
          final JenisBbmEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIdBbm;
            _tmpIdBbm = _cursor.getString(_cursorIndexOfIdBbm);
            final String _tmpNamaBbm;
            _tmpNamaBbm = _cursor.getString(_cursorIndexOfNamaBbm);
            final String _tmpJenisBahanBakar;
            _tmpJenisBahanBakar = _cursor.getString(_cursorIndexOfJenisBahanBakar);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new JenisBbmEntity(_tmpIdBbm,_tmpNamaBbm,_tmpJenisBahanBakar,_tmpIsActive,_tmpUpdatedAt);
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

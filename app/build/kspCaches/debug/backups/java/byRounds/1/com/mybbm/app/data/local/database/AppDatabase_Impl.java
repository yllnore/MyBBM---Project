package com.mybbm.app.data.local.database;

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
import com.mybbm.app.data.local.dao.DeviceInfoDao;
import com.mybbm.app.data.local.dao.DeviceInfoDao_Impl;
import com.mybbm.app.data.local.dao.HargaBbmDao;
import com.mybbm.app.data.local.dao.HargaBbmDao_Impl;
import com.mybbm.app.data.local.dao.HasilPerhitunganDao;
import com.mybbm.app.data.local.dao.HasilPerhitunganDao_Impl;
import com.mybbm.app.data.local.dao.JenisBbmDao;
import com.mybbm.app.data.local.dao.JenisBbmDao_Impl;
import com.mybbm.app.data.local.dao.KendaraanDao;
import com.mybbm.app.data.local.dao.KendaraanDao_Impl;
import com.mybbm.app.data.local.dao.PerjalananDao;
import com.mybbm.app.data.local.dao.PerjalananDao_Impl;
import com.mybbm.app.data.local.dao.SyncMetaDao;
import com.mybbm.app.data.local.dao.SyncMetaDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile KendaraanDao _kendaraanDao;

  private volatile JenisBbmDao _jenisBbmDao;

  private volatile HargaBbmDao _hargaBbmDao;

  private volatile PerjalananDao _perjalananDao;

  private volatile HasilPerhitunganDao _hasilPerhitunganDao;

  private volatile DeviceInfoDao _deviceInfoDao;

  private volatile SyncMetaDao _syncMetaDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `device_info` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `jenis_bbm` (`id_bbm` TEXT NOT NULL, `nama_bbm` TEXT NOT NULL, `jenis_bahan_bakar` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id_bbm`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `harga_bbm` (`id_harga` TEXT NOT NULL, `id_bbm` TEXT NOT NULL, `wilayah` TEXT NOT NULL, `harga_per_liter` TEXT NOT NULL, `berlaku_mulai` INTEGER NOT NULL, `berlaku_hingga` INTEGER, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id_harga`), FOREIGN KEY(`id_bbm`) REFERENCES `jenis_bbm`(`id_bbm`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_harga_bbm_id_bbm_wilayah_berlaku_mulai` ON `harga_bbm` (`id_bbm`, `wilayah`, `berlaku_mulai`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_harga_bbm_id_bbm` ON `harga_bbm` (`id_bbm`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_harga_bbm_updated_at` ON `harga_bbm` (`updated_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `kendaraan` (`id_kendaraan` TEXT NOT NULL, `nama_kendaraan` TEXT NOT NULL, `jenis_kendaraan` TEXT NOT NULL, `konsumsi_km_per_liter` TEXT NOT NULL, `is_default` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `deleted_at` INTEGER, `sync_state` INTEGER NOT NULL, PRIMARY KEY(`id_kendaraan`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_kendaraan_updated_at` ON `kendaraan` (`updated_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `perjalanan` (`id_perjalanan` TEXT NOT NULL, `label` TEXT, `asal_nama` TEXT, `tujuan_nama` TEXT, `asal_place_id` TEXT, `tujuan_place_id` TEXT, `asal_lat` REAL NOT NULL, `asal_lng` REAL NOT NULL, `tujuan_lat` REAL NOT NULL, `tujuan_lng` REAL NOT NULL, `jarak_km` TEXT NOT NULL, `durasi_menit` INTEGER, `encoded_polyline` TEXT, `sumber_jarak` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `deleted_at` INTEGER, `sync_state` INTEGER NOT NULL, PRIMARY KEY(`id_perjalanan`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_perjalanan_updated_at` ON `perjalanan` (`updated_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `hasil_perhitungan` (`id_hasil` TEXT NOT NULL, `id_perjalanan` TEXT NOT NULL, `id_kendaraan` TEXT NOT NULL, `id_bbm` TEXT NOT NULL, `id_harga` TEXT NOT NULL, `liter_dibutuhkan` TEXT NOT NULL, `total_biaya` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `deleted_at` INTEGER, `sync_state` INTEGER NOT NULL, PRIMARY KEY(`id_hasil`), FOREIGN KEY(`id_perjalanan`) REFERENCES `perjalanan`(`id_perjalanan`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`id_kendaraan`) REFERENCES `kendaraan`(`id_kendaraan`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`id_bbm`) REFERENCES `jenis_bbm`(`id_bbm`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`id_harga`) REFERENCES `harga_bbm`(`id_harga`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hasil_perhitungan_id_perjalanan` ON `hasil_perhitungan` (`id_perjalanan`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hasil_perhitungan_id_kendaraan` ON `hasil_perhitungan` (`id_kendaraan`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hasil_perhitungan_id_bbm` ON `hasil_perhitungan` (`id_bbm`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hasil_perhitungan_id_harga` ON `hasil_perhitungan` (`id_harga`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hasil_perhitungan_updated_at` ON `hasil_perhitungan` (`updated_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_meta` (`key` TEXT NOT NULL, `value` INTEGER NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2c6f043f43401fd029db945bd65ce5d4')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `device_info`");
        db.execSQL("DROP TABLE IF EXISTS `jenis_bbm`");
        db.execSQL("DROP TABLE IF EXISTS `harga_bbm`");
        db.execSQL("DROP TABLE IF EXISTS `kendaraan`");
        db.execSQL("DROP TABLE IF EXISTS `perjalanan`");
        db.execSQL("DROP TABLE IF EXISTS `hasil_perhitungan`");
        db.execSQL("DROP TABLE IF EXISTS `sync_meta`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsDeviceInfo = new HashMap<String, TableInfo.Column>(2);
        _columnsDeviceInfo.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeviceInfo.put("value", new TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeviceInfo = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeviceInfo = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeviceInfo = new TableInfo("device_info", _columnsDeviceInfo, _foreignKeysDeviceInfo, _indicesDeviceInfo);
        final TableInfo _existingDeviceInfo = TableInfo.read(db, "device_info");
        if (!_infoDeviceInfo.equals(_existingDeviceInfo)) {
          return new RoomOpenHelper.ValidationResult(false, "device_info(com.mybbm.app.data.local.entity.DeviceInfoEntity).\n"
                  + " Expected:\n" + _infoDeviceInfo + "\n"
                  + " Found:\n" + _existingDeviceInfo);
        }
        final HashMap<String, TableInfo.Column> _columnsJenisBbm = new HashMap<String, TableInfo.Column>(5);
        _columnsJenisBbm.put("id_bbm", new TableInfo.Column("id_bbm", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJenisBbm.put("nama_bbm", new TableInfo.Column("nama_bbm", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJenisBbm.put("jenis_bahan_bakar", new TableInfo.Column("jenis_bahan_bakar", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJenisBbm.put("is_active", new TableInfo.Column("is_active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsJenisBbm.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysJenisBbm = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesJenisBbm = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoJenisBbm = new TableInfo("jenis_bbm", _columnsJenisBbm, _foreignKeysJenisBbm, _indicesJenisBbm);
        final TableInfo _existingJenisBbm = TableInfo.read(db, "jenis_bbm");
        if (!_infoJenisBbm.equals(_existingJenisBbm)) {
          return new RoomOpenHelper.ValidationResult(false, "jenis_bbm(com.mybbm.app.data.local.entity.JenisBbmEntity).\n"
                  + " Expected:\n" + _infoJenisBbm + "\n"
                  + " Found:\n" + _existingJenisBbm);
        }
        final HashMap<String, TableInfo.Column> _columnsHargaBbm = new HashMap<String, TableInfo.Column>(7);
        _columnsHargaBbm.put("id_harga", new TableInfo.Column("id_harga", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHargaBbm.put("id_bbm", new TableInfo.Column("id_bbm", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHargaBbm.put("wilayah", new TableInfo.Column("wilayah", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHargaBbm.put("harga_per_liter", new TableInfo.Column("harga_per_liter", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHargaBbm.put("berlaku_mulai", new TableInfo.Column("berlaku_mulai", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHargaBbm.put("berlaku_hingga", new TableInfo.Column("berlaku_hingga", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHargaBbm.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHargaBbm = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysHargaBbm.add(new TableInfo.ForeignKey("jenis_bbm", "CASCADE", "NO ACTION", Arrays.asList("id_bbm"), Arrays.asList("id_bbm")));
        final HashSet<TableInfo.Index> _indicesHargaBbm = new HashSet<TableInfo.Index>(3);
        _indicesHargaBbm.add(new TableInfo.Index("index_harga_bbm_id_bbm_wilayah_berlaku_mulai", true, Arrays.asList("id_bbm", "wilayah", "berlaku_mulai"), Arrays.asList("ASC", "ASC", "ASC")));
        _indicesHargaBbm.add(new TableInfo.Index("index_harga_bbm_id_bbm", false, Arrays.asList("id_bbm"), Arrays.asList("ASC")));
        _indicesHargaBbm.add(new TableInfo.Index("index_harga_bbm_updated_at", false, Arrays.asList("updated_at"), Arrays.asList("ASC")));
        final TableInfo _infoHargaBbm = new TableInfo("harga_bbm", _columnsHargaBbm, _foreignKeysHargaBbm, _indicesHargaBbm);
        final TableInfo _existingHargaBbm = TableInfo.read(db, "harga_bbm");
        if (!_infoHargaBbm.equals(_existingHargaBbm)) {
          return new RoomOpenHelper.ValidationResult(false, "harga_bbm(com.mybbm.app.data.local.entity.HargaBbmEntity).\n"
                  + " Expected:\n" + _infoHargaBbm + "\n"
                  + " Found:\n" + _existingHargaBbm);
        }
        final HashMap<String, TableInfo.Column> _columnsKendaraan = new HashMap<String, TableInfo.Column>(9);
        _columnsKendaraan.put("id_kendaraan", new TableInfo.Column("id_kendaraan", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("nama_kendaraan", new TableInfo.Column("nama_kendaraan", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("jenis_kendaraan", new TableInfo.Column("jenis_kendaraan", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("konsumsi_km_per_liter", new TableInfo.Column("konsumsi_km_per_liter", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("is_default", new TableInfo.Column("is_default", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsKendaraan.put("sync_state", new TableInfo.Column("sync_state", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysKendaraan = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesKendaraan = new HashSet<TableInfo.Index>(1);
        _indicesKendaraan.add(new TableInfo.Index("index_kendaraan_updated_at", false, Arrays.asList("updated_at"), Arrays.asList("ASC")));
        final TableInfo _infoKendaraan = new TableInfo("kendaraan", _columnsKendaraan, _foreignKeysKendaraan, _indicesKendaraan);
        final TableInfo _existingKendaraan = TableInfo.read(db, "kendaraan");
        if (!_infoKendaraan.equals(_existingKendaraan)) {
          return new RoomOpenHelper.ValidationResult(false, "kendaraan(com.mybbm.app.data.local.entity.KendaraanEntity).\n"
                  + " Expected:\n" + _infoKendaraan + "\n"
                  + " Found:\n" + _existingKendaraan);
        }
        final HashMap<String, TableInfo.Column> _columnsPerjalanan = new HashMap<String, TableInfo.Column>(18);
        _columnsPerjalanan.put("id_perjalanan", new TableInfo.Column("id_perjalanan", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("label", new TableInfo.Column("label", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("asal_nama", new TableInfo.Column("asal_nama", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("tujuan_nama", new TableInfo.Column("tujuan_nama", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("asal_place_id", new TableInfo.Column("asal_place_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("tujuan_place_id", new TableInfo.Column("tujuan_place_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("asal_lat", new TableInfo.Column("asal_lat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("asal_lng", new TableInfo.Column("asal_lng", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("tujuan_lat", new TableInfo.Column("tujuan_lat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("tujuan_lng", new TableInfo.Column("tujuan_lng", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("jarak_km", new TableInfo.Column("jarak_km", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("durasi_menit", new TableInfo.Column("durasi_menit", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("encoded_polyline", new TableInfo.Column("encoded_polyline", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("sumber_jarak", new TableInfo.Column("sumber_jarak", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerjalanan.put("sync_state", new TableInfo.Column("sync_state", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPerjalanan = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPerjalanan = new HashSet<TableInfo.Index>(1);
        _indicesPerjalanan.add(new TableInfo.Index("index_perjalanan_updated_at", false, Arrays.asList("updated_at"), Arrays.asList("ASC")));
        final TableInfo _infoPerjalanan = new TableInfo("perjalanan", _columnsPerjalanan, _foreignKeysPerjalanan, _indicesPerjalanan);
        final TableInfo _existingPerjalanan = TableInfo.read(db, "perjalanan");
        if (!_infoPerjalanan.equals(_existingPerjalanan)) {
          return new RoomOpenHelper.ValidationResult(false, "perjalanan(com.mybbm.app.data.local.entity.PerjalananEntity).\n"
                  + " Expected:\n" + _infoPerjalanan + "\n"
                  + " Found:\n" + _existingPerjalanan);
        }
        final HashMap<String, TableInfo.Column> _columnsHasilPerhitungan = new HashMap<String, TableInfo.Column>(11);
        _columnsHasilPerhitungan.put("id_hasil", new TableInfo.Column("id_hasil", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("id_perjalanan", new TableInfo.Column("id_perjalanan", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("id_kendaraan", new TableInfo.Column("id_kendaraan", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("id_bbm", new TableInfo.Column("id_bbm", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("id_harga", new TableInfo.Column("id_harga", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("liter_dibutuhkan", new TableInfo.Column("liter_dibutuhkan", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("total_biaya", new TableInfo.Column("total_biaya", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("deleted_at", new TableInfo.Column("deleted_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHasilPerhitungan.put("sync_state", new TableInfo.Column("sync_state", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHasilPerhitungan = new HashSet<TableInfo.ForeignKey>(4);
        _foreignKeysHasilPerhitungan.add(new TableInfo.ForeignKey("perjalanan", "CASCADE", "NO ACTION", Arrays.asList("id_perjalanan"), Arrays.asList("id_perjalanan")));
        _foreignKeysHasilPerhitungan.add(new TableInfo.ForeignKey("kendaraan", "CASCADE", "NO ACTION", Arrays.asList("id_kendaraan"), Arrays.asList("id_kendaraan")));
        _foreignKeysHasilPerhitungan.add(new TableInfo.ForeignKey("jenis_bbm", "CASCADE", "NO ACTION", Arrays.asList("id_bbm"), Arrays.asList("id_bbm")));
        _foreignKeysHasilPerhitungan.add(new TableInfo.ForeignKey("harga_bbm", "CASCADE", "NO ACTION", Arrays.asList("id_harga"), Arrays.asList("id_harga")));
        final HashSet<TableInfo.Index> _indicesHasilPerhitungan = new HashSet<TableInfo.Index>(5);
        _indicesHasilPerhitungan.add(new TableInfo.Index("index_hasil_perhitungan_id_perjalanan", false, Arrays.asList("id_perjalanan"), Arrays.asList("ASC")));
        _indicesHasilPerhitungan.add(new TableInfo.Index("index_hasil_perhitungan_id_kendaraan", false, Arrays.asList("id_kendaraan"), Arrays.asList("ASC")));
        _indicesHasilPerhitungan.add(new TableInfo.Index("index_hasil_perhitungan_id_bbm", false, Arrays.asList("id_bbm"), Arrays.asList("ASC")));
        _indicesHasilPerhitungan.add(new TableInfo.Index("index_hasil_perhitungan_id_harga", false, Arrays.asList("id_harga"), Arrays.asList("ASC")));
        _indicesHasilPerhitungan.add(new TableInfo.Index("index_hasil_perhitungan_updated_at", false, Arrays.asList("updated_at"), Arrays.asList("ASC")));
        final TableInfo _infoHasilPerhitungan = new TableInfo("hasil_perhitungan", _columnsHasilPerhitungan, _foreignKeysHasilPerhitungan, _indicesHasilPerhitungan);
        final TableInfo _existingHasilPerhitungan = TableInfo.read(db, "hasil_perhitungan");
        if (!_infoHasilPerhitungan.equals(_existingHasilPerhitungan)) {
          return new RoomOpenHelper.ValidationResult(false, "hasil_perhitungan(com.mybbm.app.data.local.entity.HasilPerhitunganEntity).\n"
                  + " Expected:\n" + _infoHasilPerhitungan + "\n"
                  + " Found:\n" + _existingHasilPerhitungan);
        }
        final HashMap<String, TableInfo.Column> _columnsSyncMeta = new HashMap<String, TableInfo.Column>(2);
        _columnsSyncMeta.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSyncMeta.put("value", new TableInfo.Column("value", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSyncMeta = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSyncMeta = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSyncMeta = new TableInfo("sync_meta", _columnsSyncMeta, _foreignKeysSyncMeta, _indicesSyncMeta);
        final TableInfo _existingSyncMeta = TableInfo.read(db, "sync_meta");
        if (!_infoSyncMeta.equals(_existingSyncMeta)) {
          return new RoomOpenHelper.ValidationResult(false, "sync_meta(com.mybbm.app.data.local.entity.SyncMetaEntity).\n"
                  + " Expected:\n" + _infoSyncMeta + "\n"
                  + " Found:\n" + _existingSyncMeta);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "2c6f043f43401fd029db945bd65ce5d4", "1d6771d3147b7c0b7cbba4809844742e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "device_info","jenis_bbm","harga_bbm","kendaraan","perjalanan","hasil_perhitungan","sync_meta");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `device_info`");
      _db.execSQL("DELETE FROM `jenis_bbm`");
      _db.execSQL("DELETE FROM `harga_bbm`");
      _db.execSQL("DELETE FROM `kendaraan`");
      _db.execSQL("DELETE FROM `perjalanan`");
      _db.execSQL("DELETE FROM `hasil_perhitungan`");
      _db.execSQL("DELETE FROM `sync_meta`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(KendaraanDao.class, KendaraanDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(JenisBbmDao.class, JenisBbmDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HargaBbmDao.class, HargaBbmDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PerjalananDao.class, PerjalananDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HasilPerhitunganDao.class, HasilPerhitunganDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DeviceInfoDao.class, DeviceInfoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SyncMetaDao.class, SyncMetaDao_Impl.getRequiredConverters());
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
  public KendaraanDao kendaraanDao() {
    if (_kendaraanDao != null) {
      return _kendaraanDao;
    } else {
      synchronized(this) {
        if(_kendaraanDao == null) {
          _kendaraanDao = new KendaraanDao_Impl(this);
        }
        return _kendaraanDao;
      }
    }
  }

  @Override
  public JenisBbmDao jenisBbmDao() {
    if (_jenisBbmDao != null) {
      return _jenisBbmDao;
    } else {
      synchronized(this) {
        if(_jenisBbmDao == null) {
          _jenisBbmDao = new JenisBbmDao_Impl(this);
        }
        return _jenisBbmDao;
      }
    }
  }

  @Override
  public HargaBbmDao hargaBbmDao() {
    if (_hargaBbmDao != null) {
      return _hargaBbmDao;
    } else {
      synchronized(this) {
        if(_hargaBbmDao == null) {
          _hargaBbmDao = new HargaBbmDao_Impl(this);
        }
        return _hargaBbmDao;
      }
    }
  }

  @Override
  public PerjalananDao perjalananDao() {
    if (_perjalananDao != null) {
      return _perjalananDao;
    } else {
      synchronized(this) {
        if(_perjalananDao == null) {
          _perjalananDao = new PerjalananDao_Impl(this);
        }
        return _perjalananDao;
      }
    }
  }

  @Override
  public HasilPerhitunganDao hasilPerhitunganDao() {
    if (_hasilPerhitunganDao != null) {
      return _hasilPerhitunganDao;
    } else {
      synchronized(this) {
        if(_hasilPerhitunganDao == null) {
          _hasilPerhitunganDao = new HasilPerhitunganDao_Impl(this);
        }
        return _hasilPerhitunganDao;
      }
    }
  }

  @Override
  public DeviceInfoDao deviceInfoDao() {
    if (_deviceInfoDao != null) {
      return _deviceInfoDao;
    } else {
      synchronized(this) {
        if(_deviceInfoDao == null) {
          _deviceInfoDao = new DeviceInfoDao_Impl(this);
        }
        return _deviceInfoDao;
      }
    }
  }

  @Override
  public SyncMetaDao syncMetaDao() {
    if (_syncMetaDao != null) {
      return _syncMetaDao;
    } else {
      synchronized(this) {
        if(_syncMetaDao == null) {
          _syncMetaDao = new SyncMetaDao_Impl(this);
        }
        return _syncMetaDao;
      }
    }
  }
}

package com.truthlens.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.truthlens.app.data.local.db.dao.AppSettingsDao
import com.truthlens.app.data.local.db.dao.ScanHistoryDao
import com.truthlens.app.data.local.db.entity.AppSettingsEntity
import com.truthlens.app.data.local.db.entity.ScanHistoryEntity

@Database(
    entities = [ScanHistoryEntity::class, AppSettingsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TruthLensDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "truthlens.db"
    }
}

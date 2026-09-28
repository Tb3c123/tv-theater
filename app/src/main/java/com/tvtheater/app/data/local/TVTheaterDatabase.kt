package com.tvtheater.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvtheater.app.data.local.dao.WatchHistoryDao
import com.tvtheater.app.data.local.dao.WatchlistDao
import com.tvtheater.app.data.local.entity.WatchHistoryEntity
import com.tvtheater.app.data.local.entity.WatchlistEntity

@Database(
    entities = [WatchHistoryEntity::class, WatchlistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TVTheaterDatabase : RoomDatabase() {

    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        @Volatile
        private var INSTANCE: TVTheaterDatabase? = null

        fun getInstance(context: Context): TVTheaterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TVTheaterDatabase::class.java,
                    "tv_theater_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.agenticedge.shopdemo.data.event

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AgentEvent::class, ProductPreference::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun agentEventDao(): AgentEventDao
    abstract fun productPreferenceDao(): ProductPreferenceDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edgeshop.db"
                ).build().also { instance = it }
            }
        }
    }
}

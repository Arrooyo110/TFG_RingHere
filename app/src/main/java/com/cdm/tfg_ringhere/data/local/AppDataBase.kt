package com.cdm.tfg_ringhere.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cdm.tfg_ringhere.data.local.dao.AlarmaDao
import com.cdm.tfg_ringhere.model.Alarma

@Database(entities = [Alarma::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmaDao(): AlarmaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

// Crea el método getDatabase siguiendo el patrón Singleton
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
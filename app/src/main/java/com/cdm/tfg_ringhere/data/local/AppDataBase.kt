package com.cdm.tfg_ringhere.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cdm.tfg_ringhere.data.local.dao.AlarmaDao
import com.cdm.tfg_ringhere.data.local.dao.EventoAlarmaDao
import com.cdm.tfg_ringhere.model.Alarma
import com.cdm.tfg_ringhere.model.EventoAlarma

@Database(entities = [Alarma::class, EventoAlarma::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmaDao(): AlarmaDao
    abstract fun eventoAlarmaDao(): EventoAlarmaDao

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
                )
                    .fallbackToDestructiveMigration() // Actualizar tablas si se realiza algun cambio
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
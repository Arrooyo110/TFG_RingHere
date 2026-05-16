package com.cdm.tfg_ringhere.data.repository

import com.cdm.tfg_ringhere.data.local.dao.AlarmaDao
import com.cdm.tfg_ringhere.model.Alarma
import kotlinx.coroutines.flow.Flow

class AlarmaRepository(private val alarmaDao: AlarmaDao) {

    fun getAlarmasByUser(email: String): Flow<List<Alarma>> {
        return alarmaDao.getAlarmasByUser(email)
    }

    suspend fun insert(alarma: Alarma) {
        alarmaDao.insertAlarma(alarma)
    }

    suspend fun delete(alarma: Alarma) {
        alarmaDao.deleteAlarma(alarma)
    }
}
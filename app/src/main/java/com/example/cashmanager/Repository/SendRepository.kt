package com.example.cashmanager.Repository

import com.example.cashmanager.data.SendDao
import com.example.cashmanager.data.models.SendTran
import kotlinx.coroutines.*

class SendRepository(private val sendDao: SendDao) {

    suspend fun insertTransfer(transfer: SendTran) {
        coroutineScope {
            launch {sendDao.insertTransfer(transfer)}
        }
    }

    suspend fun removeTransfer(transfer: SendTran){
        coroutineScope {
            launch {sendDao.removeTransfer(transfer)}
        }
    }

    suspend fun getAll(): List<SendTran> {
        return sendDao.getAll()
    }

}
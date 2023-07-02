package com.example.cashmanager.Repository

import androidx.lifecycle.LiveData
import com.example.cashmanager.data.CurrencyTransferDao
import com.example.cashmanager.data.models.CurrencyTransfer
import kotlinx.coroutines.*

class CurrencyTransferRepository(
    private val currencyTransferDao: CurrencyTransferDao
) {
    suspend fun addCurrencyTransfer(currencyTransfer: CurrencyTransfer) {
        coroutineScope {
            launch{ currencyTransferDao.insertCurrencyTransfer(currencyTransfer)}
        }
    }

    fun getCurrencyTransfers(): LiveData<List<CurrencyTransfer>> {
        return currencyTransferDao.getCurrencyTransfers()
    }

//    suspend fun updateReceivedTran(receivedTran: ReceivedTran) {
//        coroutineScope {
//            launch{receivedTranDao.updateReceivedTran(receivedTran)}
//        }
//    }


}
package com.example.cashmanager

import android.app.Application
import com.example.cashmanager.Repository.CurrencyTransferRepository
import com.example.cashmanager.data.CurrencyTransferDb

class CurrencyTransfersApplication: Application() {
    val currencyTransactionRepository: CurrencyTransferRepository
        get() = CurrencyTransferRepository(
            CurrencyTransferDb.getDatabase(this)!!.currencyTransferDao()
        )
}
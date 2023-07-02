package com.example.cashmanager.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.cashmanager.data.models.CurrencyTransfer

@Dao
interface CurrencyTransferDao {
    @Insert
    fun insertCurrencyTransfer(currencyTransfer: CurrencyTransfer)

    @Query("SELECT * FROM currency_transfers")
    fun getCurrencyTransfers(): LiveData<List<CurrencyTransfer>>

}
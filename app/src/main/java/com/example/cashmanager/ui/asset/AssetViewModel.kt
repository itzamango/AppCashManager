package com.example.cashmanager.ui.asset

import androidx.lifecycle.ViewModel
import com.example.cashmanager.Repository.CurrencyTransferRepository

class AssetViewModel(private val currencyTransferRepository: CurrencyTransferRepository): ViewModel() {
    val currencyTransferList = currencyTransferRepository.getCurrencyTransfers()
}
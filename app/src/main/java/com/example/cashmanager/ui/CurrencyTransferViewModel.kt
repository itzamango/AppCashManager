package com.example.cashmanager.ui

import android.util.Log
import java.text.SimpleDateFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashmanager.Repository.CurrencyTransferRepository
import com.example.cashmanager.data.models.CurrencyTransfer
import kotlinx.coroutines.launch
import java.util.*

class CurrencyTransferViewModel(private val currencyTransferRepository: CurrencyTransferRepository): ViewModel() {
    private var _currencyTransferDone = MutableLiveData<Boolean>(false)
    val currencyTransferDone = _currencyTransferDone


    var amount: String? = null
    var asset: String? = null

    var amountSend: String? = null
    var assetSend: String? = null
    var emailSend: String? = null

    fun setAmount(s: CharSequence, start:Int, before: Int, count:Int){
        amount = s.toString()
    }

    fun setAsset(s: CharSequence, start:Int, before: Int, count:Int){
        asset = s.toString()
    }

    fun setAmountSend(s: CharSequence, start:Int, before: Int, count:Int){
        amountSend = s.toString()
    }

    fun setAssetSend(s: CharSequence, start:Int, before: Int, count:Int){
        assetSend = s.toString()
    }

    fun setEmailSend(s: CharSequence, start:Int, before: Int, count:Int){
        emailSend = s.toString()
    }

    fun getCurrentDateTime(): Date {
        // Get date
        return Calendar.getInstance().time
    }
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        // Convert date
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun newCurrencyTransfer() = viewModelScope.launch{
        if ( !amount.isNullOrBlank() && !asset.isNullOrBlank()){
            val date = getCurrentDateTime()
            val dateInString = date.toString("yyyy/MM/dd HH:mm:ss")
            val currencyWallet = CurrencyTransfer(
                type = "Receive",
                amount = amount.toString(),
                asset = asset.toString(),
                userFromTo = "None",
                createdAt = dateInString,
                status = "generated",
                id = 0
            )

            currencyTransferRepository.addCurrencyTransfer(currencyWallet)
//
            _currencyTransferDone.value = true
        }
    }

    fun newCurrencyTransferSend() = viewModelScope.launch{
        if ( !amountSend.isNullOrBlank() && !assetSend.isNullOrBlank()){
            //Log.d("amount", amountSend.toString())
            //Log.d("asset", assetSend.toString())
            val date = getCurrentDateTime()
            val dateInString = date.toString("yyyy/MM/dd HH:mm:ss")
            val currencyWallet = CurrencyTransfer(
                type = "Transfer",
                amount = amountSend.toString(),
                asset = assetSend.toString(),
                userFromTo = emailSend.toString(),
                createdAt = dateInString,
                status = "generated",
                id = 0
            )

            currencyTransferRepository.addCurrencyTransfer(currencyWallet)

            _currencyTransferDone.value = true
        }
    }

}
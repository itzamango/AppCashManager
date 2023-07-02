package com.example.cashmanager.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cashmanager.Asset
import com.example.cashmanager.Asset_Activity
import com.example.cashmanager.R
import kotlin.math.round

class DashboardViewModel: ViewModel() {

    private val _assets = MutableLiveData<MutableList<Asset>>()
    val assets : LiveData<MutableList<Asset>>
        get() = _assets

    private val _activities = MutableLiveData<MutableList<Asset_Activity>>()
    val activities: LiveData<MutableList<Asset_Activity>>
        get() = _activities

    fun getAssets(){
        val a = getContacts()
        _assets.value = a
    }

    fun getActivities(){
        val a = getAssets_Activities()
        _activities.value = a
    }



    fun getTotalBalance(list: MutableList<Asset>): Double {
        var total: Double = 0.0

        list.forEach { total += it.balance_fiat}

        return total
    }

    //
    fun getContacts(): MutableList<Asset>{
        var assets:MutableList<Asset> = ArrayList()
        var balance: List<Double> = listOf(1.0, 20.0, 10.0)
        var fiatPrice: List<Double> = listOf(1.0, 20.0, 10.0)

        assets.add(Asset("MXN", balance[0], fiatPrice[0],round(fiatPrice[0]*balance[0]), R.drawable.ic_mxn, "MXN"))
        assets.add(Asset("DLS", balance[1], fiatPrice[1],round(fiatPrice[1]*balance[1]), R.drawable.ic_dls, "DLS"))
        assets.add(Asset("YEN", balance[2], fiatPrice[2],round(fiatPrice[2]*balance[2]), R.drawable.ic_yen,"YEN"))


        return assets
    }

    //
    private fun getAssets_Activities() = mutableListOf(
    Asset_Activity("Transfer", 1.5, "B","Oct 15",R.drawable.cm_send_icon, "MXN"),
    Asset_Activity("Transfer", 0.5, "C","Oct 14",R.drawable.cm_send_icon,"DLS"),
    Asset_Activity("Receive", 3.6, "F","Oct 13",R.drawable.cm_receive_icon, "MXN"),
    Asset_Activity("Transfer", 2.5, "G","Oct 12",R.drawable.cm_send_icon,"DLS"),

    )
}
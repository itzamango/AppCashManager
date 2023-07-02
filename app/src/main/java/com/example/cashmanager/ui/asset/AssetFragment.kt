package com.example.cashmanager.ui.asset

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cashmanager.*
import com.example.cashmanager.data.models.CurrencyTransfer
import com.example.cashmanager.databinding.FragmentAssetBinding
import androidx.lifecycle.Observer
import com.example.cashmanager.ui.dashboard.DashboardViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AssetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssetFragment(var position: Int = 0) : Fragment() {

    private lateinit var adapter: RecyclerAdapter_AssetActivity
    private lateinit var viewModel: AssetViewModel
    private lateinit var binding: FragmentAssetBinding
    private lateinit var currentAsset: String

    private val viewModelDashboard = DashboardViewModel()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val safeArgs: AssetFragmentArgs by navArgs()
        position = safeArgs.positionSelected

        when(position){
            0 -> currentAsset = "MXN"
            1 -> currentAsset = "DLS"
            2 -> currentAsset = "YEN"
            else -> currentAsset = ""
        }


        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_asset,
            container,
            false
        )

        viewModel = AssetViewModel(
            (requireContext().applicationContext as CurrencyTransfersApplication).currencyTransactionRepository
        )

        binding.lifecycleOwner = this
        binding.currencytransferListViewModel = viewModel

        binding.appBar.setNavigationOnClickListener {

            findNavController().navigate(R.id.action_assetFragment_to_dashboardFragmentDest, null)
        }

        return binding.root

    }

override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    setupCurrencyTransferList()

}

    private fun setupCurrencyTransferList() {
        var totalAmount = 0f
        var filterList: ArrayList<CurrencyTransfer> = arrayListOf()
        if(viewModel!=null){
            adapter = RecyclerAdapter_AssetActivity(viewModel)
            binding.recyclerContactsAssetFrg.adapter = adapter

            viewModel.currencyTransferList.observe(viewLifecycleOwner, Observer {
                it?.let {

                    for(item in it){
                        if(item.asset == currentAsset) {
                            if(item.type == "Receive") {
                                totalAmount += item.amount?.toFloat()!!
                            }
                            else{
                                totalAmount -= item.amount?.toFloat()!!
                            }
                            filterList.add(item)
                        }
                    }
                    val assets: MutableList<Asset> = viewModelDashboard.getContacts()
                    adapter.submitList(filterList)
                    totalAmount = (totalAmount * assets[position].fiat_price).toFloat()
                    binding.balanceAssetFragment.text = "$${totalAmount.toString()}"
                }
            })
        }
    }





}
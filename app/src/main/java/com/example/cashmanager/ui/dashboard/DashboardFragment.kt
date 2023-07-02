package com.example.cashmanager.ui.dashboard

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.example.cashmanager.ui.user.PREF_NAME
import com.example.cashmanager.ui.user.USERNAME
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashmanager.*
import com.example.cashmanager.ui.asset.AssetViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {

    private lateinit var navigation_view: NavigationView
    private lateinit var header: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var balance:TextView
    private lateinit var send_button:Button
    private lateinit var receive_button:Button
    private lateinit var asset_button:Button
    private lateinit var activity_button:Button

    private lateinit var recyclerContacts: RecyclerView
    private lateinit var mAdapter : RecyclerAdapter

    private lateinit var usernameAppbar: TextView
    private lateinit var emailAppbar: TextView
    private lateinit var preferences: SharedPreferences

    private val viewModel = DashboardViewModel()

    private lateinit var viewModelAsset: AssetViewModel
    private lateinit var mAdapter_activity: RecyclerAdapter_AssetActivity
    private lateinit var binding: DashboardFragment


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

        //MDC-102
        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        preferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        drawerLayout = view.findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = view.findViewById(R.id.app_bar) as Toolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        ActionBarDrawerToggle(view.context as Activity?,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer)
        navigation_view = view.findViewById(R.id.nav_view)
        header = navigation_view.getHeaderView(0)
        usernameAppbar = header.findViewById(R.id.userNameAppbar)
        emailAppbar = header.findViewById(R.id.emailAppbar)

        val user = FirebaseAuth.getInstance().currentUser
        val mAuth = FirebaseAuth.getInstance()

        //Buttons - textviews
        balance = view.findViewById(R.id.balance)
        send_button = view.findViewById(R.id.send)
        receive_button = view.findViewById(R.id.receive)
        asset_button = view.findViewById(R.id.button3)
        activity_button = view.findViewById(R.id.button4)

        recyclerContacts = view.findViewById(R.id.recyclerContacts)

        viewModelAsset = AssetViewModel(
            (requireContext().applicationContext as CurrencyTransfersApplication).currencyTransactionRepository
        )

        viewModel.getAssets()
        setUpRecyclerView()

        usernameAppbar.text = preferences.getString(USERNAME, "")
        emailAppbar.text = user?.email.toString()

        send_button.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_sendFragment, null)
        }


        receive_button.setOnClickListener {

            findNavController().navigate(R.id.action_dashboardFragment_to_receiveFragment, null)
        }

        activity_button.setOnClickListener {
            viewModel.getActivities()
            setUpRecyclerView_Activity()
        }

        asset_button.setOnClickListener {
            setUpRecyclerView()
        }

        navigation_view.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.logout -> {
                    mAuth!!.signOut()

                    findNavController().navigate(R.id.action_dashboardFragmentDest_to_loginFragment, null)
                }
            }
            true
        }

        viewModel.assets.observe(viewLifecycleOwner, Observer {
            if(asset_button.isActivated){
                setUpRecyclerView()
            }
        })

        viewModel.activities.observe(viewLifecycleOwner, Observer {
            if(activity_button.isActivated){
                setUpRecyclerView_Activity()
            }
        })


        return view
    }



    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.cm_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //configuramos lo necesario para desplegar el RecyclerView
    private fun setUpRecyclerView(){
        var totalAmount = 0f
        var totalMXN = 0f
        var totalDLS = 0f
        var totalYEN = 0f
        var assets_value: MutableList<AssetValue>
        val assets: MutableList<Asset> = viewModel.getContacts()
        recyclerContacts.setHasFixedSize(true)
        viewModelAsset.currencyTransferList.observe(viewLifecycleOwner, Observer {
            it?.let {
                for (item in it) {
                    if(item.asset == "MXN") {
                        if(item.type == "Receive") {
                            totalMXN += item.amount?.toFloat()!!
                        }
                        else{
                            totalMXN -= item.amount?.toFloat()!!
                        }
                    }
                    if(item.asset == "DLS") {
                        if(item.type == "Receive") {
                            totalDLS += item.amount?.toFloat()!!
                        }
                        else{
                            totalDLS -= item.amount?.toFloat()!!
                        }
                    }
                    if(item.asset == "YEN") {
                        if(item.type == "Receive") {
                            totalYEN += item.amount?.toFloat()!!
                        }
                        else{
                            totalYEN -= item.amount?.toFloat()!!
                        }
                    }
                }
                totalMXN = (totalMXN * assets[0].fiat_price).toFloat()
                totalDLS = (totalDLS * assets[1].fiat_price).toFloat()
                totalYEN = (totalYEN * assets[2].fiat_price).toFloat()
                totalAmount = totalYEN + totalMXN + totalDLS
                balance.text = "$" + totalAmount.toString()

                assets_value = mutableListOf<AssetValue>(
                    AssetValue("MXN", totalMXN, (totalMXN / assets[0].fiat_price.toFloat())),
                    AssetValue("DLS", totalDLS, (totalDLS / assets[1].fiat_price.toFloat())),
                    AssetValue("YEN", totalYEN, (totalYEN / assets[2].fiat_price.toFloat()))
                )
                //nuestro layout va a ser de una sola columna
                recyclerContacts.layoutManager = LinearLayoutManager(context)
                //seteando el Adapter¿
                mAdapter = RecyclerAdapter(context,viewModel.assets.value!!, assets_value)
                //asignando el Adapter al RecyclerView
                recyclerContacts.adapter = mAdapter
            }
        })

    }

    //RecyclerView
    private fun setUpRecyclerView_Activity(){
        var totalAmount = 0f

        recyclerContacts.setHasFixedSize(true)

        //Adapter
        if(viewModel!=null) {
            mAdapter_activity = RecyclerAdapter_AssetActivity(viewModelAsset)
            //asignando el Adapter al RecyclerView
            recyclerContacts.adapter = mAdapter_activity
            viewModelAsset.currencyTransferList.observe(viewLifecycleOwner, Observer {
                it?.let {
                    mAdapter_activity.submitList(it)
                    for (item in it) {
                        if(item.type == "Receive") {
                            totalAmount += item.amount?.toFloat()!!
                        }
                        else{
                            totalAmount -= item.amount?.toFloat()!!
                        }
                    }
                }
            })
        }
    }


}
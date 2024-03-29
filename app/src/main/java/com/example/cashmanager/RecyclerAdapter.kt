package com.example.cashmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.cashmanager.ui.dashboard.DashboardFragmentDirections


class RecyclerAdapter(
    var context: Context?,
    var assets: MutableList<Asset>,
    var assets_value: MutableList<AssetValue>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    //Aquí atamos el ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = assets.get(position)
        val asset_value = assets_value.get(position)
        holder.bind(item, context!!, asset_value)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_asset, parent, false))
    }

    override fun getItemCount(): Int {
        return assets.size
    }




    //El ViewHolder ata los datos del RecyclerView a la Vista para desplegar la información
    //También se encarga de gestionar los eventos de la View, como los clickListeners
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //obteniendo las referencias a las Views
        val asset_name = view.findViewById(R.id.asset_name) as TextView
        val price_fiat = view.findViewById(R.id.price_fiat) as TextView
        val asset_balance = view.findViewById(R.id.asset_balance) as TextView
        val fiat_balance = view.findViewById(R.id.fiat_balance) as TextView
        val image = view.findViewById(R.id.userImage) as ImageView

        init {
            itemView.setOnClickListener {
                val position: Int = adapterPosition

                /*val assetfragment = AssetFragment(position)

                val fragmentManager = (itemView.context as FragmentActivity).supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                // animation
                transaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right)
                fragmentManager.popBackStack()
                transaction.replace(R.id.fragment_container, assetfragment)
                transaction.commit()*/
                val action = DashboardFragmentDirections.actionDashboardFragmentDestToAssetFragment(position)
                Navigation.findNavController(it).navigate(action)
                //Navigation.findNavController(it).navigate(R.id.action_dashboardFragmentDest_to_assetFragment, null)
            }
        }

        //"atando" los datos a las Views
        fun bind(asset: Asset, context: Context, asset_value: AssetValue) {
            asset_name.text = asset.name
            price_fiat.text = "$" + asset.fiat_price.toString()
            asset_balance.text =  asset_value.assetConversion.toString()
            fiat_balance.text = "$" + asset_value.assetTotal.toString()
            image.setImageResource(asset.idImage)

        }
    }


}
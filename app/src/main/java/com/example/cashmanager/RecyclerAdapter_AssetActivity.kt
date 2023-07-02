package com.example.cashmanager

import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.example.cashmanager.data.models.CurrencyTransfer
import com.example.cashmanager.databinding.ItemActivityBinding
import com.example.cashmanager.ui.asset.AssetViewModel

class RecyclerAdapter_AssetActivity(
    private val viewModel: AssetViewModel
    ) :
    ListAdapter<CurrencyTransfer, RecyclerAdapter_AssetActivity.ViewHolder>(CurrencyTransferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    //ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    class ViewHolder private constructor(val binding: ItemActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var loadingDialog: Dialog? = null

        private fun hideLoading() {
            loadingDialog?.let { if(it.isShowing)it.cancel() }
        }

        private fun showLoading() {
            hideLoading()
            loadingDialog = CommonUtils.showLoadingDialog(itemView.context)
        }

        //"atando" los datos a las Views
        fun bind(viewModel: AssetViewModel, item: CurrencyTransfer){
            binding.viewModel = viewModel
            binding.currencytransfer = item
            binding.executePendingBindings()


            when(item.type){
                "Receive"-> binding.userImage.setImageResource(R.drawable.ic_send)
                "Transfer" -> binding.userImage.setImageResource(R.drawable.ic_receive)
            }

            //Gestionando los eventos e interacciones con la vista
            itemView.setOnClickListener{
                val position: Int = adapterPosition
                showLoading()
                android.os.Handler().postDelayed({hideLoading()},3000)

                val amountTransaction = item.amount
                val assetTransaction = item.asset
                val dateTransaction = item.createdAt

            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemActivityBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }



    }


}

class CurrencyTransferDiffCallback : DiffUtil.ItemCallback<CurrencyTransfer>() {
    override fun areItemsTheSame(oldItem: CurrencyTransfer, newItem: CurrencyTransfer): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CurrencyTransfer, newItem: CurrencyTransfer): Boolean {
        return oldItem == newItem
    }
}

package blockeq.com.stellarwallet.adapters

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import android.view.View
import blockeq.com.stellarwallet.models.TransactionType


class WalletRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

    }

    override fun getItemCount(): Int {

    }

    override fun getItemViewType(position: Int): Int {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    //region View Holders
    class TotalBalanceViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var balance : TextView? = null

        init {

        }
    }

    class AvailableBalanceViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var balance : TextView? = null

        init {

        }
    }

    class AccounActivityViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var amount : TextView? = null
        var transactionType : TransactionType? = null

        init {

        }
    }

    //endregion
}

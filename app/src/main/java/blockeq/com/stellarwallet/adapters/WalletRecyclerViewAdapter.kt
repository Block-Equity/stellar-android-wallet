package blockeq.com.stellarwallet.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.models.AvailableBalance
import blockeq.com.stellarwallet.models.TotalBalance
import blockeq.com.stellarwallet.models.AccountEffect
import blockeq.com.stellarwallet.models.EffectType


class WalletRecyclerViewAdapter(var items : ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onAssetsDropdownListener : OnAssetDropdownListener? = null
    private var onLearnMoreListener : OnLearnMoreButtonListener? = null

    interface OnAssetDropdownListener {
        fun onAssetDropdownClicked(view : View, position : Int)
    }

    interface OnLearnMoreButtonListener {
        fun onLearnMoreButtonClicked(view : View, position : Int)
    }

    enum class TransactionViewType(val value :Int) {
        TOTAL(0), AVAILABLE(1), HEADER(2), TRANSACTION(3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(viewType) {
            TransactionViewType.TOTAL.value -> {
                val v = inflater.inflate(R.layout.item_total_balance, parent, false)
                TotalBalanceViewHolder(v)
            }
            TransactionViewType.AVAILABLE.value -> {
                val v = inflater.inflate(R.layout.item_available_balance, parent, false)
                AvailableBalanceViewHolder(v)
            }
            TransactionViewType.HEADER.value -> {
                val v = inflater.inflate(R.layout.item_header_transaction_list, parent, false)
                TransactionHeaderViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(v)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            items[position] is TotalBalance -> TransactionViewType.TOTAL.value
            items[position] is AvailableBalance -> TransactionViewType.AVAILABLE.value
            items[position] is Pair<*, *> -> TransactionViewType.HEADER.value
            items[position] is AccountEffect -> TransactionViewType.TRANSACTION.value
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TransactionViewType.TOTAL.value -> {
                val vh = holder as TotalBalanceViewHolder
                configureTotalBalanceViewHolder(vh, position)
            }
            TransactionViewType.AVAILABLE.value -> {
                val vh = holder as AvailableBalanceViewHolder
                configureAvailableBalanceViewHolder(vh, position)
            }
            TransactionViewType.HEADER.value -> {
                val vh = holder as TransactionHeaderViewHolder
                configureTransactionHeaderViewHolder(vh, position)
            }
            TransactionViewType.TRANSACTION.value -> {
                val vh = holder as TransactionViewHolder
                configureTransactionViewHolder(vh, position)
            }
        }
    }


    fun setOnAssetDropdownListener(listener: OnAssetDropdownListener) {
        onAssetsDropdownListener = listener
    }

    fun setOnLearnMoreButtonListener(listener: OnLearnMoreButtonListener) {
        onLearnMoreListener = listener
    }

    //region View Holders
    inner class TotalBalanceViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var balance : TextView? = null
        var assetsButton : ImageView? = null

        init {
            balance = v.findViewById(R.id.balanceTextView)
            assetsButton = v.findViewById(R.id.assetsButton)

            assetsButton!!.setOnClickListener {
                if (onAssetsDropdownListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onAssetsDropdownListener!!.onAssetDropdownClicked(v, position)
                    }
                }
            }
        }
    }

    class AvailableBalanceViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var balance : TextView? = null

        init {
            balance = v.findViewById(R.id.availableBalanceTextView)
        }
    }

    class TransactionHeaderViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var activityTextView : TextView? = null
        var amountTextView : TextView? = null

        init {
            activityTextView = v.findViewById(R.id.activityHeaderTextView)
            amountTextView = v.findViewById(R.id.amountHeaderTextView)
        }
    }

    class TransactionViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var amount : TextView? = null
        var date : TextView? = null
        var transactionType : TextView? = null

        init {
            amount = v.findViewById(R.id.amountTextView)
            date = v.findViewById(R.id.dateTextView)
            transactionType = v.findViewById(R.id.transactionTypeTextView)
        }
    }

    private fun configureTotalBalanceViewHolder(viewHolder : TotalBalanceViewHolder,
                                                position : Int) {
        val totalBalance = items[position] as TotalBalance
        viewHolder.balance!!.text = totalBalance.balance
    }

    private fun configureAvailableBalanceViewHolder(viewHolder : AvailableBalanceViewHolder,
                                                position : Int) {
        val availableBalance = items[position] as AvailableBalance
        viewHolder.balance!!.text = availableBalance.balance
    }

    private fun configureTransactionHeaderViewHolder(viewHolder : TransactionHeaderViewHolder,
                                                     position : Int) {
        val pair = items[position] as Pair<*, *>
        viewHolder.activityTextView!!.text = pair.first.toString()
        viewHolder.amountTextView!!.text = pair.second.toString()
    }

    private fun configureTransactionViewHolder(viewHolder : TransactionViewHolder, position : Int) {
        val transaction = items[position] as AccountEffect

        viewHolder.amount!!.text = transaction.amount
        viewHolder.date!!.text = transaction.createdAt

        viewHolder.transactionType!!.text = when(transaction.type) {
            EffectType.RECEIVED.value -> EffectType.RECEIVED.value
            EffectType.SENT.value -> EffectType.SENT.value
            EffectType.CREATED.value -> EffectType.CREATED.value
            EffectType.REMOVED.value -> EffectType.REMOVED.value
            EffectType.TRADE.value -> EffectType.TRADE.value
            else -> ""
        }
    }

    //endregion
}

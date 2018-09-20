package blockeq.com.stellarwallet.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.models.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


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
        TOTAL(0), AVAILABLE(1), HEADER(2),
        ACCOUNT_EFFECT(3), TRADE_EFFECT(4)
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
            TransactionViewType.TRADE_EFFECT.value -> {
                val v = inflater.inflate(R.layout.item_account_effect, parent, false)
                TradeEffectViewHolder(v)
            }
            else -> {
                val v = inflater.inflate(R.layout.item_account_effect, parent, false)
                AccountEffectViewHolder(v)
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
            items[position] is AccountEffect -> TransactionViewType.ACCOUNT_EFFECT.value
            items[position] is TradeEffect -> TransactionViewType.TRADE_EFFECT.value
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
            TransactionViewType.ACCOUNT_EFFECT.value -> {
                val vh = holder as AccountEffectViewHolder
                configureAccountEffectViewHolder(vh, position)
            }
            TransactionViewType.TRADE_EFFECT.value -> {
                val vh = holder as TradeEffectViewHolder
                configureTradeEffectViewHolder(vh, position)

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

    class AccountEffectViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var amount : TextView? = null
        var date : TextView? = null
        var transactionType : TextView? = null

        init {
            amount = v.findViewById(R.id.amountTextView)
            date = v.findViewById(R.id.dateTextView)
            transactionType = v.findViewById(R.id.transactionTypeTextView)
        }
    }

    class TradeEffectViewHolder(v : View) : RecyclerView.ViewHolder(v) {
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
        viewHolder.balance!!.text = truncateDecimalPlaces(totalBalance.balance)
    }

    private fun configureAvailableBalanceViewHolder(viewHolder : AvailableBalanceViewHolder,
                                                position : Int) {
        val availableBalance = items[position] as AvailableBalance
        viewHolder.balance!!.text = truncateDecimalPlaces(availableBalance.balance)
    }

    private fun configureTransactionHeaderViewHolder(viewHolder : TransactionHeaderViewHolder,
                                                     position : Int) {
        val pair = items[position] as Pair<*, *>
        viewHolder.activityTextView!!.text = pair.first.toString()
        viewHolder.amountTextView!!.text = pair.second.toString()
    }

    private fun configureAccountEffectViewHolder(viewHolder : AccountEffectViewHolder, position : Int) {
        val transaction = items[position] as AccountEffect

        viewHolder.amount!!.text = truncateDecimalPlaces(transaction.amount)
        viewHolder.date!!.text = getFormattedDate(transaction.createdAt)

        viewHolder.transactionType!!.text = when(transaction.type) {
            EffectType.RECEIVED.value -> "Received"
            EffectType.SENT.value -> "Sent"
            EffectType.CREATED.value -> "Account created"
            EffectType.REMOVED.value -> "Account Removed"
            else -> "Error"
        }
    }

    private fun configureTradeEffectViewHolder(viewHolder : TradeEffectViewHolder, position : Int) {
        val trade = items[position] as TradeEffect

        viewHolder.amount!!.text = truncateDecimalPlaces(trade.boughtAmount) //TODO: Equals which ever asset is the selected one? Adding braces depending on that
        viewHolder.date!!.text = getFormattedDate(trade.createdAt)

        viewHolder.transactionType!!.text = "Trade " + trade.soldAsset + " for " + trade.boughtAsset
    }

    //endregion

    private fun getFormattedDate(str: String):String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, uuuu", Locale.ENGLISH)
                .withZone(ZoneId.of("UTC"))
        return formatter.format(Instant.parse(str))
    }

    private fun truncateDecimalPlaces(string: String?): String {
        if (string == null) return ""
        return string
    }
}
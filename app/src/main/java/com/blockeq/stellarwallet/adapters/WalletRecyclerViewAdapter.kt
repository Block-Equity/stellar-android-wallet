package com.blockeq.stellarwallet.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.utils.StringFormat
import com.blockeq.stellarwallet.utils.StringFormat.Companion.getFormattedDate
import com.blockeq.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces

class WalletRecyclerViewAdapter(var context: Context, var items : ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
        var balance: TextView = v.findViewById(R.id.balanceTextView)
        var assetName: TextView = v.findViewById(R.id.assetNameTextView)
        var assetsButton: ImageView = v.findViewById(R.id.assetsButton)

        init {
            assetsButton.setOnClickListener {
                onAssetsDropdownListener?.let { listener ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAssetDropdownClicked(v, position)
                    }
                }
            }
        }
    }

    inner class AvailableBalanceViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var balance : TextView = v.findViewById(R.id.availableBalanceTextView)
        var learnMoreButton : TextView = v.findViewById(R.id.learnMoreButton)

        init {
            learnMoreButton.setOnClickListener {
                onLearnMoreListener?.let { listener ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onLearnMoreButtonClicked(v, position)
                    }
                }
            }
        }
    }

    class TransactionHeaderViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var activityTextView : TextView = v.findViewById(R.id.activityHeaderTextView)
        var amountTextView : TextView = v.findViewById(R.id.amountHeaderTextView)
    }

    class AccountEffectViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var amount : TextView = v.findViewById(R.id.amountTextView)
        var date : TextView = v.findViewById(R.id.dateTextView)
        var transactionType : TextView = v.findViewById(R.id.transactionTypeTextView)
        var dot : ImageView = v.findViewById(R.id.iconImageView)
    }

    class TradeEffectViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var amount : TextView = v.findViewById(R.id.amountTextView)
        var date : TextView = v.findViewById(R.id.dateTextView)
        var transactionType : TextView = v.findViewById(R.id.transactionTypeTextView)
        var dot : ImageView = v.findViewById(R.id.iconImageView)
    }

    private fun configureTotalBalanceViewHolder(viewHolder : TotalBalanceViewHolder, position : Int) {
        val totalBalance = items[position] as TotalBalance

        viewHolder.balance.text = truncateDecimalPlaces(totalBalance.balance)
        viewHolder.assetName.text = String.format(context.getString(R.string.asset_template),
                totalBalance.assetName, getVisibleAssetcode(totalBalance.assetCode))
    }

    private fun configureAvailableBalanceViewHolder(viewHolder : AvailableBalanceViewHolder, position : Int) {
        val availableBalance = items[position] as AvailableBalance
        viewHolder.balance.text = "${truncateDecimalPlaces(availableBalance.balance)} ${getVisibleAssetcode(availableBalance.assetCode)}"
    }

    private fun getVisibleAssetcode(assetCode : String) : String {
        if(assetCode == "native") return "XLM"
        else return assetCode
    }
    private fun configureTransactionHeaderViewHolder(viewHolder : TransactionHeaderViewHolder, position : Int) {
        val pair = items[position] as Pair<*, *>
        viewHolder.activityTextView.text = pair.first.toString()
        viewHolder.amountTextView.text = pair.second.toString()
    }

    private fun formatNumber4Decimals(amount : String?) : String? {
        if (amount == null) return "--"
        val displayAmount = truncateDecimalPlaces(amount)
        if (displayAmount.toFloat() == 0f) {
            return "< 0.0001"
        }
        return displayAmount
    }

    @SuppressLint("SetTextI18n")
    private fun configureAccountEffectViewHolder(viewHolder : AccountEffectViewHolder, position : Int) {
        val transaction = items[position] as AccountEffect

        viewHolder.amount.text = formatNumber4Decimals(transaction.amount)
        viewHolder.date.text = getFormattedDate(transaction.createdAt)

        viewHolder.transactionType.text = when(transaction.type) {
            EffectType.RECEIVED.value -> "Received"
            EffectType.SENT.value -> "Sent"
            EffectType.CREATED.value -> "Account created"
            EffectType.REMOVED.value -> "Account Removed"
            EffectType.ACCOUNT_HOME_DOMAIN_UPDATED.value -> "Account home domain updated"
            EffectType.ACCOUNT_FLAGS_UPDATED.value -> "Account flags updated"
            EffectType.ACCOUNT_INFLATION_DESTINATION_UPDATED.value -> "Account inflation destination updated"
            EffectType.SIGNER_CREATED.value -> "Signer created"
            EffectType.SIGNER_REMOVED.value -> "Signer removed"
            EffectType.SIGNER_UPDATED.value -> "Signer updated"
            EffectType.TRUSTLINE_CREATED.value -> "Trustline created"
            EffectType.TRUSTLINE_REMOVED.value -> "Trustline removed"
            EffectType.TRUSTLINE_UPDATED.value -> "Trustline updated"
            EffectType.TRUSTLINE_AUTHORIZED.value -> "Trustline authorized"
            EffectType.TRUSTLINE_DEAUTHORIZED.value -> "Trustline deauthorized"
            EffectType.OFFER_CREATED.value -> "Offer created"
            EffectType.OFFER_REMOVED.value -> "Offer removed"
            EffectType.OFFER_UPDATED.value -> "Offer updated"
            EffectType.DATA_CREATED.value -> "Data created"
            EffectType.DATA_REMOVED.value -> "Data removed"
            EffectType.DATA_UPDATED.value -> "Data updated"
            EffectType.SEQUENCE_BUMPED.value -> "Sequence bumped"
            else -> "Error"
        }

        when {
            transaction.type == EffectType.RECEIVED.value -> viewHolder.dot.setColorFilter(ContextCompat.getColor(context, R.color.mantis), PorterDuff.Mode.SRC_IN)
            transaction.type == EffectType.SENT.value -> {
                viewHolder.dot.setColorFilter(ContextCompat.getColor(context, R.color.apricot), PorterDuff.Mode.SRC_IN)
                viewHolder.amount.text = String.format(context.getString(R.string.bracket_template), viewHolder.amount.text.toString())
            }
            else -> viewHolder.dot.setColorFilter(ContextCompat.getColor(context, R.color.paleSky), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun configureTradeEffectViewHolder(viewHolder : TradeEffectViewHolder, position : Int) {
        val trade = items[position] as TradeEffect

        viewHolder.transactionType.text = String.format(context.getString(R.string.trade_item_template),
                StringFormat.formatAssetCode(trade.soldAsset), StringFormat.formatAssetCode(trade.boughtAsset))
        viewHolder.dot.setColorFilter(ContextCompat.getColor(context, R.color.paleSky), PorterDuff.Mode.SRC_IN)
        if (WalletApplication.userSession.currAssetCode == trade.boughtAsset) {
            viewHolder.amount.text = truncateDecimalPlaces(trade.boughtAmount)
        } else {
            viewHolder.amount.text = String.format(context.getString(R.string.bracket_template),
                    truncateDecimalPlaces(trade.soldAmount))
        }
        viewHolder.date.text = getFormattedDate(trade.createdAt)
    }

    //endregion
}

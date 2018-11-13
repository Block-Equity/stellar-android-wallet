package blockeq.com.stellarwallet.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.models.*
import blockeq.com.stellarwallet.utils.StringFormat
import blockeq.com.stellarwallet.utils.StringFormat.Companion.getFormattedDate
import blockeq.com.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces

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
        var balance : TextView? = null
        var assetName : TextView? = null
        var assetsButton : ImageView? = null

        init {
            balance = v.findViewById(R.id.balanceTextView)
            assetsButton = v.findViewById(R.id.assetsButton)
            assetName = v.findViewById(R.id.assetNameTextView)

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

    inner class AvailableBalanceViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var balance : TextView? = null
        var learnMoreButton : TextView? = null

        init {
            balance = v.findViewById(R.id.availableBalanceTextView)
            learnMoreButton = v.findViewById(R.id.learnMoreButton)

            learnMoreButton!!.setOnClickListener {
                if (onLearnMoreListener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onLearnMoreListener!!.onLearnMoreButtonClicked(v, position)
                    }
                }
            }
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
        var dot : ImageView? = null

        init {
            amount = v.findViewById(R.id.amountTextView)
            date = v.findViewById(R.id.dateTextView)
            transactionType = v.findViewById(R.id.transactionTypeTextView)
            dot = v.findViewById(R.id.iconImageView)
        }
    }

    class TradeEffectViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        var amount : TextView? = null
        var date : TextView? = null
        var transactionType : TextView? = null
        var dot : ImageView? = null

        init {
            amount = v.findViewById(R.id.amountTextView)
            date = v.findViewById(R.id.dateTextView)
            transactionType = v.findViewById(R.id.transactionTypeTextView)
            dot = v.findViewById(R.id.iconImageView)
        }
    }

    private fun configureTotalBalanceViewHolder(viewHolder : TotalBalanceViewHolder,
                                                position : Int) {
        val totalBalance = items[position] as TotalBalance

        viewHolder.balance!!.text = truncateDecimalPlaces(totalBalance.balance)
        viewHolder.assetName!!.text = String.format(context.getString(R.string.asset_template),
                WalletApplication.userSession.currAssetName, WalletApplication.userSession.getFormattedCurrentAssetCode())
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

        if (transaction.type == EffectType.RECEIVED.value) {
            viewHolder.dot!!.setColorFilter(ContextCompat.getColor(context, R.color.mantis), PorterDuff.Mode.SRC_IN)
        } else if (transaction.type == EffectType.SENT.value) {
            viewHolder.dot!!.setColorFilter(ContextCompat.getColor(context, R.color.apricot), PorterDuff.Mode.SRC_IN)
            viewHolder.amount!!.text = String.format(context.getString(R.string.bracket_template),
                    viewHolder.amount!!.text)
        } else {
            viewHolder.dot!!.setColorFilter(ContextCompat.getColor(context, R.color.paleSky), PorterDuff.Mode.SRC_IN)
        }
    }

    private fun configureTradeEffectViewHolder(viewHolder : TradeEffectViewHolder, position : Int) {
        val trade = items[position] as TradeEffect

        viewHolder.transactionType!!.text = String.format(context.getString(R.string.trade_item_template),
                StringFormat.formatAssetCode(trade.soldAsset), StringFormat.formatAssetCode(trade.boughtAsset))

        if (WalletApplication.userSession.currAssetCode == trade.boughtAsset) {
            viewHolder.amount!!.text = truncateDecimalPlaces(trade.boughtAmount)
            viewHolder.dot!!.setColorFilter(ContextCompat.getColor(context, R.color.mantis), PorterDuff.Mode.SRC_IN)
        } else {
            viewHolder.amount!!.text = String.format(context.getString(R.string.bracket_template),
                    truncateDecimalPlaces(trade.soldAmount))
            viewHolder.dot!!.setColorFilter(ContextCompat.getColor(context, R.color.apricot), PorterDuff.Mode.SRC_IN)
        }

        viewHolder.date!!.text = getFormattedDate(trade.createdAt)

    }

    //endregion
}

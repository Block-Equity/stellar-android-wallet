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
import android.widget.ProgressBar
import android.widget.TextView
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.utils.StringFormat
import com.blockeq.stellarwallet.utils.StringFormat.Companion.getFormattedDate
import com.blockeq.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces
import android.support.v4.graphics.drawable.DrawableCompat
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat.getColor


class WalletRecyclerViewAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onAssetsDropdownListener : OnAssetDropdownListener? = null
    private var onLearnMoreListener : OnLearnMoreButtonListener? = null
    private var items : ArrayList<Any>? = null

    interface OnAssetDropdownListener {
        fun onAssetDropdownClicked(view : View, position : Int)
    }

    interface OnLearnMoreButtonListener {
        fun onLearnMoreButtonClicked(view : View, assetCode:String, issuer:String?, position : Int)
    }

    enum class TransactionViewType(val value :Int) {
        TOTAL(0), AVAILABLE(1), HEADER(2),
        ACCOUNT_EFFECT(3), TRADE_EFFECT(4)
    }

    fun setItems(list : ArrayList<Any>) {
        items = list
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
        if (items == null) return 0
        return items!!.size
    }

    override fun getItemViewType(position: Int): Int {
        items?.let{
            return when {
                it[position] is TotalBalance -> TransactionViewType.TOTAL.value
                it[position] is AvailableBalance -> TransactionViewType.AVAILABLE.value
                it[position] is Pair<*, *> -> TransactionViewType.HEADER.value
                it[position] is AccountEffect -> TransactionViewType.ACCOUNT_EFFECT.value
                it[position] is TradeEffect -> TransactionViewType.TRADE_EFFECT.value
                else -> 0
            }
        }

        return 0
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
        var root: View = v.findViewById(R.id.balanceRoot)
        var balance: TextView = v.findViewById(R.id.balanceTextView)
        var assetName: TextView = v.findViewById(R.id.assetNameTextView)
        var assetsButton: ImageView = v.findViewById(R.id.assetsButton)
        var progressBar: ProgressBar = v.findViewById(R.id.progressRefreshingWallet)

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
        val totalBalance = items!![position] as TotalBalance

        viewHolder.balance.text = totalBalance.balance
        val code = getVisibleAssetCode(totalBalance.assetCode)

        //TODO move this to states
        if (code.isEmpty()) {
            tintProgressBar(viewHolder.progressBar, getColor(context, R.color.white))
            viewHolder.progressBar.visibility = View.VISIBLE
            viewHolder.assetsButton.visibility = View.GONE
            viewHolder.assetName.text = totalBalance.assetName
        } else {
            viewHolder.progressBar.visibility = View.GONE
            viewHolder.assetsButton.visibility = View.VISIBLE
            viewHolder.assetName.text = String.format(context.getString(com.blockeq.stellarwallet.R.string.asset_template),
                    totalBalance.assetName, getVisibleAssetCode(totalBalance.assetCode))
        }

        when(totalBalance.state) {
            WalletState.ERROR, WalletState.NOT_FUNDED -> {
                viewHolder.root.setBackgroundColor(getColor(context, R.color.paleSky))
                viewHolder.progressBar.visibility = View.GONE
            }
            WalletState.ACTIVE, WalletState.UPDATING -> {
                viewHolder.root.setBackgroundColor(getColor(context, R.color.blue2))
            }
            else -> { //nothing
            }
        }
    }

    private fun tintProgressBar(progressBar: ProgressBar, @ColorInt color : Int){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val wrapDrawable = DrawableCompat.wrap(progressBar.indeterminateDrawable)
            DrawableCompat.setTint(wrapDrawable, color)
            progressBar.indeterminateDrawable = DrawableCompat.unwrap<Drawable>(wrapDrawable)
        } else {
            progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun configureAvailableBalanceViewHolder(viewHolder : AvailableBalanceViewHolder, position : Int) {
        val availableBalance = items!![position] as AvailableBalance
        @SuppressLint("SetTextI18n")
        viewHolder.balance.text = "${availableBalance.balance} ${getVisibleAssetCode(availableBalance.assetCode)}"
        viewHolder.learnMoreButton.setOnClickListener { view ->
            onLearnMoreListener?.
                    onLearnMoreButtonClicked(view, availableBalance.assetCode, availableBalance.issuer, position)
        }
    }

    private fun getVisibleAssetCode(assetCode : String) : String {
        return if (assetCode != "native") {
            assetCode
        } else {
            "XLM"
        }
    }
    private fun configureTransactionHeaderViewHolder(viewHolder : TransactionHeaderViewHolder, position : Int) {
        val pair = items!![position] as Pair<*, *>
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
        val transaction = items!![position] as AccountEffect

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
        val trade = items!![position] as TradeEffect

        viewHolder.transactionType.text = String.format(context.getString(R.string.trade_item_template),
                StringFormat.formatAssetCode(trade.soldAsset), StringFormat.formatAssetCode(trade.boughtAsset))
        viewHolder.dot.setColorFilter(ContextCompat.getColor(context, R.color.paleSky), PorterDuff.Mode.SRC_IN)
        if (WalletApplication.userSession.getSessionAsset().assetCode == trade.boughtAsset) {
            viewHolder.amount.text = truncateDecimalPlaces(trade.boughtAmount)
        } else {
            viewHolder.amount.text = String.format(context.getString(R.string.bracket_template),
                    truncateDecimalPlaces(trade.soldAmount))
        }
        viewHolder.date.text = getFormattedDate(trade.createdAt)
    }

    //endregion
}

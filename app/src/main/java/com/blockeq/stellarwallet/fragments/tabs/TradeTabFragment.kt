package com.blockeq.stellarwallet.fragments.tabs

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.AfterTextChanged
import com.blockeq.stellarwallet.interfaces.OnItemSelected
import com.blockeq.stellarwallet.interfaces.OnTradeCurrenciesChanged
import com.blockeq.stellarwallet.interfaces.OnUpdateTradeTab
import com.blockeq.stellarwallet.models.AssetUtil
import com.blockeq.stellarwallet.models.Currency
import com.blockeq.stellarwallet.models.SelectionModel
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.vmodels.TradingViewModel
import kotlinx.android.synthetic.main.fragment_tab_trade.*
import kotlinx.android.synthetic.main.view_custom_selector.view.*
import org.stellar.sdk.responses.OrderBookResponse
import timber.log.Timber

class TradeTabFragment : Fragment(), View.OnClickListener, OnUpdateTradeTab {

    private lateinit var appContext : Context

    private var sellingCurrencies = mutableListOf<SelectionModel>()
    private var buyingCurrencies = mutableListOf<SelectionModel>()
    private var selectedSellingCurrency: SelectionModel? = null
    private var selectedBuyingCurrency: SelectionModel? = null
    private var holdingsAmount = 0f
    private lateinit var listener: OnTradeCurrenciesChanged
    private lateinit var tradingViewModel: TradingViewModel
    private var addedCurrencies : ArrayList<Currency> = ArrayList()
    private var latestBid: OrderBookResponse.Row? = null
    private var orderType : OrderType = OrderType.MARKET
    private var dataAvailable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext = view.context.applicationContext
        tradingViewModel = ViewModelProviders.of(this).get(TradingViewModel::class.java)

        buyingCustomSelector.editText.isEnabled = false
        refreshAddedCurrencies()
        setupListeners()

        Timber.d("TradeTabFragment{$this}#onViewCreated")
    }

    private fun setupListeners() {
        toggleMarket.setOnClickListener(this)
        toggleLimit.setOnClickListener(this)
        tenth.setOnClickListener(this)
        quarter.setOnClickListener(this)
        half.setOnClickListener(this)
        threeQuarters.setOnClickListener(this)
        all.setOnClickListener(this)
        submitTrade.setOnClickListener(this)
        sellingCustomSelector.isEnabled = true
        buyingCustomSelector.isEnabled = true

        sellingCustomSelector.editText.addTextChangedListener(object : AfterTextChanged() {
            override fun afterTextChanged(editable: Editable) {
                refreshSubmitTradeButton()
                updateBuyingValueIfNeeded()
            }
        })

        sellingCustomSelector.setSelectionValues(sellingCurrencies)
        sellingCustomSelector.spinner.onItemSelectedListener = object : OnItemSelected() {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSellingCurrency = sellingCurrencies[position]
                holdingsAmount = selectedSellingCurrency!!.holdings
                holdings.text = String.format(getString(R.string.holdings_amount),
                        holdingsAmount,
                        selectedSellingCurrency!!.label)
                resetBuyingCurrencies()
                buyingCurrencies.removeAt(position)
                buyingCustomSelector.setSelectionValues(buyingCurrencies)

                notifyParent(selectedSellingCurrency, selectedBuyingCurrency)

                refreshSubmitTradeButton()
            }
        }

        buyingCustomSelector.setSelectionValues(buyingCurrencies)
        buyingCustomSelector.spinner.onItemSelectedListener = object : OnItemSelected(){
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBuyingCurrency = buyingCurrencies[position]
                notifyParent(selectedSellingCurrency, selectedBuyingCurrency)

                refreshSubmitTradeButton()
            }
        }
    }

    private fun refreshSubmitTradeButton() {
        if (sellingCustomSelector.editText.text.isEmpty() || buyingCustomSelector.editText.text.isEmpty()) {
           submitTrade.isEnabled = false
        } else {
            submitTrade.isEnabled = sellingCustomSelector.editText.text.toString().toFloat() <= selectedSellingCurrency!!.holdings
        }
    }

    private fun updateBuyingValueIfNeeded() {
        if (sellingCustomSelector == null) return
        val stringText = sellingCustomSelector.editText.text.toString()
        if (stringText.isEmpty()) {
            buyingCustomSelector.editText.text.clear()
            return
        }

        if (orderType == OrderType.MARKET && latestBid != null) {
            val value = sellingCustomSelector.editText.text.toString().toFloatOrNull()
            val priceR = latestBid?.priceR
            if (value != null && priceR != null) {
                val intValue : Float = value.toFloat()
                val stringValue = String.format("%.4f", priceR.numerator*intValue/priceR.denominator*0.9999)
                buyingCustomSelector.editText.setText(stringValue)
            }

        }
    }

    private fun notifyParent(selling : SelectionModel?, buying : SelectionModel?) {
        if (selling != null && buying != null) {
            listener.onCurrencyChange(selling, buying)
        }
    }

    enum class OrderType {
        LIMIT,
        MARKET
    }

    override fun onClick(view: View) {
        val context = view.context.applicationContext
        when (view.id) {
            R.id.tenth -> sellingCustomSelector.editText.setText((0.1 * holdingsAmount).toString())
            R.id.quarter -> sellingCustomSelector.editText.setText((0.25 * holdingsAmount).toString())
            R.id.half -> sellingCustomSelector.editText.setText((0.5 * holdingsAmount).toString())
            R.id.threeQuarters -> sellingCustomSelector.editText.setText((0.75 * holdingsAmount).toString())
            R.id.all -> sellingCustomSelector.editText.setText(holdingsAmount.toString())
            R.id.toggleMarket -> {
                orderType = OrderType.MARKET
                toggleMarket.setBackgroundResource(R.drawable.left_toggle_selected)
                toggleLimit.setBackgroundResource(R.drawable.right_toggle)
                buyingCustomSelector.editText.isEnabled = false
            }
            R.id.toggleLimit -> {
                orderType = OrderType.LIMIT
                toggleLimit.setBackgroundResource(R.drawable.right_toggle_selected)
                toggleMarket.setBackgroundResource(R.drawable.left_toggle)
                buyingCustomSelector.editText.isEnabled = true
            }
            R.id.submitTrade -> {
                progressBar.visibility = View.VISIBLE
                submitTrade.isEnabled = false

                if (orderType == OrderType.MARKET && !dataAvailable) {
                    // buyingEditText should be empty at this moment
                    Toast.makeText(context,"not enough data to submit a market order, try a limit order", Toast.LENGTH_LONG).show()
                } else {
                    setSelectorsEnabled(false)
                    WalletApplication.userSession.getAvailableBalance()
                    Horizon.getCreateMarketOffer(object: Horizon.OnMarketOfferListener {
                        override fun onExecuted() {
                            Toast.makeText(context,"Order executed", Toast.LENGTH_LONG).show()
                            submitTrade.isEnabled = true
                            progressBar.visibility = View.GONE
                            sellingCustomSelector.editText.text.clear()
                            setSelectorsEnabled(true)
                        }

                        override fun onFailed(errorMessage : String) {
                            Toast.makeText(context, "Order failed: $errorMessage", Toast.LENGTH_LONG).show()
                            submitTrade.isEnabled = true
                            progressBar.visibility = View.GONE
                            setSelectorsEnabled(true)
                        }
                    }, AccountUtils.getSecretSeed(appContext), selectedSellingCurrency!!.asset!!, selectedBuyingCurrency!!.asset!!,
                            sellingCustomSelector.editText.text.toString(), (buyingCustomSelector.editText.text.toString().toFloat() / sellingCustomSelector.editText.text.toString().toFloat()).toString())
                }
            }
        }
    }

    private fun setSelectorsEnabled(isEnabled : Boolean) {
        sellingCustomSelector.isEnabled = isEnabled
        buyingCustomSelector.isEnabled = isEnabled
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = parentFragment as OnTradeCurrenciesChanged
        } catch (e: ClassCastException) {
            Timber.e("the parent activity must implement: %s", OnTradeCurrenciesChanged::class.java.simpleName)
        }
    }

    private fun resetBuyingCurrencies() {
        buyingCurrencies.clear()
        addedCurrencies.forEach {
            buyingCurrencies.add(it)
        }
    }

    private fun refreshAddedCurrencies() {
        val accounts = WalletApplication.localStore.balances
        if (accounts != null) {
            addedCurrencies.clear()
            var i = 0
            var native : Currency? = null
            accounts.forEach { it ->
                val currency = if(it.assetType != "native") {
                    Currency(i, it.assetCode, it.assetCode, it.balance.toFloat(), it.asset)
                } else {
                    native = Currency(i, AssetUtil.NATIVE_ASSET_CODE, "LUMEN", it.balance.toFloat(), it.asset)
                    native as Currency
                }
                addedCurrencies.add(currency)
                i++
            }

            native?.let {
                addedCurrencies.remove(it)
                addedCurrencies.add(0, it)
            }
        }

        sellingCurrencies.clear()
        buyingCurrencies.clear()
        addedCurrencies.forEach {
            sellingCurrencies.add(it)
            buyingCurrencies.add(it)
        }
    }

    override fun onLastOrderBookUpdated(asks: Array<OrderBookResponse.Row>, bids: Array<OrderBookResponse.Row>) {
        if (bids.isNotEmpty()) {
           latestBid = bids[0]
           updateBuyingValueIfNeeded()
           dataAvailable = true
        } else {
            dataAvailable = false
        }
    }

}
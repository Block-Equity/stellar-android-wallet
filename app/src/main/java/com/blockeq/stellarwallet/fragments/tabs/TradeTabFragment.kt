package com.blockeq.stellarwallet.fragments.tabs

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.interfaces.OnTradeCurrenciesChanged
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.vmodels.TradingViewModel
import kotlinx.android.synthetic.main.fragment_tab_trade.*
import kotlinx.android.synthetic.main.view_custom_selector.view.*
import org.stellar.sdk.Asset
import timber.log.Timber

class TradeTabFragment : Fragment(), View.OnClickListener {
    private lateinit var appContext : Context

    private var sellingCurrencies = mutableListOf<SelectionModel>()
    private var buyingCurrencies = mutableListOf<SelectionModel>()
    private var selectedSellingCurrency: SelectionModel? = null
    private var selectedBuyingCurrency: SelectionModel? = null
    private var holdingsAmount = 0f
    private lateinit var listener: OnTradeCurrenciesChanged
    private lateinit var tradingViewModel: TradingViewModel
    private var addedCurrencies : ArrayList<Currency> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext = view.context.applicationContext
        tradingViewModel = ViewModelProviders.of(this).get(TradingViewModel::class.java)

        buyingCustomSelector.editText.isEnabled = false
//        mockupData(true)
        refreshAddedCurrencies()
        setupListeners()

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

        sellingCustomSelector.editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(amount: Editable) {
                if (selectedSellingCurrency != null) {
                    if (amount.toString().isNotEmpty()) {
                        submitTrade.isEnabled = amount.toString().toFloat() <= selectedSellingCurrency!!.holdings
                    } else {
                        submitTrade.isEnabled = false
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        sellingCustomSelector.setSelectionValues(sellingCurrencies)
        sellingCustomSelector.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSellingCurrency = sellingCurrencies[position]
                holdingsAmount = selectedSellingCurrency!!.holdings
                holdings.text = getString(R.string.holdings_amount,
                        holdingsAmount,
                        selectedSellingCurrency!!.label)
//                mockupData(false)
                resetBuyingCurrencies()
                buyingCurrencies.removeAt(position)
                buyingCustomSelector.setSelectionValues(buyingCurrencies)

                notifyParent(selectedSellingCurrency, selectedBuyingCurrency)
            }
        }

        buyingCustomSelector.setSelectionValues(buyingCurrencies)
        buyingCustomSelector.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBuyingCurrency = buyingCurrencies[position]
                notifyParent(selectedSellingCurrency, selectedBuyingCurrency)
            }
        }
    }

    private fun notifyParent(selling : SelectionModel?, buying : SelectionModel?) {
        if (selling != null && buying != null) {
            listener.onCurrencyChange(selling, buying)
        }
    }

    override fun onClick(view: View) {
        val context = view.context.applicationContext
        when (view.id) {
            R.id.toggleMarket -> {
                toggleMarket.setBackgroundResource(R.drawable.left_toggle_selected)
                toggleLimit.setBackgroundResource(R.drawable.right_toggle)
                buyingCustomSelector.editText.isEnabled = false
            }
            R.id.toggleLimit -> {
                toggleLimit.setBackgroundResource(R.drawable.right_toggle_selected)
                toggleMarket.setBackgroundResource(R.drawable.left_toggle)
                buyingCustomSelector.editText.isEnabled = true
            }
            R.id.tenth -> {
                sellingCustomSelector.editText.setText((0.1 * holdingsAmount).toString())
            }
            R.id.quarter -> {
                sellingCustomSelector.editText.setText((0.25 * holdingsAmount).toString())
            }
            R.id.half -> {
                sellingCustomSelector.editText.setText((0.5 * holdingsAmount).toString())
            }
            R.id.threeQuarters -> {
                sellingCustomSelector.editText.setText((0.75 * holdingsAmount).toString())
            }
            R.id.all -> {
                sellingCustomSelector.editText.setText(holdingsAmount.toString())
            }
            R.id.submitTrade -> {
                WalletApplication.userSession.getAvailableBalance()
                Horizon.getCreateMarketOffer(object: Horizon.OnMarketOfferListener {
                    override fun onExecuted() {
                       Toast.makeText(context,"Order executed", Toast.LENGTH_LONG).show()
                    }

                    override fun onFailed(errorMessage : String) {
                        Toast.makeText(context, "Order failed: $errorMessage", Toast.LENGTH_LONG).show()

                    }

                }, AccountUtils.getSecretSeed(appContext), selectedSellingCurrency!!.asset!!, selectedBuyingCurrency!!.asset!!,
                        sellingCustomSelector.editText.text.toString(), buyingCustomSelector.editText.text.toString())
            }
        }
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
            accounts.forEach { it ->
                val currency = if(it.assetType != "native") {
                    Currency(i, it.assetCode, it.assetCode, it.balance.toFloat(), it.asset)
                } else {
                    Currency(i, "LMX", "LUMEN", it.balance.toFloat(), it.asset)
                }
                addedCurrencies.add(currency)
                i++
            }
        }

        sellingCurrencies.clear()
        buyingCurrencies.clear()
        addedCurrencies.forEach {
            sellingCurrencies.add(it)
            buyingCurrencies.add(it)
        }
//
//        addedCurrencies.drop(1).forEach {
//            sellingCurrencies.add(it)
//        }
    }

    // Mockup Data. This would be populated through an API or a DB call
    private fun mockupData(both: Boolean) {
        var accounts = WalletApplication.localStore.balances
        val xlm = Currency(1, "XLM", "Stellar", accounts!![2].balance.toFloat(), Asset.create("native", null, null))

        var CAD = accounts!![0].asset
        var PTS = accounts!![1].asset
        val cad = Currency(2, "CAD", "Canadian Dollar",  accounts!![0].balance.toFloat(), CAD)
        val usd = Currency(3, "PTS", "PTS",  accounts!![1].balance.toFloat(), PTS)
        if (both) {
            sellingCurrencies.add(xlm)
            sellingCurrencies.add(cad)
            sellingCurrencies.add(usd)
        }
        buyingCurrencies = mutableListOf<SelectionModel>()
        buyingCurrencies.add(xlm)
        buyingCurrencies.add(cad)
        buyingCurrencies.add(usd)
        // ******************
    }


}
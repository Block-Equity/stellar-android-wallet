package blockeq.com.stellarwallet.fragments.tabs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.interfaces.OnTradeCurrenciesChange
import blockeq.com.stellarwallet.models.Currency
import blockeq.com.stellarwallet.models.SelectionModel
import kotlinx.android.synthetic.main.fragment_tab_trade.*
import kotlinx.android.synthetic.main.view_custom_selector.view.*
import android.text.Editable
import android.text.TextWatcher
import blockeq.com.stellarwallet.services.networking.Horizon
import blockeq.com.stellarwallet.utils.AccountUtils
import org.stellar.sdk.Asset


class TradeTabFragment : Fragment(), View.OnClickListener {

    private var sellingCurrencies = mutableListOf<SelectionModel>()
    private var buyingCurrencies = mutableListOf<SelectionModel>()
    private var selectedSellingCurrency: SelectionModel? = null
    private var selectedBuyingCurrency: SelectionModel? = null
    private var holdingsAmount = 0f
    private lateinit var onTradeCurrenciesChange: OnTradeCurrenciesChange

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_trade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buyingCustomSelector.editText.isEnabled = false
        mockupData(true)
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
                selectedSellingCurrency = sellingCurrencies.get(position)
                holdingsAmount = selectedSellingCurrency!!.holdings
                holdings.text = getString(R.string.holdings_amount,
                        holdingsAmount,
                        selectedSellingCurrency!!.label)
                mockupData(false)
                buyingCurrencies.removeAt(position)
                buyingCustomSelector.setSelectionValues(buyingCurrencies)
                onTradeCurrenciesChange.onCurrencyChange(selectedSellingCurrency?.label,
                        selectedBuyingCurrency?.label)
            }
        }

        buyingCustomSelector.setSelectionValues(buyingCurrencies)
        buyingCustomSelector.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBuyingCurrency = buyingCurrencies.get(position)
                onTradeCurrenciesChange.onCurrencyChange(selectedSellingCurrency?.label,
                        selectedBuyingCurrency?.label)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
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
//                Horizon.getCreateMarketOffer(object: Horizon.OnMarketOfferListener {
//                    override fun onExecuted() {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                    override fun onFailed() {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                }, AccountUtils.getSecretSeed(activity!!.applicationContext), Asset.createNonNativeAsset(), Asset.createNonNativeAsset(), "2", "0.1")
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            onTradeCurrenciesChange = parentFragment as OnTradeCurrenciesChange
        } catch (e: ClassCastException) {

        }
    }

    // Mockup Data. This would be populated through an API or a DB call
    private fun mockupData(both: Boolean) {
        val xlm = Currency(1, "XLM", "Stellar", 38.7832f)
        val cad = Currency(2, "CAD", "Canadian Dollar", 100.00f)
        val usd = Currency(3, "USD", "US Dollar", 98.00f)
        val btc = Currency(4, "BTC", "Bitcoin", 0.23243414f)
        if (both) {
            sellingCurrencies.add(xlm)
            sellingCurrencies.add(cad)
            sellingCurrencies.add(usd)
            sellingCurrencies.add(btc)
        }
        buyingCurrencies = mutableListOf<SelectionModel>()
        buyingCurrencies.add(xlm)
        buyingCurrencies.add(cad)
        buyingCurrencies.add(usd)
        buyingCurrencies.add(btc)
        // ******************
    }

}